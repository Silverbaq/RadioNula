package com.radionula.radionula.services.mediaplayer

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.core.util.logError
import com.radionula.radionula.domain.repository.PlaylistRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyIsLiveStream
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatus
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess

/**
 * The iOS half of what RadioPlaybackService does on Android.
 *
 * AVPlayer plays one item at a time, so "the three channels are the playlist"
 * becomes an index this class keeps and an item it swaps in. Background audio
 * comes from the AVAudioSession playback category (plus the audio background
 * mode in the app's Info.plist), and the lock screen comes from the remote
 * command centre - the two things media3's MediaSession gave us for free.
 *
 * ponytail: isPlaying is tracked from our own calls and from the remote
 * commands rather than observed on AVPlayer, because KVO from Kotlin/Native
 * needs an ObjC observer class for one boolean. A stall or a failed stream will
 * therefore still read as playing; observe timeControlStatus if that matters.
 * There is also no tuning noise and no lock-screen artwork yet.
 */
@OptIn(ExperimentalForeignApi::class)
class IosMediaPlayerController(
    private val playlistRepository: PlaylistRepository,
) : MediaPlayerController {

    private val player = AVPlayer()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _channelIndex = MutableStateFlow(0)
    override val channelIndex: StateFlow<Int> = _channelIndex

    init {
        configureAudioSession()
        registerRemoteCommands()
        observeInterruptions()
        observeNowPlaying()
    }

    override fun tuneIn(channelIndex: Int) {
        val channel = ChannelPresenter.Channel.entries.getOrNull(channelIndex) ?: return
        _channelIndex.value = channelIndex
        val url = NSURL.URLWithString(channel.url) ?: return
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = url))
        player.play()
        _isPlaying.value = true
    }

    override fun nextChannel() {
        // REPEAT_MODE_ALL on the Android player - the notification's next button
        // wraps from the last channel back to the first.
        val next = (_channelIndex.value + 1) % ChannelPresenter.Channel.entries.size
        tuneIn(next)
    }

    override fun pauseRadio() {
        player.pause()
        _isPlaying.value = false
    }

    private fun resume() {
        // Nothing queued yet means the listener has not tuned in at all, so
        // there is no channel to resume - start the one the UI is showing.
        if (player.currentItem == null) tuneIn(_channelIndex.value) else player.play()
        _isPlaying.value = true
    }

    private fun configureAudioSession() {
        val session = AVAudioSession.sharedInstance()
        try {
            session.setCategory(AVAudioSessionCategoryPlayback, null)
            session.setActive(true, null)
        } catch (e: Throwable) {
            logError("IosMediaPlayer", "Could not configure the audio session", e)
        }
    }

    private fun registerRemoteCommands() {
        val commands = MPRemoteCommandCenter.sharedCommandCenter()
        commands.playCommand.addTargetWithHandler { resume(); success() }
        commands.pauseCommand.addTargetWithHandler { pauseRadio(); success() }
        commands.nextTrackCommand.addTargetWithHandler { nextChannel(); success() }
    }

    private fun success(): MPRemoteCommandHandlerStatus = MPRemoteCommandHandlerStatusSuccess

    /**
     * A phone call or a Siri request pauses playback without going through us,
     * and the screen has to show that - the same reason isPlaying is reported by
     * the player rather than set by the UI on Android.
     */
    private fun observeInterruptions() {
        NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVAudioSessionInterruptionNotification,
            `object` = null,
            queue = null,
        ) { _ -> _isPlaying.value = false }
    }

    /** The feed's track, on the lock screen. */
    private fun observeNowPlaying() {
        scope.launch {
            playlistRepository.currentSong().collect { song ->
                val channel = ChannelPresenter.Channel.entries
                    .getOrNull(_channelIndex.value) ?: return@collect
                MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = mapOf<Any?, Any?>(
                    MPMediaItemPropertyTitle to song.title.ifBlank { channel.displayName },
                    MPMediaItemPropertyArtist to song.artist,
                    MPNowPlayingInfoPropertyIsLiveStream to true,
                )
            }
        }
    }
}
