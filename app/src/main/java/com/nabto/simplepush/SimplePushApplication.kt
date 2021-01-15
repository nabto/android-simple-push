package com.nabto.simplepush

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SimplePushApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}