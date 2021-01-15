package com.nabto.simplepush.repository

import androidx.lifecycle.LiveData

interface PairedDevicesRepository {
    val pairedDevices : LiveData<List<PairedDevicesRow>>
    suspend fun upsertPairedDevice(productId: String, deviceId: String, fingerprint: String, sct: String) : Unit
}