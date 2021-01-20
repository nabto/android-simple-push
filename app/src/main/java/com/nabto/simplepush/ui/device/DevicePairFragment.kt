package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.databinding.DevicePairFragmentBinding
import com.nabto.simplepush.ui.view_model.DevicePairResult
import kotlinx.coroutines.launch

class DevicePairFragment : DeviceFragment() {

    private var binding: DevicePairFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DevicePairFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DevicePairFragmentBinding.inflate(inflater, container, false)

        binding!!.viewModel = viewModel
        binding?.lifecycleOwner = viewLifecycleOwner

        binding!!.pairButton.setOnClickListener { view : View ->
            viewLifecycleOwner.lifecycleScope.launch {
                var devicePairResult = viewModel.pair(binding!!.username.text.toString())
                when (devicePairResult) {
                    is DevicePairResult.Paired -> {
                        val action = DevicePairFragmentDirections.actionDevicePairFragmentToDeviceSettingsFragment(safeArgs.productId, safeArgs.deviceId);
                        findNavController().navigate(action);
                    }
                    is DevicePairResult.Error -> {
                        val action = DevicePairFragmentDirections.actionDevicePairFragmentToDeviceDisconnectedFragment(safeArgs.productId, safeArgs.deviceId);
                        findNavController().navigate(action);
                    }
                }

            }
        }
        return binding!!.root
    }
}