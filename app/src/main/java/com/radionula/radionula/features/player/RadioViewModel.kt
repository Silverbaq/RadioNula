package com.radionula.radionula.features.player

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radionula.radionula.R
import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.domain.model.NulaTrack
import com.radionula.radionula.domain.repository.PlaylistRepository

/** The three per-channel drawables the player swaps together. */
data class ChannelArt(
    @param:DrawableRes val logo: Int,
    @param:DrawableRes val skip: Int,
    @param:DrawableRes val pause: Int,
)

data class PlayerUiState(
    val showTuneIn: Boolean = true,
    val isPlaying: Boolean = false,
    val cover: String = "",
    val tracks: List<NulaTrack> = emptyList(),
    val channelArt: ChannelArt = CLASSIC_ART,
)

val CLASSIC_ART =
    ChannelArt(R.drawable.nula_channel1, R.drawable.skip_channel1, R.drawable.pause_channel1)

class RadioViewModel(
    private val playlistReposetory: PlaylistRepository,
    private val channelPresenter: ChannelPresenter,
    private val mediaplayerPresenter: MediaplayerPresenter,
    private val nulaDatabase: NulaDatabase
) : ViewModel() {
    private val channelArt = MutableStateFlow(CLASSIC_ART)

    /**
     * Sticky. The tune-in button belongs to a cold start only - pausing or
     * skipping a channel must never bring it back.
     */
    private val tunedIn = MutableStateFlow(false)

    /** One-shot, so a rotation does not re-show the toast. */
    private val _favoriteAdded = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val favoriteAdded: Flow<String> = _favoriteAdded

    val uiState: StateFlow<PlayerUiState> = combine(
        // Both repository flows are replay-1 SharedFlows that stay empty until
        // the first fetch, and combine waits for every source. Without a seed
        // the screen would have no state at all before the first track lands.
        playlistReposetory.currentSong().onStart { emit(EMPTY_SONG) },
        playlistReposetory.currentPlaylist().onStart { emit(emptyList()) },
        // Comes from the player, so audio focus and notification pauses show up here too.
        mediaplayerPresenter.isPlaying,
        channelArt,
        tunedIn,
    ) { song, playlist, playing, art, tuned ->
        PlayerUiState(
            showTuneIn = !tuned,
            isPlaying = playing,
            cover = song.cover,
            tracks = playlist,
            channelArt = art,
        )
        // Eagerly, not WhileSubscribed: the sticky tune-in state has to survive
        // the screen going away, and it is the ViewModel that owns it.
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlayerUiState())

    init {
        viewModelScope.launch {
            // Playback resumed from the notification counts as tuned in as well.
            mediaplayerPresenter.isPlaying.collect { if (it) tunedIn.value = true }
        }
        viewModelScope.launch {
            mediaplayerPresenter.channelIndex.collect(::onChannelChanged)
        }
    }

    fun autoFetchPlaylist() {
        playlistReposetory.autoFetchPlaylist()
    }

    fun tuneIn() {
        if (!tunedIn.value) {
            tunedIn.value = true
            // A fresh ViewModel means a fresh listening session. The repository
            // is a Koin single that survives the activity, so anything left from
            // last time is dropped before the first fetch rather than shown.
            playlistReposetory.clearSession()
        }
        mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.ordinal)
    }

    /**
     * Skips channel while playing, and starts the current channel while stopped
     * - which is also the way back from a pause, since the tune-in button is
     * gone for good after the first tap.
     */
    fun nextChannel() {
        if (mediaplayerPresenter.isPlaying.value) {
            mediaplayerPresenter.nextChannel()
        } else {
            mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.ordinal)
        }
    }

    fun pauseRadio() {
        mediaplayerPresenter.pauseRadio()
    }

    fun addFavoriteClicked(track: NulaTrack) {
        viewModelScope.launch {
            nulaDatabase.insertTrack(track)
            _favoriteAdded.emit(track.title)
        }
    }

    private suspend fun onChannelChanged(index: Int) {
        val channel = channelPresenter.select(index)
        playlistReposetory.setChannel(channel)
        channelArt.value = getChannelLogo(channel)

        // Nothing is fetched until the radio has been started, so a cold start
        // shows no playlist rather than tracks this session never heard.
        if (tunedIn.value) playlistReposetory.fetchCurrentPlaylist()
    }

    private fun getChannelLogo(channel: ChannelPresenter.Channel): ChannelArt {
        return when (channel) {
            ChannelPresenter.Channel.Classic -> CLASSIC_ART
            ChannelPresenter.Channel.Ch2 -> ChannelArt(
                R.drawable.nula_channel2, R.drawable.skip_channel2, R.drawable.pause_channel2
            )
            ChannelPresenter.Channel.Smoky -> ChannelArt(
                R.drawable.nula_channel3, R.drawable.skip_channel3, R.drawable.pause_channel3
            )
        }
    }

    private companion object {
        val EMPTY_SONG = CurrentSong(
            artist = "",
            cover = "",
            title = ""
        )
    }
}
