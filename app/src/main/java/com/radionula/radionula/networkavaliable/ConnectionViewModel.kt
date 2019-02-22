package com.radionula.radionula.networkavaliable

import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.util.ConnectivityLiveData

class ConnectionViewModel(val context: Context) : ViewModel() {

    val connectionData = ConnectivityLiveData(
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    )
    val reconnectData = MutableLiveData<Boolean>()


    fun observeConnection(): LiveData<Boolean> = connectionData
    fun observeReconnection(): LiveData<Boolean> = reconnectData

    fun reconnect(needed: Boolean) {
        reconnectData.value = needed
    }
}