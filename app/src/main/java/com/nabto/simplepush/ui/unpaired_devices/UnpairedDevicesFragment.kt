package com.nabto.simplepush.ui.unpaired_devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.nabto.simplepush.databinding.UnpairedDevicesFragmentBinding
import com.nabto.simplepush.ui.view_model.UnpairedDevicesRowViewModel
import com.nabto.simplepush.model.UnpairedDevicesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class UnpairedDevicesFragment : Fragment() {

    private val viewModel: UnpairedDevicesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = UnpairedDevicesFragmentBinding.inflate(inflater, container, false)

        val unpairedDevicesAdapter = UnpairedDevicesAdapter()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.unpairedDevicesList.adapter = unpairedDevicesAdapter

        viewModel.devices.observe(viewLifecycleOwner, Observer { l -> unpairedDevicesAdapter.submitList(l.map { UnpairedDevicesRowViewModel(it.productId,it.deviceId) }) })
        return binding.root
    }

}