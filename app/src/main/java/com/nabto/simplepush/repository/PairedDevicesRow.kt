package com.nabto.simplepush.repository

data class PairedDevicesRow(val productId : String, val deviceId : String, val updatedFcmToken : Boolean) {
}