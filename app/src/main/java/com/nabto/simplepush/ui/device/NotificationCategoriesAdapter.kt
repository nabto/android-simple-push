package com.nabto.simplepush.ui.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nabto.simplepush.databinding.NotificationCategoryRowBinding
import com.nabto.simplepush.databinding.PairedDeviceRowBinding
import com.nabto.simplepush.ui.view_model.NotificationCategoryViewModel
import com.nabto.simplepush.ui.view_model.PairedDevicesRowViewModel

class NotificationCategoriesAdapter() : ListAdapter<NotificationCategoryViewModel, NotificationCategoriesAdapter.NotificationCategoryViewHolder>(NotificationCategoriesDiffCallback()) {

    class NotificationCategoryViewHolder(val binding: NotificationCategoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : NotificationCategoryViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = NotificationCategoryRowBinding.inflate(layoutInflater)
        return NotificationCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: NotificationCategoryViewHolder, position: Int)
    {
        var notificationCategoryViewModel : NotificationCategoryViewModel = getItem(position);
        viewHolder.binding.viewModel = notificationCategoryViewModel

        viewHolder.binding.executePendingBindings()
    }
}

class NotificationCategoriesDiffCallback : DiffUtil.ItemCallback<NotificationCategoryViewModel>() {
    override fun areItemsTheSame(oldItem: NotificationCategoryViewModel, newItem: NotificationCategoryViewModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: NotificationCategoryViewModel, newItem: NotificationCategoryViewModel): Boolean {
        return oldItem == newItem // kotlin data classes implements equals for the values
    }
}
