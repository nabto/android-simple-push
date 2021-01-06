package com.nabto.simplepush.repository

import androidx.lifecycle.LiveData
import com.nabto.simplepush.edge.MdnsDevice

interface UnpairedDevicesRepository {
    fun getDevices() : LiveData<List<MdnsDevice>>
}