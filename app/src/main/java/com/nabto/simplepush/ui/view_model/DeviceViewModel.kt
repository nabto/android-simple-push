package com.nabto.simplepush.ui.view_model

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.R
import com.nabto.simplepush.dao.PairedDevicesDao
import kotlinx.coroutines.launch
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.edge.*
import com.nabto.simplepush.model.Empty
import com.nabto.simplepush.ui.device.DeviceConnectingFragment
import com.nabto.simplepush.ui.device.DeviceConnectingFragmentArgs
import com.nabto.simplepush.ui.device.DeviceConnectingFragmentDirections
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragmentDirections
import kotlinx.coroutines.tasks.await

sealed class DeviceConnectResult {
    class Unpaired() : DeviceConnectResult();
    class Connected() : DeviceConnectResult();
    class Error(error : Throwable) : DeviceConnectResult();
}

sealed class DeviceUpdateFcmResult {
    class Connected() : DeviceUpdateFcmResult();
    class Error(val error : Throwable) : DeviceUpdateFcmResult()
}

class DeviceViewModel constructor(val application : Application,
                                  val pairedDevicesDao: PairedDevicesDao,
                                  val settings : Settings,
                                  val nabtoClient: NabtoClient,
                                  val productId : String,
                                  val deviceId : String) : ViewModel()
{

    lateinit var connection : Connection;



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
                    return DeviceConnectResult.Unpaired()
                }
                // TODO check fingerprint of the device that it matches the fingerprint in the database

                var result = IAM.getMe(connection.connection)
                when (result) {
                    is Result.Error -> {
                        return DeviceConnectResult.Unpaired()
                    }
                    is Result.Success<User> -> {
                        if (pairedDeviceEntity.updatedFcmToken) {
                            return DeviceConnectResult.Connected()
                        } else {

                            var updateFcmResult = updateFcm(result.data)
                            when(updateFcmResult) {
                                is DeviceUpdateFcmResult.Connected -> return DeviceConnectResult.Connected()
                                is DeviceUpdateFcmResult.Error -> return DeviceConnectResult.Error(updateFcmResult.error)
                            }
                        }
                    }
                }
            }
            is ConnectResult.Error -> {
                return DeviceConnectResult.Error(result.error);
            }
        }
    }

    suspend fun isPaired() {
        var result = IAM.getMe(connection.connection)
    }

    fun pair() {
        viewModelScope.launch {
            val me = IAM.getMe(connection.connection);
            when(me) {
                is Result.Success<User> -> {
                    // the user is already paired update the database with the device and navigate to the device.
                    pairedDevicesDao.upsertPairedDevice(PairedDeviceEntity(productId, deviceId, me.data.Sct ?: "", me.data.Fingerprint?:"", false))


                    Toast.makeText(application, "Already paired", Toast.LENGTH_LONG).show()
                    // We was paired and now redirect to update fcm
                }
            }

            val result = IAM.openLocalPair(connection.connection, "foo")
            when (result) {
                is Result.Success<Unit> -> {}
                is Result.Error -> {
                    val e = result;
                    Toast.makeText(application, e.exception.message, Toast.LENGTH_LONG ).show()
                }
            }
        }
    }

    suspend fun updateFcm(user : User) : DeviceUpdateFcmResult {


        var t : String = FirebaseMessaging.getInstance().token.await();

        var fcm : Fcm = Fcm(t, application.getString(R.string.project_id));

        var r = IAM.setUserFcm(connection.connection, user.Username, fcm)
        when(r) {
            is Result.Error -> return DeviceUpdateFcmResult.Error(r.exception)
            is Result.Success<Empty> -> {
                pairedDevicesDao.updateFcmStatus(productId,deviceId);
                return DeviceUpdateFcmResult.Connected()
            }
        }
    }


}