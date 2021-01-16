package com.nabto.simplepush.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PairedDevicesDao {
    @Query("SELECT * FROM paired_devices")
    fun listPairedDevides() : LiveData<List<PairedDeviceEntity>>

    @Query("SELECT * FROM paired_devices")
    fun getPairedDevices() : List<PairedDeviceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPairedDevice(pairedDeviceEntity: PairedDeviceEntity)

    @Query("SELECT * FROM paired_devices WHERE product_id = :productId AND device_id = :deviceId")
    suspend fun getDevice(productId : String, deviceId : String) : PairedDeviceEntity

    @Query("UPDATE paired_devices SET updated_fcm_token = 0")
    suspend fun invalidateAllFcmTokens()

    @Query("UPDATE paired_devices SET updated_fcm_token = 1 WHERE product_id = :productId AND device_id = :deviceId")
    suspend fun updateFcmStatus(productId: String, deviceId: String)
}