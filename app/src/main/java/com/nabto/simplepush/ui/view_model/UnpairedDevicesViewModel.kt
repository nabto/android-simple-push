package com.nabto.simplepush.model

import android.content.Context
import android.widget.Toast
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class Empty() {}

class UnpairedDevicesViewModel @ViewModelInject constructor(private val nabtoClient: NabtoClient,
                                                            private val settings: Settings,
                                                            private val pairedDevicesRepository: PairedDevicesRepository,
                                                            @ApplicationContext val context : Context) : ViewModel() {
    val scanner = MdnsScanner(nabtoClient, "heatpump");
    val devices : LiveData<List<MdnsDevice>> = scanner.devices;

    val isPairing : LiveData<Boolean> = MutableLiveData<Boolean>(false);


    suspend fun pairDevice(productId : String, deviceId: String) {
        viewModelScope.launch {
            var connection: Connection = Connection(nabtoClient, settings)
            connection.connect(productId, deviceId);

            val me = IAM.getMe(connection.connection);
            when(me) {
                is Success<User> -> {
                    // the user is already paired update the database with the device and navigate to the device.
                    pairedDevicesRepository.upsertPairedDevice(productId, deviceId, me.data.Sct ?: "", me.data.Fingerprint?:"");
                    Toast.makeText(context, "Already paired", Toast.LENGTH_LONG).show()
                }
                is Result.Error -> {
                    Toast.makeText(context, me.exception.message, Toast.LENGTH_LONG).show()
                }
            }

            val result = IAM.openLocalPair(connection.connection, "foo")
            when (result) {
                is Success<Unit> -> {}
                is Result.Error -> {
                    val e = result;
                    Toast.makeText(context, e.exception.message, Toast.LENGTH_LONG ).show()
                }
            }
        }
    }
}