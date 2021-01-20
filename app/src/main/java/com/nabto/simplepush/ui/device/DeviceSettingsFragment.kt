package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nabto.simplepush.NavGraphDirections
import com.nabto.simplepush.databinding.DeviceSettingsFragmentBinding
import com.nabto.simplepush.ui.view_model.DeviceConnectResult
import com.nabto.simplepush.ui.view_model.NotificationCategoriesChangedResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceSettingsFragment : DeviceFragment() {
    private var binding: DeviceSettingsFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: DeviceSettingsFragmentArgs by navArgs()
        initViewModel(safeArgs.productId, safeArgs.deviceId)

        binding = DeviceSettingsFragmentBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = viewLifecycleOwner

        binding?.viewModel = viewModel
        binding?.fragment = this

        var navController : NavController = findNavController()

        //findNavController().
        lifecycleScope.launch {
                var result = viewModel.connect();

                when (result) {
                    is DeviceConnectResult.Unpaired -> {
                        val action =
                            DeviceSettingsFragmentDirections.actionDeviceSettingsFragmentToDevicePairFragment(
                                safeArgs.productId,
                                safeArgs.deviceId
                            );
                        findNavController().navigate(action);
                    }
                    is DeviceConnectResult.Connected -> {
                        // show page
                    }
                    is DeviceConnectResult.Error -> {
                        val action = NavGraphDirections.actionGlobalDeviceDisconnectedFragment(
                            safeArgs.productId,
                            safeArgs.deviceId
                        )
                        navController.navigate(action);
                    }
                }
            }
        return binding!!.root
    }

    fun notificationCategoriesChanged(category : String, state : Boolean) {
        lifecycleScope.launch {
            var  result : com.nabto.simplepush.ui.view_model.NotificationCategoriesChangedResult  = viewModel.notificationCategoriesChanged(category, state);
            when (result) {
                is NotificationCategoriesChangedResult.Success -> {}

                is NotificationCategoriesChangedResult.Error -> {
                    val safeArgs: DeviceSettingsFragmentArgs by navArgs()
                    viewModel.updateLastError(result.error)
                    var action = DeviceSettingsFragmentDirections.actionGlobalDeviceDisconnectedFragment(safeArgs.productId, safeArgs.deviceId)
                    findNavController().navigate(action)
                }
            }
        }
    }
}
