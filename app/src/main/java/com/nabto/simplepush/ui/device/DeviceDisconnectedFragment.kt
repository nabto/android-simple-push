package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.databinding.DeviceDisconnectedFragmentBinding

class DeviceDisconnectedFragment : DeviceFragment() {
    private var binding: DeviceDisconnectedFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DeviceDisconnectedFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DeviceDisconnectedFragmentBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = viewLifecycleOwner
        binding!!.viewModel = viewModel

        return binding!!.root
    }
}