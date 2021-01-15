package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.databinding.DeviceConnectingFragmentBinding
import com.nabto.simplepush.databinding.DevicePairFragmentBinding

class DevicePairFragment : DeviceFragment() {

    private var binding: DevicePairFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DeviceConnectingFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DevicePairFragmentBinding.inflate(inflater, container, false)

        binding!!.viewModel = viewModel

        viewModel.pair();

        return binding!!.root
    }
}