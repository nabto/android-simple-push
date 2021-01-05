package com.nabto.simplepush.ui.unpaired_devices

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nabto.simplepush.R
import com.nabto.simplepush.databinding.UnpairedDeviceRowBinding
import com.nabto.simplepush.databinding.UnpairedDevicesFragmentBinding
import com.nabto.simplepush.model.UnpairedDevice

class UnpairedDevicesAdapter : ListAdapter<UnpairedDevice, UnpairedDevicesAdapter.UnpairedDeviceViewHolder>(UnpairedDevicesDiffCallback()) {

    class UnpairedDeviceViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) =
            UnpairedDeviceViewHolder(
                    UnpairedDeviceRowBinding
                            .inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                            .root
            )
    override fun onBindViewHolder(viewHolder: UnpairedDeviceViewHolder, position: Int)
    {
        UnpairedDeviceRowBinding.bind(viewHolder.itemView).apply{
            var unpairedDevice : UnpairedDevice = getItem(position);
            var foo = itemCount
            unpairedProductId.text = unpairedDevice.productId;
            unpairedDeviceId.text = unpairedDevice.deviceId;
        }
    }
}


class UnpairedDevicesDiffCallback : DiffUtil.ItemCallback<UnpairedDevice>() {
    override fun areItemsTheSame(oldItem: UnpairedDevice, newItem: UnpairedDevice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: UnpairedDevice, newItem: UnpairedDevice): Boolean {
        return oldItem.productId == newItem.productId &&
                oldItem.deviceId == newItem.deviceId
    }
}
