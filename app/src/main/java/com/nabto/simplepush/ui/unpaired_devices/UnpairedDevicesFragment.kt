package com.nabto.simplepush.ui.unpaired_devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.nabto.simplepush.databinding.UnpairedDevicesFragmentBinding
import com.nabto.simplepush.model.UnpairedDevicesViewModel

class UnpairedDevicesFragment : Fragment() {

    companion object {
        fun newInstance() = UnpairedDevicesFragment()
    }

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private var unpairedDevicesFragmentBinding: UnpairedDevicesFragmentBinding? = null

    private val viewModel: UnpairedDevicesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = UnpairedDevicesFragmentBinding.inflate(inflater, container, false)
        unpairedDevicesFragmentBinding = binding

        viewModel.devices.observe(viewLifecycleOwner, Observer {  })

        val unpairedDevicesAdapter = UnpairedDevicesAdapter()
        binding.unpairedDevicesList.adapter = unpairedDevicesAdapter

        viewModel.devices.observe(viewLifecycleOwner, Observer { l -> unpairedDevicesAdapter.submitList(l) })
        return binding.root
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        unpairedDevicesFragmentBinding = null
        super.onDestroyView()
    }
}