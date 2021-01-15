package com.nabto.simplepush

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.nabto.simplepush.ui.paired_devices.PairedDevicesFragment
import com.nabto.simplepush.ui.unpaired_devices.UnpairedDevicesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
//        if (savedInstanceState == null) {
//            supportFragmentManager.commit {
//                setReorderingAllowed(true)
//                //add<UnpairedDevicesFragment>(R.id.fragment_container_view)
//                add<PairedDevicesFragment>(R.id.fragment_container_view)
//            }
//        }
    }
}