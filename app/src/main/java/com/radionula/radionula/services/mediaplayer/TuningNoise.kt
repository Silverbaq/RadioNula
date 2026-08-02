package com.radionula.radionula.services.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import com.radionula.radionula.core.util.logError
import java.io.IOException

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

    /** For the test - the class is otherwise write-only. */
    val isPlaying: Boolean get() = player?.isPlaying == true

    fun start() {
        val noise = player ?: create() ?: return
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

    /**
     * From the Compose resource rather than res/raw, so iOS plays the same file
     * from the same place. It arrives as an asset, and openFd works because
     * AAPT never compresses .mp3 - a compressed asset would have no file
     * descriptor to seek in.
     */
    private fun create(): MediaPlayer? = try {
        context.assets.openFd(NOISE_ASSET).use { asset ->
            MediaPlayer().apply {
                setDataSource(asset.fileDescriptor, asset.startOffset, asset.length)
                isLooping = true
                prepare()
            }
        }
    } catch (e: IOException) {
        logError("TuningNoise", "Could not open $NOISE_ASSET", e)
        null
    }

    private companion object {
        /** Matches compose.resources.packageOfResClass in :shared. */
        const val NOISE_ASSET =
            "composeResources/com.radionula.radionula.resources/files/radionoise.mp3"
    }
}
