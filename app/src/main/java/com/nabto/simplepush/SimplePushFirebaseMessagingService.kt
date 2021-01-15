package com.nabto.simplepush

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class SimplePushFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToDevices(token)
    }

    fun sendRegistrationToDevices(token : String) {

    }

    fun getFcmProjectId(context: Context) : String {
        return context.getString(R.string.project_id)
    }

    companion object {

        private const val TAG = "SimplePushFirebaseMsgService"
    }
}