package com.radionula.services.mediaplayer

import android.content.Context
import android.content.Intent
import com.radionula.radionula.model.Constants
import com.radionula.services.MediaPlayerService

class MediaplayerPresenter(private val context: Context) {
    var mediaPlayerServiceIntent: Intent = Intent(context, MediaPlayerService::class.java)
    private var currentChannel = CHANNEL1
    private var isPlaying = false

    fun tuneIn() {
        if (isPlaying) {
            when (currentChannel) {
                CHANNEL1 -> currentChannel = CHANNEL2
                CHANNEL2 -> currentChannel = CHANNEL3
                CHANNEL3 -> currentChannel = CHANNEL1
            }
            mediaPlayerServiceIntent.action = Constants.ACTION.NEXT_ACTION
            mediaPlayerServiceIntent.putExtra("radioUrl", currentChannel)
        } else {
            mediaPlayerServiceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            mediaPlayerServiceIntent.putExtra("radioUrl", currentChannel)
        }

        context.startService(mediaPlayerServiceIntent)
        isPlaying = true
    }

    fun pauseRadio() {
        mediaPlayerServiceIntent.action = Constants.ACTION.STOPFOREGROUND_ACTION
        context.startService(mediaPlayerServiceIntent)
        isPlaying = false
    }

    fun isPlaying(): Boolean = isPlaying

    private fun changeChannel(channel: String){

    }

    companion object {
        const val CHANNEL1 = "http://streaming.radionula.com:8800/classics"
        const val CHANNEL2 = "http://streaming.radionula.com:8800/channel2"
        const val CHANNEL3 = "http://streaming.radionula.com:8800/lounge"
    }
}