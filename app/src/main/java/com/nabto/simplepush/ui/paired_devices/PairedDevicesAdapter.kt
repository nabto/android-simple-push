package com.nabto.simplepush.ui.paired_devices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nabto.simplepush.databinding.PairedDeviceRowBinding
import com.nabto.simplepush.ui.view_model.PairedDevicesRowViewModel

class PairedDevicesAdapter : ListAdapter<PairedDevicesRowViewModel, PairedDevicesAdapter.PairedDeviceViewHolder>(PairedDevicesDiffCallback()) {

    class PairedDeviceViewHolder(val binding: PairedDeviceRowBinding ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : PairedDeviceViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = PairedDeviceRowBinding.inflate(layoutInflater)
        return PairedDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: PairedDeviceViewHolder, position: Int)
    {
        var pairedDevicesViewModel : PairedDevicesRowViewModel = getItem(position)
        viewHolder.binding.item = pairedDevicesViewModel

        viewHolder.binding.executePendingBindings()
    }
}

class PairedDevicesDiffCallback : DiffUtil.ItemCallback<PairedDevicesRowViewModel>() {
    override fun areItemsTheSame(oldItem: PairedDevicesRowViewModel, newItem: PairedDevicesRowViewModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PairedDevicesRowViewModel, newItem: PairedDevicesRowViewModel): Boolean {
        return oldItem == newItem // kotlin data classes implements equals for the values
    }
}
