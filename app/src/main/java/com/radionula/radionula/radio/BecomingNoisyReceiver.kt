package com.radionula.radionula.radio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BecomingNoisyReceiver : BroadcastReceiver() {

    private val _noizy = MutableLiveData<Unit>()
    val getsNoize: LiveData<Unit> = _noizy

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            Log.d("headset", "Pause music")
            _noizy.postValue(Unit)
        }
    }
}