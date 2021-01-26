package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.databinding.DeviceSettingsFragmentBinding
import com.nabto.simplepush.ui.paired_devices.PairedDevicesAdapter
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragmentDirections
import com.nabto.simplepush.ui.view_model.DeviceConnectResult
import com.nabto.simplepush.ui.view_model.NotificationCategoriesChangedResult
import com.nabto.simplepush.ui.view_model.NotificationCategoryViewModel
import com.nabto.simplepush.ui.view_model.PairedDevicesRowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceSettingsFragment : DeviceFragment() {
    private lateinit var binding: DeviceSettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DeviceSettingsFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DeviceSettingsFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel
        binding.fragment = this


        val notificationCategoriesAdapter = NotificationCategoriesAdapter()
        binding.notificationCategoriesList.adapter = notificationCategoriesAdapter

        viewModel.notificationCategories.observe(viewLifecycleOwner, Observer {
                l -> notificationCategoriesAdapter.submitList(l.map { NotificationCategoryViewModel(lifecycleScope, viewModel, it) })
        })

        binding.notPaired.setOnClickListener {
            var action = DeviceSettingsFragmentDirections.actionDeviceSettingsFragmentToDevicePairFragment(safeArgs.productId, safeArgs.deviceId)
            findNavController().navigate(action)
        }

        binding.sendTestNotification.setOnClickListener { view ->
            lifecycleScope.launch {
                viewModel.sendTestNotification()
            }
        }

        binding.reconnect.setOnClickListener { view ->
            lifecycleScope.launch {
                viewModel.start()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.start()
        }
    }
}
