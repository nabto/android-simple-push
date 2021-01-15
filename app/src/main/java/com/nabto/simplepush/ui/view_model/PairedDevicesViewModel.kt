package com.nabto.simplepush.ui.view_model

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.repository.PairedDevice
import com.nabto.simplepush.repository.PairedDevicesRepository
import com.nabto.simplepush.repository.PairedDevicesRow

class PairedDevicesViewModel @ViewModelInject constructor(val pairedDevicesRepository: PairedDevicesRepository) : ViewModel() {
    val pairedDevices : LiveData<List<PairedDevicesRow>> = pairedDevicesRepository.pairedDevices

}