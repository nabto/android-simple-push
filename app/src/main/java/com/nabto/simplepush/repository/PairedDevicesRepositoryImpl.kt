package com.nabto.simplepush.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PairedDevicesRepositoryImpl constructor(private val pairedDevicesDao : PairedDevicesDao,
                                                      val settings :Settings,
                                                      val nabtoClient: NabtoClient,
                                                      val application: Application
) :PairedDevicesRepository {
    override val pairedDevices: LiveData<List<PairedDevicesRow>> = Transformations.map(pairedDevicesDao.listPairedDevides()) {
        it.map {
            PairedDevicesRow(it.productId, it.deviceId, it.updatedFcmToken)
        }
    }
    override suspend fun upsertPairedDevice(productId: String, deviceId: String, sct: String, fingerprint: String) : Unit {
        pairedDevicesDao.upsertPairedDevice(PairedDeviceEntity(productId,deviceId,sct,fingerprint, false))
    }
}