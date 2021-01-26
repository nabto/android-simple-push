package com.nabto.simplepush

import android.app.Application
import android.content.Context

import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.Settings
import com.nabto.simplepush.repository.PairedDevicesRepository
import com.nabto.simplepush.repository.PairedDevicesRepositoryImpl

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun providePairedDevicesRepository(pairedDevicesDao :PairedDevicesDao,
                                       settings: Settings,
                                       application : Application,
                                       nabtoClient : NabtoClient

                                       ) : PairedDevicesRepository {
        return PairedDevicesRepositoryImpl(pairedDevicesDao, settings, nabtoClient, application)
    }
}
