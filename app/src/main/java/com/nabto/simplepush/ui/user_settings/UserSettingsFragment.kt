package com.nabto.simplepush.ui.user_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.databinding.UserSettingsFragmentBinding
import com.nabto.simplepush.edge.Settings
import com.nabto.simplepush.ui.view_model.UserSettingsViewModel
import com.nabto.simplepush.ui.view_model.UserSettingsViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class UserSettingsFragment : Fragment() {

    private var binding: UserSettingsFragmentBinding? = null

    private lateinit var viewModelFactory : UserSettingsViewModelFactory
    private lateinit var viewModel: UserSettingsViewModel

    @Inject lateinit var nabtoClient: NabtoClient
    @Inject lateinit var settings : Settings

    fun refresh() {
        binding!!.swiperefresh.isRefreshing = true;
        //viewModel.refreshUser();
        binding!!.swiperefresh.isRefreshing = false;
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val safeArgs: UserSettingsFragmentArgs by navArgs()

        viewModelFactory = UserSettingsViewModelFactory(requireActivity().application, settings, nabtoClient, safeArgs.productId, safeArgs.deviceId)
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserSettingsViewModel::class.java)

        binding = UserSettingsFragmentBinding.inflate(inflater, container, false)


        binding!!.viewModel = viewModel
        binding!!.swiperefresh.setOnRefreshListener {
            refresh();
        }

        return binding!!.root
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        binding = null
        super.onDestroyView()
    }
}