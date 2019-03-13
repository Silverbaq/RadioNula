package com.radionula.services.mediaplayer

import android.content.Context
import android.content.Intent
import com.radionula.radionula.model.Constants
import com.radionula.services.MediaPlayerService

class MediaplayerPresenter(private val context: Context) {
    var mediaPlayerServiceIntent: Intent = Intent(context, MediaPlayerService::class.java)
    private var isPlaying = false

    fun tuneIn(channelUrl: String) {
        if (isPlaying) {
            mediaPlayerServiceIntent.action = Constants.ACTION.NEXT_ACTION
            mediaPlayerServiceIntent.putExtra("radioUrl", channelUrl)
        } else {
            mediaPlayerServiceIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            mediaPlayerServiceIntent.putExtra("radioUrl", channelUrl)
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
}