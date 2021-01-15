package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.databinding.DeviceConnectingFragmentBinding
import com.nabto.simplepush.databinding.UserSettingsFragmentBinding
import com.nabto.simplepush.ui.user_settings.UserSettingsFragmentArgs
import com.nabto.simplepush.ui.view_model.DeviceConnectResult
import com.nabto.simplepush.ui.view_model.UserSettingsViewModel
import com.nabto.simplepush.ui.view_model.UserSettingsViewModelFactory
import kotlinx.coroutines.launch

class DeviceConnectingFragment : DeviceFragment() {

    private var binding: DeviceConnectingFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DeviceConnectingFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DeviceConnectingFragmentBinding.inflate(inflater, container, false)

        binding!!.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            var result = viewModel.connect();
            when (result) {
                is DeviceConnectResult.Unpaired -> {
                    val action = DeviceConnectingFragmentDirections.actionDeviceConnectingFragmentToDevicePairFragment(safeArgs.productId, safeArgs.deviceId);
                    findNavController().navigate(action);
                }
                is DeviceConnectResult.Connected -> {
                    val action = DeviceConnectingFragmentDirections.actionDeviceConnectingFragmentToDeviceSettingsFragment(safeArgs.productId, safeArgs.deviceId);
                    findNavController().navigate(action);
                }
                is DeviceConnectResult.Error -> {
                    val action = DeviceConnectingFragmentDirections.actionDeviceConnectingFragmentToDeviceDisconnectedFragment(safeArgs.productId, safeArgs.deviceId);
                    findNavController().navigate(action);
                }
            }
        }

        return binding!!.root
    }
}