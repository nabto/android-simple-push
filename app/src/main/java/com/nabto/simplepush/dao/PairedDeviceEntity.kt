package com.nabto.simplepush.dao

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["product_id", "device_id"], tableName = "paired_devices")
data class PairedDeviceEntity (
    @ColumnInfo(name = "product_id") val productId : String,
    @ColumnInfo(name = "device_id") val deviceId : String,
    @ColumnInfo(name = "sct") val sct : String,
    @ColumnInfo(name = "fingerprint") val fingerprint : String,
    @ColumnInfo(name = "updated_fcm_token") val updatedFcmToken : Boolean
    )
{
}



