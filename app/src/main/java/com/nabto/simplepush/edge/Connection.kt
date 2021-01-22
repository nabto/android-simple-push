package com.nabto.simplepush.edge

import com.nabto.edge.client.*
import com.nabto.edge.client.Connection
import com.nabto.edge.client.swig.Coap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.Exception

sealed class ConnectResult {
    class Success() : ConnectResult()
    data class Error(var error: Throwable) : ConnectResult()
}

class CoapRequest(val method : String, val path : String) {
    var payload : ByteArray? = null
    var contentFormat : Int = -1
}

class CoapResponse {
    var payload : ByteArray? = null
    var contentFormat : Int = -1
    var statusCode : Int = 0
}

sealed class CoapRequestResult {
    class Success(val coapResponse : CoapResponse) : CoapRequestResult() {}
    class NotConnected() : CoapRequestResult() {}
    class Error(val error : Throwable) : CoapRequestResult() {}
}

class Connection(val nabtoClient : NabtoClient, val settings: Settings, val productId: String, val deviceId: String) {
    val connection : com.nabto.edge.client.Connection = nabtoClient.createConnection();
    suspend fun connect() : ConnectResult {
        return withContext(Dispatchers.IO) {
            var options : JSONObject = JSONObject()
            options.put("ProductId", productId);
            options.put("DeviceId", deviceId);
            options.put("PrivateKey", settings.getPrivateKey())
            options.put("ServerKey", settings.getNabtoServerKey())
            options.put("ServerUrl", settings.getNabtoServerUrl(productId))

            connection.updateOptions(options.toString());
            try {
                connection.connect()
            } catch (e: Exception) {
                return@withContext ConnectResult.Error(e);
            }
            return@withContext ConnectResult.Success()
        }
    }

    suspend fun connect(sct: String, fingerprint: String): ConnectResult {
        return withContext(Dispatchers.IO) {
            var options: JSONObject = JSONObject()
            options.put("ProductId", productId);
            options.put("DeviceId", deviceId);
            options.put("PrivateKey", settings.getPrivateKey())
            options.put("ServerKey", settings.getNabtoServerKey())
            options.put("ServerUrl", settings.getNabtoServerUrl(productId))
            options.put("ServerConnectToken", sct)

            connection.updateOptions(options.toString());
            try {
                connection.connect()
            } catch (e: Exception) {
                return@withContext ConnectResult.Error(e);
            }
            if (connection.deviceFingerprint == fingerprint) {
                return@withContext ConnectResult.Success()
            } else {
                return@withContext ConnectResult.Error(Exception("Fingerprint mismatch"))
            }

        }
    }


    suspend fun coapRequest(coapRequest : CoapRequest) : CoapRequestResult {
        // try the request if the status is not connected, reconnect and try the request again.
        var result = coapRequestConnected(coapRequest)
        when (result) {
            is CoapRequestResult.NotConnected -> {
                var r = connect();
                when (r) {
                    is ConnectResult.Error -> return CoapRequestResult.Error(r.error)
                    is ConnectResult.Success -> return coapRequestConnected(coapRequest)
                }
            }
            else -> return result
        }
    }

    suspend fun coapRequestConnected(coapRequest: CoapRequest) : CoapRequestResult {
        var coap = connection.createCoap(coapRequest.method, coapRequest.path);
        if (coapRequest.payload != null) {
            coap.setRequestPayload(coapRequest.contentFormat, coapRequest.payload)
        }
        return withContext(Dispatchers.IO) {
            try {
                coap.execute()
                var response : CoapResponse = CoapResponse()
                response.statusCode = coap.responseStatusCode
                response.contentFormat = coap.responseContentFormat
                response.payload = coap.responsePayload
                return@withContext CoapRequestResult.Success(response)
            } catch (e : NabtoRuntimeException) {
                if (e.errorCode.errorCode == ErrorCodes.NOT_CONNECTED) {
                    return@withContext CoapRequestResult.NotConnected()
                }
            } catch (e : Throwable) {
                return@withContext CoapRequestResult.Error(e)
            }
            return@withContext CoapRequestResult.Error(Exception("not here"))
        }
        return CoapRequestResult.Error(Exception("TODO"))
    }

    suspend fun reconnect() {

    }
}