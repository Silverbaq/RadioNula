package com.radionula.radionula.services.mediaplayer

import com.radionula.radionula.core.util.logError
import com.radionula.radionula.resources.Res
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * The same radio static Android plays while a stream buffers, from the same
 * Compose resource.
 *
 * AVAudioPlayer rather than a second AVPlayer: it takes the bytes directly, and
 * it does not touch the audio session the radio stream is using. Paused rather
 * than stopped between bursts, which is also what the Android one does.
 */
@OptIn(ExperimentalForeignApi::class)
class IosTuningNoise {

    private var player: AVAudioPlayer? = null

    /** Loads the file on first call, so a cold start allocates nothing. */
    suspend fun start() {
        val noise = player ?: load()?.also { player = it } ?: return
        if (!noise.playing) noise.play()
    }

    fun stop() {
        player?.takeIf { it.playing }?.apply {
            pause()
            currentTime = 0.0
        }
    }

    private suspend fun load(): AVAudioPlayer? {
        val bytes = try {
            Res.readBytes("files/radionoise.mp3")
        } catch (e: Exception) {
            logError("IosTuningNoise", "Could not read the tuning noise", e)
            return null
        }
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        return try {
            AVAudioPlayer(data = data, error = null).apply {
                // Negative means loop forever, which is isLooping = true on Android.
                numberOfLoops = -1
                prepareToPlay()
            }
        } catch (e: Exception) {
            logError("IosTuningNoise", "Could not open the tuning noise", e)
            null
        }
    }
}
