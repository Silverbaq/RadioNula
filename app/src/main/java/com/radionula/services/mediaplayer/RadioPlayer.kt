package com.radionula.services.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.radionula.radionula.R
import androidx.core.net.toUri

class RadioPlayer(private val context: Context) {

    private val mpNoize: MediaPlayer = MediaPlayer.create(context, R.raw.radionoise)
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    init {
        mpNoize.isLooping = true
    }

    private fun prepareExoPlayerFromURL(uri: Uri) {
        val mediaItem: MediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun tuneIn(radioUrl: String) {
        startRadioNoize()

        // ExoPlayer
        prepareExoPlayerFromURL(radioUrl.toUri())
        playRadio()
    }

    fun playRadio() {
        exoPlayer.playWhenReady = true
        mpNoize.stop()
    }

    fun pauseRadio() {
        exoPlayer.playWhenReady = false
    }

    fun startRadioNoize() {
        mpNoize.start()
    }

    fun stopRadioNoize() {
        mpNoize.stop()
    }

    companion object {
        const val TAG = "RadioPlayer"
    }
}