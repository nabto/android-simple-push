package com.nabto.simplepush

import android.content.Context
import android.util.Log
import androidx.room.Update
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

sealed class UpdateTokenResult {
    class Success() : UpdateTokenResult()
    class Error(val error : Throwable) : UpdateTokenResult()
}

@AndroidEntryPoint
class SimplePushFirebaseMessagingService() : FirebaseMessagingService() {

    @Inject
    lateinit var pairedDevicesDao: PairedDevicesDao

    @Inject
    lateinit var nabtoClient: NabtoClient

    @Inject
    lateinit var settings : Settings;

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        invalidateAndUpdateAllTokens(token)
    }

    fun invalidateAndUpdateAllTokens(token : String) {
        scope.launch {
            pairedDevicesDao.invalidateAllFcmTokens()
            updateTokensOnAllDevices(token)
        }
    }

    suspend fun updateTokensOnAllDevices(token : String) {
        withContext(Dispatchers.IO) {
            var devices = pairedDevicesDao.getPairedDevices()
            for (device in devices) {
                updateDeviceAndUpdateDatabase(device, token)
            }
        }
    }

    suspend fun updateDeviceAndUpdateDatabase(device : PairedDeviceEntity, token : String) {
        var result = updateTokenOnDevice(device.productId, device.deviceId, token)
        when (result) {
            is UpdateTokenResult.Error -> {
                Log.d(TAG, "Failed to update token on the device ${device.productId} ${device.deviceId} error: ${result.error.toString()}")
            }
            is UpdateTokenResult.Success -> {
                pairedDevicesDao.updateFcmStatus(device.productId, device.deviceId)
            }
        }
    }

    suspend fun updateTokenOnDevice(productId : String, deviceId : String, token : String) : UpdateTokenResult {
        val device = pairedDevicesDao.getDevice(productId,deviceId)
        var connection : Connection = Connection(nabtoClient,settings, productId, deviceId);
        var connectResult = connection.connect();
        when(connectResult) {
            is ConnectResult.Error -> return UpdateTokenResult.Error(connectResult.error)
            is ConnectResult.Success -> {
                var me = IAM.getMe(connection.connection)
                when (me) {
                    is GetMeResult.Error -> return UpdateTokenResult.Error(me.error)
                    is GetMeResult.NotPaired -> return UpdateTokenResult.Error(Exception("Not paired"));
                    is GetMeResult.Success -> {
                        var fcm : Fcm = Fcm(application.getString(R.string.project_id), token)
                        var user = me.user
                        var setFcmResult = IAM.setUserFcm(connection.connection, user.Username, fcm);
                        when (setFcmResult) {
                            is Result.Success -> return UpdateTokenResult.Success()
                            is Result.Error -> return UpdateTokenResult.Error(setFcmResult.exception)
                        }
                    }
                }
            }
        }
        return UpdateTokenResult.Error(Exception("never here"))
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    companion object {

        private const val TAG = "SimplePushFirebaseMsgService"
    }
}