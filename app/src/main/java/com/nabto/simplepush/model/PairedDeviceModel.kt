package com.nabto.simplepush.model

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.R
import com.nabto.simplepush.edge.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

sealed class GetUserResult {
    data class Success(val user : User) : GetUserResult();
    class NotPaired() : GetUserResult();
    class NotConnected() : GetUserResult();
    data class Error(val error : Throwable) : GetUserResult()
}

sealed class UpdateFcmTokenResult {
    class Success() : UpdateFcmTokenResult();
    data class Error(val error: Throwable) : UpdateFcmTokenResult();
}

class PairedDeviceModel(
    val nabtoClient: NabtoClient,
    val application: Application,
    val settings: Settings,
    val productId: String,
    val deviceId: String) {
    suspend fun getUser() : GetUserResult {
        return GetUserResult.Error(NotImplementedError("foo"))
    }
    suspend fun updateFcmToken() : UpdateFcmTokenResult {
        var connection : Connection = Connection(nabtoClient, settings);

        var connectResult = connection.connect(productId, deviceId);
        when (connectResult) {
            is ConnectResult.Error -> return UpdateFcmTokenResult.Error(connectResult.error)
        }

        var t : String = FirebaseMessaging.getInstance().token.await();

        var fcm : Fcm = Fcm(t, application.getString(R.string.project_id));

        var user : User
        var u = IAM.getMe(connection.connection)
        when(u) {
            is Result.Error -> return UpdateFcmTokenResult.Error(u.exception)
            is Result.Success<User> -> user = u.data;
        }

        var r = IAM.setUserFcm(connection.connection, user.Username, fcm)
        when(r) {
            is Result.Error -> return UpdateFcmTokenResult.Error(r.exception)
            is Result.Success<Empty> -> return UpdateFcmTokenResult.Success()
        }
    }
    suspend fun hasUpdatedFcmToken() : Boolean {
        return false;
    }
}