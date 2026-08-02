package com.radionula.radionula.services.mediaplayer

import kotlinx.coroutines.flow.StateFlow

/**
 * What the player looks like to the ViewModel.
 *
 * isPlaying and channelIndex are reported by the player itself, not set by the
 * UI, which is how an audio-focus pause, a notification tap or a headset
 * button reaches the screen.
 *
 * The media3 implementation lives in :app - media3 is Android-only, and this
 * is the seam an iOS AVPlayer implementation would sit behind.
 */
interface MediaPlayerController {
    val isPlaying: StateFlow<Boolean>
    val channelIndex: StateFlow<Int>

    fun tuneIn(channelIndex: Int)
    fun nextChannel()
    fun pauseRadio()
}
