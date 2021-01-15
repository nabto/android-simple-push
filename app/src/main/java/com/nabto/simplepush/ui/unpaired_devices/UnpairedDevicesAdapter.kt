package com.nabto.simplepush.ui.unpaired_devices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nabto.simplepush.databinding.UnpairedDeviceRowBinding
import com.nabto.simplepush.ui.view_model.UnpairedDevicesRowViewModel

class UnpairedDevicesAdapter() : ListAdapter<UnpairedDevicesRowViewModel, UnpairedDevicesAdapter.UnpairedDeviceViewHolder>(UnpairedDevicesDiffCallback()) {

    class UnpairedDeviceViewHolder(val binding : UnpairedDeviceRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : UnpairedDevicesAdapter.UnpairedDeviceViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = UnpairedDeviceRowBinding.inflate(layoutInflater)
        return UnpairedDevicesAdapter.UnpairedDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: UnpairedDeviceViewHolder, position: Int)
    {
        var unpairedDevicesViewModel : UnpairedDevicesRowViewModel = getItem(position);
        viewHolder.binding.item = unpairedDevicesViewModel

        viewHolder.binding.executePendingBindings()
    }
}


class UnpairedDevicesDiffCallback : DiffUtil.ItemCallback<UnpairedDevicesRowViewModel>() {
    override fun areItemsTheSame(oldItem: UnpairedDevicesRowViewModel, newItem: UnpairedDevicesRowViewModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UnpairedDevicesRowViewModel, newItem: UnpairedDevicesRowViewModel): Boolean {
        return oldItem.productId == newItem.productId &&
                oldItem.deviceId == newItem.deviceId
    }
}
