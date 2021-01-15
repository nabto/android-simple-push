package com.nabto.simplepush.edge

import com.nabto.edge.client.NabtoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception

sealed class ConnectResult {
    class Success() : ConnectResult()
    data class Error(var error: Throwable) : ConnectResult()
}

class Connection(val nabtoClient : NabtoClient, val settings: Settings) {
    val connection : com.nabto.edge.client.Connection = nabtoClient.createConnection();
    suspend fun connect(productId : String, deviceId : String) : ConnectResult {
        return withContext(Dispatchers.IO) {
            var options : JSONObject = JSONObject()
            options.put("ProductId", productId);
            options.put("DeviceId", deviceId);
            options.put("PrivateKey", settings.getPrivateKey())
            connection.updateOptions(options.toString());
            try {
                connection.connect()
            } catch (e: Exception) {
                return@withContext ConnectResult.Error(e);
            }
            return@withContext ConnectResult.Success()
        }
    }
}