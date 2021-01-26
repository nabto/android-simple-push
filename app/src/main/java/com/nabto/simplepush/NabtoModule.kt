package com.nabto.simplepush

import android.content.Context
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.edge.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object NabtoModule {
    @Singleton
    @Provides
    fun provideNabtoClient(@ApplicationContext context : Context) : NabtoClient {
        return NabtoClient.create(context)
    }

    @Singleton
    @Provides
    fun provideSettings(@ApplicationContext context : Context, nabtoClient : NabtoClient) : Settings {
        return Settings(nabtoClient, context)
    }
}
