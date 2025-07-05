package com.radionula.services.mediaplayer

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.lifecycle.LiveData
import com.radionula.radionula.model.Constants
import com.radionula.radionula.radio.BecomingNoisyReceiver
import com.radionula.services.MediaPlayerService

class MediaplayerPresenter(
        private val context: Context,
        private val noizyReceiver: BecomingNoisyReceiver
) {
    private var mediaPlayerServiceIntent: Intent = Intent(context, MediaPlayerService::class.java)
    private val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private var isPlaying = false

    val getsNoizy: LiveData<Unit> = noizyReceiver.getsNoize

    fun tuneIn(channelUrl: String) {
        if (isPlaying) {
            mediaPlayerServiceIntent.action = Constants.ACTION.NEXT_ACTION
            mediaPlayerServiceIntent.putExtra("radioUrl", channelUrl)
        } else {
            mediaPlayerServiceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            mediaPlayerServiceIntent.putExtra("radioUrl", channelUrl)
        }

        context.startService(mediaPlayerServiceIntent)
        context.registerReceiver(noizyReceiver, intentFilter)
        isPlaying = true
    }

    fun pauseRadio() {
        mediaPlayerServiceIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        context.startService(mediaPlayerServiceIntent)
        // TODO: Fix this non-registered crash
        //context.unregisterReceiver(noizyReceiver)
        isPlaying = false
    }
}