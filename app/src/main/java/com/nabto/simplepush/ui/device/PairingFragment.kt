package com.nabto.simplepush.ui.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.R
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.databinding.PairingFragmentBinding
import com.nabto.simplepush.edge.Settings
import com.nabto.simplepush.ui.view_model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Device pairing
 *
 * The device pairing fragment has a button, Pair Now. Once this button is pressed one of the following cases is met:
 *   1. The client is not paired with the device and the device does not know the client
 *   2. The client is not paired with the device but the device has a registered user for the client
 *   3. The client has a record for the device but the device does not know the client.
 *   4. The client has a record for the device and the device knows the client.
 *
 * First we determine what case we are in.
 * 1. Connect to the device and call getMe.
 *     var connect = Success || Error
 *     var getMe = User || Not Found || Error
 *     var deviceRecord = DeviceRecord || Not Found
 * If connect or getMe is error -> fail.
 * if getMe == NotFound AND  deviceRecord == Not Found -> Case 1.
 * if getMe == User AND deviceRecord == DeviceRecord -> case 4
 * if getMe == User AND deviceRecord == Not Found -> case 2
 * if getMe == NotFound AND deviceRecord == DeviceRecord -> case 3
 *
 * Case 1.
 *   Do a Local Open Pairing, save the device information
 *
 * Case 2.
 *   Update the client with the device information.
 *
 * Case 3.
 *   Do a local Open Pairing, upsert the device information.
 *
 * Case 4.
 *   Do nothing.
 */

@AndroidEntryPoint
class PairingFragment() : Fragment() {

    @Inject lateinit var pairedDevicesDao: PairedDevicesDao

    @Inject lateinit var settings: Settings

    @Inject lateinit var nabtoClient: NabtoClient

    lateinit var binding: PairingFragmentBinding

    lateinit var pairingViewModelFactory: PairingViewModelFactory;
    lateinit var pairingViewModel: PairingViewModel;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val safeArgs : PairingFragmentArgs by navArgs()

        pairingViewModelFactory = PairingViewModelFactory(
            requireActivity().application,
            pairedDevicesDao,
            settings,
            nabtoClient,
            safeArgs.productId,
            safeArgs.deviceId
        )
        pairingViewModel =
            ViewModelProvider(requireActivity(), pairingViewModelFactory).get(PairingViewModel::class.java)


        binding = PairingFragmentBinding.inflate(inflater, container, false)

        binding.viewModel = pairingViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.goToSettings.setOnClickListener { view : View ->
            var action = PairingFragmentDirections.actionDevicePairFragmentToDeviceSettingsFragment(safeArgs.productId, safeArgs.deviceId)
            findNavController().navigate(action)
        }

        binding!!.pairButton.setOnClickListener { view: View ->
            viewLifecycleOwner.lifecycleScope.launch {
                pairingViewModel.pair(binding!!.username.text.toString())
            }
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val safeArgs: PairingFragmentArgs by navArgs()
        lifecycleScope.launch {
            pairingViewModel.startPairing();
        }
    }
}