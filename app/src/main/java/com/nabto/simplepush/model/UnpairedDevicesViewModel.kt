package com.nabto.simplepush.model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.edge.MdnsDevice
import com.nabto.simplepush.edge.MdnsScanner
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class UnpairedDevicesViewModel @Inject constructor(@ActivityContext private val context: Context) : ViewModel() {
    private val client = com.nabto.edge.client.NabtoClient.create(context);
    val scanner = MdnsScanner(client, "simple_push");
    val devices : LiveData<List<MdnsDevice>> = scanner.devices;
}