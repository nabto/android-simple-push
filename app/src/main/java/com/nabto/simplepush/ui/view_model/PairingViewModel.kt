package com.nabto.simplepush.ui.view_model

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nabto.edge.client.NabtoClient
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.dao.PairedDevicesDao
import com.nabto.simplepush.edge.*

sealed class StartPairingResult {
    class Error(val error : Throwable) : StartPairingResult()
    class IsPAired() : StartPairingResult()
    class NeedsPairing() : StartPairingResult()
}

sealed class PairResult {
    class Error(val error : Throwable) : PairResult()
    class Paired() : PairResult()
    class UsernameExists() : PairResult()
}

enum class PairingViewState {
    NONE,
    LOADING,
    PAIRING_INPUT,
    PAIRED,
    ERROR
}

class PairingViewModel(val application: Application,
                       val pairedDevicesDao: PairedDevicesDao,
                       val settings: Settings,
                       val nabtoClient: NabtoClient,
                       val productId: String,
                       val deviceId: String) : ViewModel()
{

    val isPaired : MutableLiveData<Boolean> = MutableLiveData(false)
    val state : MutableLiveData<PairingViewState> = MutableLiveData(PairingViewState.NONE)
    val lastError : MutableLiveData<String> = MutableLiveData("")

    fun setError(error: Throwable) {
        setError(error.toString())
    }

    fun setError(error : String) {
        lastError.postValue(error)
        state.postValue(PairingViewState.ERROR)
    }

    suspend fun saveDevice(user : User, connection : com.nabto.edge.client.Connection) {
        pairedDevicesDao.upsertPairedDevice(PairedDeviceEntity(productId, deviceId, user.Sct!!, connection.deviceFingerprint, false))
    }

    suspend fun startPairing() {
        state.postValue(PairingViewState.LOADING)
        var result = doStartPairing()
        when (result) {
            is StartPairingResult.Error -> {
                setError(result.error)
            }
            is StartPairingResult.NeedsPairing -> {
                state.postValue(PairingViewState.PAIRING_INPUT)
            }
            is StartPairingResult.IsPAired -> {
                state.postValue(PairingViewState.PAIRED)
            }
        }
    }

    // make a connection
    suspend fun doStartPairing() : StartPairingResult {
        var connection = Connection(nabtoClient, settings, productId, deviceId)
        var result = connection.connect()
        when (result) {
            is ConnectResult.Error -> {
                return StartPairingResult.Error(result.error)
            }
        }
        var me = IAM.getMe(connection.connection)
        when (me) {
            is GetMeResult.Error -> {
                return StartPairingResult.Error(me.error);
            }
            is GetMeResult.NotPaired -> {
                return StartPairingResult.NeedsPairing()
            }

            is GetMeResult.Success -> {
                var user = me.user
                saveDevice(user, connection.connection);
                return StartPairingResult.IsPAired()
            }
        }
    }

    suspend fun pair(username : String) {
        state.postValue(PairingViewState.LOADING)
        var r = doPair(username)
        when (r) {
            is PairResult.Error -> {
                setError(r.error)
            }
            is PairResult.Paired -> {
                state.postValue(PairingViewState.PAIRED)
            }
            is PairResult.UsernameExists -> {
                setError("Username already exists, try another one.")
            }
        }
    }

    suspend fun doPair(username : String) : PairResult {
        var connection = Connection(nabtoClient, settings, productId, deviceId)
        var result = connection.connect()
        when (result) {
            is ConnectResult.Error -> {
                return PairResult.Error(result.error)
            }
        }
        var r = IAM.openLocalPair(connection.connection, username)
        when (r) {
            is OpenLocalPairResult.Error -> {
                return PairResult.Error(r.error)
            }
            is OpenLocalPairResult.UsernameExists -> {
                return PairResult.UsernameExists()
            }
        }
        var me = IAM.getMe(connection.connection)
        when (me) {
            is GetMeResult.Error -> {
                return PairResult.Error(me.error);
            }
            is GetMeResult.NotPaired -> {
                return PairResult.Error(Exception("Not paired, try again."))
            }
            is GetMeResult.Success -> {
                var user = me.user;
                saveDevice(user, connection.connection);
                return PairResult.Paired()
            }
        }
    }

}