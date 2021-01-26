package com.nabto.simplepush.model

import android.content.Context
import android.widget.Toast
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.edge.*
import com.nabto.simplepush.edge.Result.Success
import com.nabto.simplepush.repository.PairedDevicesRepository

import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class Empty

class UnpairedDevicesViewModel @ViewModelInject constructor(private val nabtoClient: NabtoClient,
                                                            private val settings: Settings,
                                                            private val pairedDevicesRepository: PairedDevicesRepository,
                                                            @ApplicationContext val context : Context) : ViewModel() {
    val scanner = MdnsScanner(nabtoClient, "simplepush")
    val devices : LiveData<List<MdnsDevice>> = scanner.devices
    val showLoader : LiveData<Boolean> = Transformations.map(devices) { ds -> (ds.size == 0) }
}