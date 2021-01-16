package com.nabto.simplepush.edge

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nabto.edge.client.Connection
import com.nabto.simplepush.model.Empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class Result<out R> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}


@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
class Fcm(@JsonProperty("Token") val Token : String, @JsonProperty("ProjectId") val ProjectId : String) {
}

class User(
    val Username: String,
    val DisplayName: String?,
    val Fingerprint: String?,
    val Sct: String?,
    val Role: String?,
    val Fcm: Fcm?,
    var NotificationCategories: HashSet<String>?
) {
}
class LocalOpenPairRequest() {
    @JsonProperty("Username", required = true) var username : String = ""

}

class WrongStatusCodeException(val expected: Int, val actual: Int) : Exception("Wrong status returned expected: " + expected + " actual: " + actual) {}
class WrongContentFormatException(val expected: Int, val actual: Int) : Exception("Wrong content format returned expected: " + expected + " actual: " + actual) {}

object IAM {
    suspend fun openLocalPair(
        connection: Connection,
        username: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            var coap = connection.createCoap("POST", "iam/pairing/local-open")
            var f = CBORFactory();
            var mapper = ObjectMapper(f);
            var r = LocalOpenPairRequest();
            r.username = username
            val cborData = mapper.writeValueAsBytes(r);
            coap.setRequestPayload(60, cborData);
            coap.execute();
            var result : Result<Unit>
            if (coap.responseStatusCode != 201) {
                result = Result.Error(
                    WrongStatusCodeException(
                        201,
                        coap.responseStatusCode
                    )
                )
            } else {
                result = Result.Success<Unit>(Unit);
            }
            return@withContext result
        }
    }

    suspend fun getMe(connection: Connection): Result<User> {
        return withContext(Dispatchers.IO) {
            var coap = connection.createCoap("GET", "/iam/me")
            coap.execute()
            if (coap.responseStatusCode != 205) {
                return@withContext Result.Error(
                    WrongStatusCodeException(
                        205,
                        coap.responseStatusCode
                    )
                )
            }
            if (coap.responseContentFormat != 60) {
                return@withContext Result.Error(
                    WrongContentFormatException(
                        60,
                        coap.responseContentFormat
                    )
                )
            }
            var f = CBORFactory();
            var mapper = ObjectMapper(f);
            mapper.registerKotlinModule()
            var user = mapper.readValue<User>(coap.responsePayload)
            return@withContext Result.Success(user)
        }
    }

    suspend fun setUserFcm(connection: Connection, username : String, fcm : Fcm) : Result<Empty> {
        return withContext(Dispatchers.IO) {
            var coap = connection.createCoap("PUT", "/iam/users/" + username + "/fcm")

            var f = CBORFactory();
            var mapper = ObjectMapper(f);
            val cborData = mapper.writeValueAsBytes(fcm);
            coap.setRequestPayload(60, cborData);
            coap.execute();
            var result : Result<Empty>
            if (coap.responseStatusCode != 204) {
                result = Result.Error(
                    WrongStatusCodeException(
                        204,
                        coap.responseStatusCode
                    )
                )
            } else {
                result = Result.Success<Empty>(Empty());
            }
            return@withContext result
        }
    }

    suspend fun setUserNotificationCategories(connection: Connection, username : String, categories : Set<String>) : Result<Empty> {
        return withContext(Dispatchers.IO) {
            try {
                var coap = connection.createCoap(
                    "PUT",
                    "/iam/users/" + username + "/notification-categories"
                )

                var f = CBORFactory();
                var mapper = ObjectMapper(f);
                val cborData = mapper.writeValueAsBytes(categories);
                coap.setRequestPayload(60, cborData);
                coap.execute();
                var result: Result<Empty>
                if (coap.responseStatusCode != 204) {
                    result = Result.Error(
                        WrongStatusCodeException(
                            204,
                            coap.responseStatusCode
                        )
                    )
                } else {
                    result = Result.Success<Empty>(Empty());
                }
                return@withContext result
            } catch (e : Throwable) {
                return@withContext Result.Error(e)
            }
        }
    }

}
