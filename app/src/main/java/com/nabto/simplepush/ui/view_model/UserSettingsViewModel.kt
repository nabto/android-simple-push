package com.nabto.simplepush.ui.view_model

import android.app.Application
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.edge.Result.*
import com.nabto.simplepush.edge.Settings
import com.nabto.simplepush.edge.User
import com.nabto.simplepush.model.Empty
import com.nabto.simplepush.model.GetUserResult
import com.nabto.simplepush.model.PairedDeviceModel
import com.nabto.simplepush.model.UpdateFcmTokenResult
import com.nabto.simplepush.repository.PairedDevice
import com.nabto.simplepush.repository.PairedDeviceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserSettingsViewModel constructor(val application : Application,
                                        val settings : Settings,
                                        val nabtoClient: NabtoClient,
                                        val productId : String,
                                        val deviceId : String) : ViewModel() {
    var username : String = "foo"
    var loadingVisibile : LiveData<Boolean> = MutableLiveData<Boolean>(false)
    var user : MutableLiveData<User> = MutableLiveData<User>()

    var pairedDevice : PairedDevice = PairedDeviceImpl(application, settings, nabtoClient, productId, deviceId, "", "")

    lateinit var pairedDeviceModel : PairedDeviceModel

    fun refreshUser() {
        viewModelScope.launch {
            var userResult = pairedDeviceModel.getUser()
            when(userResult) {
                is GetUserResult.NotConnected -> {
                    // handle cannot connect to device
                }
                is GetUserResult.Error -> {
                    // handle error
                }
                is GetUserResult.Success -> {
                    user.postValue(userResult.user)
                }
            }
            if (!pairedDeviceModel.hasUpdatedFcmToken()) {
                var updateFcmTokenResult = pairedDeviceModel.updateFcmToken()
                when(updateFcmTokenResult) {
                    is UpdateFcmTokenResult.Error -> {
                        // handle error
                    }
                    is UpdateFcmTokenResult.Success -> {

                    }
                }
            }
            // check fcm token status, if not uptodate uplaod fcm token.
        }
    }

    fun setFcmToken(view : View) {
        viewModelScope.launch {
            var r = pairedDevice.updateFcmToken();
            when (r) {
                is Success<Empty> -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(view.context, "Set token success", Toast.LENGTH_LONG).show()
                    }
                }
                is Error -> {
                    Log.d(TAG, r.exception.toString())
                    withContext(Dispatchers.Main) {

                        Toast.makeText(view.context, "Set token failed", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
            }
        }
    }

    companion object {

        private const val TAG = "UserSettingsViewModel"
    }

}