package com.nabto.simplepush.ui.view_model

import android.app.Application
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.R
import com.nabto.simplepush.dao.PairedDevicesDao
import kotlinx.coroutines.launch
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.edge.*
import com.nabto.simplepush.model.Empty
import com.nabto.simplepush.ui.device.*
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragmentDirections
import kotlinx.coroutines.tasks.await

sealed class DeviceConnectResult {
    class Unpaired() : DeviceConnectResult();
    class Connected() : DeviceConnectResult();
    class Error(error : Throwable) : DeviceConnectResult();
}

sealed class DeviceUpdateFcmResult {
    class Success() : DeviceUpdateFcmResult();
    class Error(val error : Throwable) : DeviceUpdateFcmResult()
}

sealed class DevicePairResult {
    class Paired() : DevicePairResult()
    class Error(val error : Throwable) : DevicePairResult()
}

class DeviceViewModel constructor(val application : Application,
                                  val pairedDevicesDao: PairedDevicesDao,
                                  val settings : Settings,
                                  val nabtoClient: NabtoClient,
                                  val productId : String,
                                  val deviceId : String) : ViewModel()
{

    lateinit var connection : Connection;

    val user : MutableLiveData<User> = MutableLiveData()



    var lastError : MutableLiveData<String> = MutableLiveData("")

    val notificationAlarmState : LiveData<Boolean> =
        Transformations.map(user) {
                u -> u.NotificationCategories?.contains("Alarm")
        }

    val notificationWarningState : LiveData<Boolean> =
        Transformations.map(user) { u -> u.NotificationCategories?.contains("Warn") }

    val notificationInfoState : LiveData<Boolean> =
        Transformations.map(user) { u -> u.NotificationCategories?.contains("Info") }

    fun notificationCategoriesChanged(view : View, category : String, state : Boolean)
    {
        var u : User = user.value!!;
        if (u.NotificationCategories == null) {
            u.NotificationCategories = HashSet<String>()
        }
        if (state) {
            u.NotificationCategories?.add(category);
        } else {
            u.NotificationCategories?.remove(category)
        }
        user.postValue(u);
        updateNotificationCategories(view, u.NotificationCategories!!)
    }

    fun updateLastError(err : Throwable) {
        lastError.postValue(err.toString())
    }

    fun updateNotificationCategories(view : View, categories : Set<String>) {
        viewModelScope.launch {
            var result = IAM.setUserNotificationCategories(
                connection.connection,
                user.value!!.Username,
                categories
            )
            when(result) {
                is Result.Success<Empty> -> {
                    // do nothing
                }
                is Result.Error -> {
                    updateLastError(result.exception);
                    var action = DeviceSettingsFragmentDirections.actionDeviceSettingsFragmentToDeviceDisconnectedFragment(productId,deviceId)
                    view.findNavController().navigate(action);
                }
            }
        }
    }

    suspend fun connect() : DeviceConnectResult {
        connection = Connection(nabtoClient, settings)
        var result = connection.connect(productId, deviceId);
        when (result) {
            is ConnectResult.Success -> {
                var pairedDeviceEntity : PairedDeviceEntity
                try {
                    pairedDeviceEntity = pairedDevicesDao.getDevice(productId, deviceId)
                } catch (e: Exception) {
                    // the device is not found in our local database of devices
                    // goto pairing.
                        updateLastError(e)
                    return DeviceConnectResult.Unpaired()
                }
                // TODO check fingerprint of the device that it matches the fingerprint in the database

                var result = IAM.getMe(connection.connection)
                when (result) {
                    is Result.Error -> {
                        updateLastError(result.exception)
                        return DeviceConnectResult.Unpaired()
                    }
                    is Result.Success<User> -> {
                        user.postValue(result.data);
                        if (pairedDeviceEntity.updatedFcmToken) {
                            return DeviceConnectResult.Connected()
                        } else {

                            var updateFcmResult = updateFcm(result.data)
                            when(updateFcmResult) {
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
                updateLastError(result.error)
                return DeviceConnectResult.Error(result.error);
            }
        }
    }

    suspend fun isPaired() {
        var result = IAM.getMe(connection.connection)
    }

    suspend fun upsertDeviceInDatabase(user : User) {
        pairedDevicesDao.upsertPairedDevice(
                    PairedDeviceEntity(
                        productId,
                        deviceId,
                        user.Sct ?: "",
                        connection.connection.deviceFingerprint,
                        false
                    )
                )
    }

    suspend fun getUserAndUpdateState() : DevicePairResult {
        var me = IAM.getMe(connection.connection);
        when (me) {
            is Result.Success<User> -> {
                upsertDeviceInDatabase(me.data)
                var updateFcmResult = updateFcm(me.data)
                when (updateFcmResult) {
                    is DeviceUpdateFcmResult.Success -> return DevicePairResult.Paired()
                    is DeviceUpdateFcmResult.Error -> {
                        updateLastError(updateFcmResult.error)
                        return DevicePairResult.Error(updateFcmResult.error)
                    }
                }
            }
            is Result.Error -> {
                updateLastError(me.exception)
                DevicePairResult.Error(me.exception)
            }
        }
        return DevicePairResult.Error(Exception("Never here"))
    }


    suspend fun pair(username: String): DevicePairResult {
        var r = getUserAndUpdateState()
        when (r) {
            is DevicePairResult.Paired -> return DevicePairResult.Paired()
        }
        // we are not paired or some error happened.
        val result = IAM.openLocalPair(connection.connection, username)
        when (result) {
            is Result.Error -> {
                updateLastError(result.exception)
                return DevicePairResult.Error(result.exception)
            }
        }
        return getUserAndUpdateState()
    }

    suspend fun updateFcm(user : User) : DeviceUpdateFcmResult {
        var t : String = FirebaseMessaging.getInstance().token.await();

        var fcm : Fcm = Fcm(t, application.getString(R.string.project_id));

        var r = IAM.setUserFcm(connection.connection, user.Username, fcm)
        when(r) {
            is Result.Error -> {
                updateLastError(r.exception)
                return DeviceUpdateFcmResult.Error(r.exception)
            }
            is Result.Success<Empty> -> {
                pairedDevicesDao.updateFcmStatus(productId,deviceId);
                return DeviceUpdateFcmResult.Success()
            }
        }
    }

    fun reconnectClick(view : View) {
        var action = DeviceDisconnectedFragmentDirections.actionDeviceDisconnectedFragmentToDeviceConnectingFragment(productId,deviceId)
        view.findNavController().navigate(action);
        viewModelScope.launch {
            connect();
        }
    }

}