package com.radionula.services

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.radionula.radionula.MainActivity
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.radio.ChannelPresenter
import com.radionula.services.mediaplayer.TuningNoise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Hosts the radio player and its media session. The session is what gives us
 * lock screen, notification, headset and Bluetooth controls for free, and it
 * lets ExoPlayer own audio focus instead of us hand-rolling it.
 *
 * The three channels are the player's playlist, so "skip" is just
 * seekToNextMediaItem() and the notification's next button switches channel.
 */
@OptIn(UnstableApi::class)
class RadioPlaybackService : MediaSessionService() {

    private val tuningNoise: TuningNoise by inject()
    private val playlistRepository: PlaylistRepository by inject()

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true
            )
            // Pauses when the headphones are pulled, replacing BecomingNoisyReceiver.
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()

        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(PlayerStateListener())
        // Items are queued but deliberately not prepared: nothing hits the
        // network until the listener actually tunes in.
        player.setMediaItems(ChannelPresenter.Channel.entries.map { it.toMediaItem() })

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(openAppIntent())
            .build()

        observeNowPlaying()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /** Swiping the app away stops the radio, which is what the old service did. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.stop()
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        tuningNoise.release()
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun openAppIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    /**
     * Pushes the track from the channel's RSS feed into the session, so the
     * lock screen and notification show what is actually playing.
     */
    private fun observeNowPlaying() {
        serviceScope.launch {
            playlistRepository.currentSong().collect { song ->
                val player = mediaSession?.player ?: return@collect
                val index = player.currentMediaItemIndex
                val channel = ChannelPresenter.Channel.entries.getOrNull(index) ?: return@collect
                // Same URI means the same period, so this swaps metadata without
                // interrupting the stream.
                player.replaceMediaItem(index, channel.toMediaItem(song))
            }
        }
    }

    private fun ChannelPresenter.Channel.toMediaItem(song: CurrentSong? = null): MediaItem =
        MediaItem.Builder()
            .setMediaId(name)
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
                    .setStation(displayName)
                    .setTitle(song?.title?.takeIf { it.isNotBlank() } ?: displayName)
                    .setArtist(song?.artist)
                    .setArtworkUri(song?.cover?.takeIf { it.isNotBlank() }?.toUri())
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build()
            )
            .build()

    private inner class PlayerStateListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) = syncTuningNoise()

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = syncTuningNoise()

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Keeps the feed in step when the channel is switched from the
            // notification, with no UI attached.
            val index = mediaSession?.player?.currentMediaItemIndex ?: return
            ChannelPresenter.Channel.entries.getOrNull(index)?.let(playlistRepository::setChannel)
        }

        /** The static hiss while a stream buffers. */
        private fun syncTuningNoise() {
            val player = mediaSession?.player ?: return
            if (player.playbackState == Player.STATE_BUFFERING && player.playWhenReady) {
                tuningNoise.start()
            } else {
                tuningNoise.stop()
            }
        }
    }
}
