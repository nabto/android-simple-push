package com.nabto.simplepush.ui.view_model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.Settings

class DeviceViewModelFactory(val application : Application, val pairedDevicesDao : PairedDevicesDao, val settings : Settings, val nabtoClient: NabtoClient, val productId : String, val deviceId : String) :
    ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {

            return DeviceViewModel(application, pairedDevicesDao, settings, nabtoClient, productId, deviceId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}