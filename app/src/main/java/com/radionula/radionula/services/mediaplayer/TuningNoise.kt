package com.radionula.radionula.services.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import com.radionula.radionula.R

/**
 * The radio static that plays while a stream buffers.
 *
 * Created on first use so opening the app does not allocate a MediaPlayer, and
 * paused rather than stopped between bursts - MediaPlayer cannot go from
 * Stopped straight back to Started, which is what used to throw
 * IllegalStateException on the second channel skip.
 */
class TuningNoise(private val context: Context) {

    private var player: MediaPlayer? = null

    fun start() {
        val noise = player ?: MediaPlayer.create(context, R.raw.radionoise)?.apply {
            isLooping = true
        } ?: return
        player = noise
        if (!noise.isPlaying) noise.start()
    }

    fun stop() {
        player?.takeIf { it.isPlaying }?.apply {
            pause()
            seekTo(0)
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}
