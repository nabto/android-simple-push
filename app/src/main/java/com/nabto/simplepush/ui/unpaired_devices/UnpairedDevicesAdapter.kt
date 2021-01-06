package com.nabto.simplepush.ui.unpaired_devices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nabto.simplepush.databinding.UnpairedDeviceRowBinding
import com.nabto.simplepush.edge.MdnsDevice

class UnpairedDevicesAdapter : ListAdapter<MdnsDevice, UnpairedDevicesAdapter.UnpairedDeviceViewHolder>(UnpairedDevicesDiffCallback()) {

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
            var mdnsDevice : MdnsDevice = getItem(position);
            var foo = itemCount
            unpairedProductId.text = mdnsDevice.productId;
            unpairedDeviceId.text = mdnsDevice.deviceId;
        }
    }
}


class UnpairedDevicesDiffCallback : DiffUtil.ItemCallback<MdnsDevice>() {
    override fun areItemsTheSame(oldItem: MdnsDevice, newItem: MdnsDevice): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MdnsDevice, newItem: MdnsDevice): Boolean {
        return oldItem.productId == newItem.productId &&
                oldItem.deviceId == newItem.deviceId
    }
}
