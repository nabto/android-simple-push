package com.nabto.simplepush.ui.view_model

import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragmentDirections

data class PairedDevicesRowViewModel(val productId : String, val deviceId : String, val updatedFcmToken : Boolean) {

    fun onClick(view : View) {
        val action = PairedDevicesFragmentDirections.actionPairedDevicesFragmentToDeviceSettingsFragment(productId,deviceId)
        view.findNavController().navigate(action);
    }
}