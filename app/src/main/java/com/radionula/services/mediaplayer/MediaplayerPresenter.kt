package com.radionula.services.mediaplayer

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.radionula.services.RadioPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Talks to [RadioPlaybackService] through a MediaController, and reports back
 * what the player is *actually* doing - which matters now that audio focus, the
 * notification and headset buttons can all change playback behind the UI's back.
 *
 * ponytail: the controller connects on first use and is never released, so the
 * service stays bound for the process lifetime. Move connect/release into the
 * Activity's onStart/onStop if the service needs to die sooner.
 */
class MediaplayerPresenter(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _channelIndex = MutableStateFlow(0)
    val channelIndex: StateFlow<Int> = _channelIndex

    fun tuneIn(channelIndex: Int) = withController { controller ->
        if (controller.currentMediaItemIndex != channelIndex) {
            controller.seekToDefaultPosition(channelIndex)
        }
        controller.prepare()
        controller.play()
    }

    fun nextChannel() = withController { controller ->
        controller.seekToNextMediaItem()
        controller.prepare()
        controller.play()
    }

    fun pauseRadio() = withController(MediaController::pause)

    private fun withController(action: (MediaController) -> Unit) {
        val future = controllerFuture ?: connect()
        // Fires straight away when the future is already done.
        future.addListener(
            { runCatching { future.get() }.getOrNull()?.let(action) },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun connect(): ListenableFuture<MediaController> {
        val token = SessionToken(context, ComponentName(context, RadioPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future

        future.addListener(
            {
                runCatching { future.get() }.getOrNull()?.let { connected ->
                    controller = connected
                    connected.addListener(PlaybackListener())
                    _isPlaying.value = connected.isPlaying
                    _channelIndex.value = connected.currentMediaItemIndex.coerceAtLeast(0)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
        return future
    }

    private inner class PlaybackListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val index = controller?.currentMediaItemIndex ?: return
            if (index >= 0) _channelIndex.value = index
        }
    }
}
