package com.nabto.simplepush.ui.view_model

import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch

// this viewmodel is created in the DeviceSettingsFragment as a viewmodel for a notification category

class NotificationCategoryViewModel(val lifecycleScope : LifecycleCoroutineScope, val deviceViewModel : DeviceViewModel, val categoryName : String) {
    val isChecked : MutableLiveData<Boolean> = deviceViewModel.getNotificationCategoryState(categoryName)

    fun notificationCategoriesChanged(view : View, state : Boolean) {
        lifecycleScope.launch {
            deviceViewModel.notificationCategoriesChanged(categoryName, state)
        }
    }

    fun equals(other : NotificationCategoryViewModel) : Boolean{
        return other.categoryName == categoryName
    }
}