package com.nabto.simplepush.ui.paired_devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.nabto.simplepush.R
import com.nabto.simplepush.databinding.PairedDevicesFragmentBinding
import com.nabto.simplepush.ui.view_model.PairedDevicesRowViewModel
import com.nabto.simplepush.ui.view_model.PairedDevicesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PairedDevicesFragment : Fragment() {
    private val viewModel: PairedDevicesViewModel by viewModels()

    private var pairedDevicesFragmentBinding: PairedDevicesFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = PairedDevicesFragmentBinding.inflate(inflater, container, false)
        pairedDevicesFragmentBinding = binding

        val pairedDevicesAdapter = PairedDevicesAdapter {
                productId : String, deviceId : String ->
            val action = PairedDevicesFragmentDirections.actionPairedDevicesFragmentToUserSettingsFragment(productId,deviceId)
            findNavController().navigate(action)
        }
        binding.pairedDevicesList.adapter = pairedDevicesAdapter

        binding.pairNewDevice.setOnClickListener { view -> view.findNavController().navigate(R.id.action_pairedDevicesFragment_to_unpairedDevicesFragment ) }

        viewModel.pairedDevices.observe(viewLifecycleOwner, Observer {
            l -> pairedDevicesAdapter.submitList(l.map { PairedDevicesRowViewModel(it.productId, it.deviceId, it.updatedFcmToken) }) })

        return binding.root
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        pairedDevicesFragmentBinding = null
        super.onDestroyView()
    }
}