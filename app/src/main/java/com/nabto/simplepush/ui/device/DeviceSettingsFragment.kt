package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.NavGraphDirections
import com.nabto.simplepush.databinding.DeviceSettingsFragmentBinding
import com.nabto.simplepush.ui.view_model.DeviceConnectResult
import kotlinx.coroutines.launch

class DeviceSettingsFragment : DeviceFragment() {
    private var binding: DeviceSettingsFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DeviceConnectingFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DeviceSettingsFragmentBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = viewLifecycleOwner

        binding?.viewModel = viewModel

        //findNavController().
        viewLifecycleOwner.lifecycleScope.launch {
            var result = viewModel.connect();
            when (result) {
                is DeviceConnectResult.Unpaired -> {
                    val action = DeviceConnectingFragmentDirections.actionDeviceConnectingFragmentToDevicePairFragment(safeArgs.productId, safeArgs.deviceId);
                    findNavController().navigate(action);
                }
                is DeviceConnectResult.Connected -> {
                    // show page
                }
                is DeviceConnectResult.Error -> {
                    val action = NavGraphDirections.actionGlobalDeviceDisconnectedFragment(safeArgs.productId,safeArgs.deviceId)
                    findNavController().navigate(action);
                }
            }
        }

        return binding!!.root
    }
}
