package com.nabto.simplepush.edge

import android.content.Context
import android.content.SharedPreferences
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.R

class Settings(val nabtoClient: NabtoClient, val context: Context) {
    fun createPrivateKey(sharedPreferences: SharedPreferences) {
        val key = nabtoClient.createPrivateKey();
        with (sharedPreferences.edit()) {
            putString(context.getString(R.string.preference_private_key), key);
            apply()
        }
    }
    fun getPrivateKey() : String {
        val sharedPref = context?.getSharedPreferences(context.getString(R.string.preference_file), Context.MODE_PRIVATE) ?: return ""
        if (!sharedPref.contains(context.getString(R.string.preference_private_key))) {
            createPrivateKey(sharedPref)
        }

        return sharedPref.getString(context.getString(R.string.preference_private_key), null) ?: return "";
    }

    fun getNabtoServerKey() : String {
        return context.getString(R.string.nabto_server_key)
    }

    fun getNabtoServerUrl(productId : String) : String {
        return "https://"+productId+context.getString(R.string.nabto_server_url_suffix)
    }
}