package com.nabto.simplepush.ui.view_model

import android.view.View
import androidx.navigation.findNavController
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragmentDirections
import com.nabto.simplepush.ui.unpaired_devices.UnpairedDevicesFragmentDirections

data class UnpairedDevicesRowViewModel(val productId : String, val deviceId :String) {
    fun onClick(view : View) {
        val action = UnpairedDevicesFragmentDirections.actionUnpairedDevicesFragmentToDeviceSettingsFragment(productId,deviceId)
        view.findNavController().navigate(action);
    }
}