package com.nabto.simplepush.ui.unpaired_devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nabto.simplepush.databinding.UnpairedDevicesFragmentBinding
import com.nabto.simplepush.model.UnpairedDevice
import com.nabto.simplepush.ui.main.MainFragment

class UnpairedDevicesFragment : Fragment() {

    companion object {
        fun newInstance() = UnpairedDevicesFragment()
    }

    // Scoped to the lifecycle of the fragment's view (between onCreateView and onDestroyView)
    private var unpairedDevicesFragmentBinding: UnpairedDevicesFragmentBinding? = null

    private var testData : ArrayList<UnpairedDevice> = ArrayList<UnpairedDevice>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = UnpairedDevicesFragmentBinding.inflate(inflater, container, false)
        unpairedDevicesFragmentBinding = binding

        testData.add(UnpairedDevice("foo", "bar"))
        testData.add(UnpairedDevice("foo", "bar2"))
        testData.add(UnpairedDevice("foo2", "bar2"))
        testData.add(UnpairedDevice("foo", "bar"))


        val unpairedDevicesAdapter = UnpairedDevicesAdapter()
        binding.unpairedDevicesList.adapter = unpairedDevicesAdapter
        unpairedDevicesAdapter.submitList(testData)
        return binding.root
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        unpairedDevicesFragmentBinding = null
        super.onDestroyView()
    }
}