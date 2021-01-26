package com.nabto.simplepush

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


sealed class UpdateTokenResult {
    class Success : UpdateTokenResult()
    class Error(val error: Throwable) : UpdateTokenResult()
}

@AndroidEntryPoint
class SimplePushFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var pairedDevicesDao: PairedDevicesDao

    @Inject
    lateinit var nabtoClient: NabtoClient

    @Inject
    lateinit var settings: Settings

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private var notificationIdCounter: Int = 0

    private var CHANNEL_ID : String = "test"

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        invalidateAndUpdateAllTokens(token)
    }

    fun invalidateAndUpdateAllTokens(token: String) {
        scope.launch {
            pairedDevicesDao.invalidateAllFcmTokens()
            updateTokensOnAllDevices(token)
        }
    }

    suspend fun updateTokensOnAllDevices(token: String) {
        withContext(Dispatchers.IO) {
            var devices = pairedDevicesDao.getPairedDevices()
            for (device in devices) {
                updateDeviceAndUpdateDatabase(device, token)
            }
        }
    }

    suspend fun updateDeviceAndUpdateDatabase(device: PairedDeviceEntity, token: String) {
        var result = updateTokenOnDevice(device.productId, device.deviceId, token)
        when (result) {
            is UpdateTokenResult.Error -> {
                Log.d(
                    TAG,
                    "Failed to update token on the device ${device.productId} ${device.deviceId} error: ${result.error}"
                )
            }
            is UpdateTokenResult.Success -> {
                pairedDevicesDao.updateFcmStatus(device.productId, device.deviceId)
            }
        }
    }

    suspend fun updateTokenOnDevice(
        productId: String,
        deviceId: String,
        token: String
    ): UpdateTokenResult {
        val device = pairedDevicesDao.getDevice(productId, deviceId)
        var connection: Connection = Connection(nabtoClient, settings, productId, deviceId)
        var connectResult = connection.connect()
        when (connectResult) {
            is ConnectResult.Error -> return UpdateTokenResult.Error(connectResult.error)
            is ConnectResult.Success -> {
                var me = IAM.getMe(connection.connection)
                when (me) {
                    is GetMeResult.Error -> return UpdateTokenResult.Error(me.error)
                    is GetMeResult.NotPaired -> return UpdateTokenResult.Error(Exception("Not paired"))
                    is GetMeResult.Success -> {
                        var fcm: Fcm = Fcm(application.getString(R.string.project_id), token)
                        var user = me.user
                        var setFcmResult =
                            IAM.setUserFcm(connection.connection, user.Username, fcm)
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleMessage(remoteMessage)

    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
        createNotificationChannel()

        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationIdCounter++, builder.build())
        }

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "messages"
            val descriptionText = "messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    companion object {

        private const val TAG = "SimplePushFirebaseMsgService"
    }
}