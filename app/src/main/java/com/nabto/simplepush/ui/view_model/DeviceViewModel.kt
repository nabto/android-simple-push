package com.nabto.simplepush.ui.view_model

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.nabto.edge.client.ErrorCode
import com.nabto.edge.client.NabtoClient
import com.nabto.edge.client.NabtoNoChannelsException
import com.nabto.simplepush.R
import com.nabto.simplepush.dao.PairedDevicesDao
import kotlinx.coroutines.launch
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.edge.*
import com.nabto.simplepush.model.Empty
import com.nabto.simplepush.ui.device.*
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class DeviceConnectResult {
    class Unpaired() : DeviceConnectResult();
    class Connected() : DeviceConnectResult();
    class Error(val error: Throwable) : DeviceConnectResult();
}

sealed class DeviceUpdateFcmResult {
    class Success() : DeviceUpdateFcmResult();
    class Error(val error: Throwable) : DeviceUpdateFcmResult()
}

sealed class NotificationCategoriesChangedResult {
    class Success() : NotificationCategoriesChangedResult();
    class Error(val error: Throwable) : NotificationCategoriesChangedResult()
}

enum class DeviceViewModelState {
    SETTINGS, ERROR, NOT_PAIRED
}

class DeviceViewModel constructor(
    val application: Application,
    val pairedDevicesDao: PairedDevicesDao,
    val settings: Settings,
    val nabtoClient: NabtoClient,
    val productId: String,
    val deviceId: String
) : ViewModel() {

    lateinit var connection: Connection;

    val state: MutableLiveData<DeviceViewModelState> = MutableLiveData(DeviceViewModelState.SETTINGS)

    val loading : MutableLiveData<Boolean> = MutableLiveData(true)

    val user: MutableLiveData<User> = MutableLiveData()

    val notificationCategories : MutableLiveData<Set<String>> = MutableLiveData(HashSet<String>())

    val notificationCategoryState : HashMap<String, MutableLiveData<Boolean>> = HashMap<String, MutableLiveData<Boolean>>()

    fun getNotificationCategoryState(category : String) : MutableLiveData<Boolean> {
        if (!notificationCategoryState.containsKey(category)) {
            notificationCategoryState[category] = MutableLiveData(false)
        }
        return notificationCategoryState[category]!!
    }

    var lastError: MutableLiveData<String> = MutableLiveData("")


    suspend fun notificationCategoriesChanged(
        category: String,
        state: Boolean
    ) {
        var u: User = user.value!!;
        if (u.NotificationCategories == null) {
            u.NotificationCategories = HashSet<String>()
        }
        if (state) {
            u.NotificationCategories?.add(category);
        } else {
            u.NotificationCategories?.remove(category)
        }
        user.postValue(u);

        updateNotificationCategories(u.NotificationCategories!!)
    }

    fun updateLastError(err: Throwable) {
        lastError.postValue(err.toString())
    }

    suspend fun updateNotificationCategories(categories: Set<String>) {
        var result = IAM.setUserNotificationCategories(
            connection.connection,
            user.value!!.Username,
            categories
        )
        when (result) {
            is Result.Success<Empty> -> {
                // nothing
            }
            is Result.Error -> {
                setError(result.exception)
            }
        }
    }

    fun setError(error : Throwable) {
        lastError.postValue(error.toString())
        state.postValue(DeviceViewModelState.ERROR)
    }

    suspend fun start() {
        loading.postValue(true)
        var r = doStart()
        loading.postValue(false)

        when (r) {
            is DeviceConnectResult.Unpaired -> {
                state.postValue(DeviceViewModelState.NOT_PAIRED)
            }
            is DeviceConnectResult.Connected -> {
                state.postValue(DeviceViewModelState.SETTINGS)
            }
            is DeviceConnectResult.Error -> {
                setError(r.error);
            }
        }
    }

    suspend fun doStart(): DeviceConnectResult {
        var r = connect()
        when (r) {
            is DeviceConnectResult.Connected -> {
                var cats = IAM.getNotificationCategories(connection.connection)
                when (cats) {
                    is Result.Success<List<String>> -> {
                        notificationCategories.postValue(HashSet<String>(cats.data))
                        return DeviceConnectResult.Connected()
                    }
                    is Result.Error -> {
                        return DeviceConnectResult.Error(cats.exception)
                    }
                }
            }
        }
        return r;

    }

    fun setUser(u : User) {
        user.postValue(u);

        u.NotificationCategories?.forEach { it ->

            if (!notificationCategoryState.containsKey(it)) {
                notificationCategoryState[it] = MutableLiveData(false)
            }

            notificationCategoryState[it]?.postValue(true);
        }
    }

    suspend fun connect(): DeviceConnectResult {
        var pairedDeviceEntity: PairedDeviceEntity
        try {
            pairedDeviceEntity = pairedDevicesDao.getDevice(productId, deviceId)
        } catch (e: Exception) {
            return DeviceConnectResult.Unpaired()
        }
        connection = Connection(nabtoClient, settings, productId, deviceId)
        var result = connection.connect(pairedDeviceEntity.sct, pairedDeviceEntity.fingerprint);
        when (result) {
            is ConnectResult.Success -> {
                var result = IAM.getMe(connection.connection)
                when (result) {
                    is GetMeResult.Error -> {
                        return DeviceConnectResult.Error(result.error)
                    }
                    is GetMeResult.NotPaired -> {
                        return DeviceConnectResult.Unpaired()
                    }
                    is GetMeResult.Success -> {
                        setUser(result.user);
                        if (pairedDeviceEntity.updatedFcmToken) {
                            return DeviceConnectResult.Connected()
                        } else {

                            var updateFcmResult = updateFcm(result.user)
                            when (updateFcmResult) {
                                is DeviceUpdateFcmResult.Success -> return DeviceConnectResult.Connected()
                                is DeviceUpdateFcmResult.Error -> {
                                    updateLastError(updateFcmResult.error)
                                    return DeviceConnectResult.Error(updateFcmResult.error)
                                }
                            }
                        }
                    }
                }
            }
            is ConnectResult.Error -> {
                when (result.error) {
                    is NabtoNoChannelsException -> {
                        var e : NabtoNoChannelsException = result.error as NabtoNoChannelsException
                        var remoteError : ErrorCode = e.remoteChannelErrorCode
                        var localError : ErrorCode = e.localChannelErrorCode

                        Log.d(TAG, "LocalError ${localError.description}, remoteError ${remoteError.description}");
                    }
                }
                return DeviceConnectResult.Error(result.error);
            }
        }
    }

    suspend fun updateFcm(user: User): DeviceUpdateFcmResult {
        var t: String = FirebaseMessaging.getInstance().token.await();

        var fcm: Fcm = Fcm(t, application.getString(R.string.project_id));

        var r = IAM.setUserFcm(connection.connection, user.Username, fcm)
        when (r) {
            is Result.Error -> {
                updateLastError(r.exception)
                return DeviceUpdateFcmResult.Error(r.exception)
            }
            is Result.Success<Empty> -> {
                pairedDevicesDao.updateFcmStatus(productId, deviceId);
                return DeviceUpdateFcmResult.Success()
            }
        }
    }

    suspend fun sendTestNotification() {
        loading.postValue(true)
        var r = IAM.sendTestNotification(connection.connection, user.value!!.Username)
        loading.postValue(false)
        when (r) {
            is Result.Error -> {
                setError(r.exception)
            }
        }
    }

    companion object {
        public const val TAG : String = "DeviceViewModel"
    }

}