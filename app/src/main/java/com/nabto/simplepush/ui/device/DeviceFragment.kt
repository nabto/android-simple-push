package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.Settings
import com.nabto.simplepush.ui.user_settings.UserSettingsFragmentArgs
import com.nabto.simplepush.ui.view_model.DeviceViewModel
import com.nabto.simplepush.ui.view_model.DeviceViewModelFactory
import com.nabto.simplepush.ui.view_model.UserSettingsViewModel
import com.nabto.simplepush.ui.view_model.UserSettingsViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class DeviceFragment : Fragment()  {

    protected lateinit var viewModelFactory : DeviceViewModelFactory
    protected lateinit var viewModel: DeviceViewModel


    @Inject
    lateinit var nabtoClient: NabtoClient
    @Inject
    lateinit var settings : Settings
    @Inject
    lateinit var pairedDevicesDao: PairedDevicesDao

    fun initViewModel(productId : String, deviceId : String) {
        viewModelFactory = DeviceViewModelFactory(
            requireActivity().application,
            pairedDevicesDao,
            settings,
            nabtoClient,
            productId,
            deviceId
        )
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(DeviceViewModel::class.java)
    }
}