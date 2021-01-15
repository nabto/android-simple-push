package com.nabto.simplepush.repository

import com.nabto.simplepush.edge.Result
import com.nabto.simplepush.edge.User
import com.nabto.simplepush.model.Empty

interface PairedDevice
{
    val productId : String
    val deviceId : String
    val sct : String
    val fingerprint : String

    suspend fun updateFcmToken() : Result<Empty>

   // suspend fun getUser() : Result<User>
}