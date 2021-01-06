package com.nabto.simplepush.edge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nabto.edge.client.MdnsResult

class MdnsScanner(var nabtoClient : com.nabto.edge.client.NabtoClient, var subtype : String) {

    var devices = MutableLiveData<List<MdnsDevice>>(ArrayList<MdnsDevice>())
    var devicesInternal = HashMap<String, MdnsDevice>();

    init {
        val resultListener = { result : MdnsResult ->
            if (result.action == MdnsResult.Action.ADD || result.action == MdnsResult.Action.UPDATE) {
                devicesInternal[result.serviceInstanceName] = MdnsDevice(result.productId, result.deviceId);
            } else if (result.action == MdnsResult.Action.REMOVE) {
                devicesInternal.remove(result.serviceInstanceName);
            }
            devices.postValue(ArrayList<MdnsDevice>(devicesInternal.values));
        }
        nabtoClient.addMdnsResultListener(resultListener, subtype);
    }
}