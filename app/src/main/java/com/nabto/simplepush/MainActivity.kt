package com.nabto.simplepush

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nabto.simplepush.ui.main.MainFragment
import com.nabto.simplepush.ui.unpaired_devices.UnpairedDevicesFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, UnpairedDevicesFragment.newInstance())
                    .commitNow()
        }
    }
}