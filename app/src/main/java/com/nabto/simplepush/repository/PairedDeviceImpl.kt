package com.nabto.simplepush.repository

import android.app.Application
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.R
import com.nabto.simplepush.edge.*
import com.nabto.simplepush.model.Empty
import kotlinx.coroutines.tasks.await

class PairedDeviceImpl(
    val application : Application,
    val settings : Settings,
    val nabtoClient : NabtoClient,
    override val productId: String,
    override val deviceId: String,
    override val sct: String,
    override val fingerprint: String
) : PairedDevice {
    override suspend fun updateFcmToken() : Result<Empty> {
        var connection : Connection = Connection(nabtoClient, settings);

        var connectResult = connection.connect(productId, deviceId);
        when (connectResult) {
            is ConnectResult.Error -> return Result.Error(connectResult.error)
        }

        var t : String = FirebaseMessaging.getInstance().token.await();

        var fcm : Fcm = Fcm(t, application.getString(R.string.project_id));

        var user : User
        var u = IAM.getMe(connection.connection)
        when(u) {
            is Result.Error -> return Result.Error(u.exception)
            is Result.Success<User> -> user = u.data;
        }

        var r = IAM.setUserFcm(connection.connection, user.Username, fcm)
        when(r) {
            is Result.Error -> return Result.Error(r.exception)
            is Result.Success<Empty> -> return Result.Success<Empty>(Empty())
        }
    }
}