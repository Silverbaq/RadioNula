package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.radionula.radionula.R
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.model.NulaTrack
import com.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RadioViewModel(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter,
        private val mediaplayerPresenter: MediaplayerPresenter,
        private val nulaDatabase: NulaDatabase
) : ViewModel() {
    private val channelData = MutableLiveData<Triple<Int, Int, Int>>()
    private val _favoriteAdded = MutableLiveData<String>()

    val currentSong: LiveData<CurrentSong> = playlistReposetory.currentSong().asLiveData()
    val playlist: LiveData<List<NulaTrack>> = playlistReposetory.currentPlaylist().asLiveData()

    /** Comes from the player, so audio focus and notification pauses show up here too. */
    val isPlaying: LiveData<Boolean> = mediaplayerPresenter.isPlaying.asLiveData()
    val currentChannelResources: LiveData<Triple<Int, Int, Int>> = channelData
    val favoriteAdded: LiveData<String> = _favoriteAdded

    private val tunedIn = MutableStateFlow(false)

    /**
     * Sticky. The tune-in button belongs to a cold start only - pausing or
     * skipping a channel must never bring it back.
     */
    val showTuneInButton: LiveData<Boolean> = tunedIn.map { !it }.asLiveData()

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
        tunedIn.value = true
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
            _favoriteAdded.postValue(track.title)
        }
    }

    private suspend fun onChannelChanged(index: Int) {
        val channel = channelPresenter.select(index)
        playlistReposetory.setChannel(channel)
        channelData.postValue(getChannelLogo(channel))

        // Nothing is fetched until the radio has been started, so a cold start
        // shows no playlist rather than tracks this session never heard.
        if (tunedIn.value) playlistReposetory.fetchCurrentPlaylist()
    }

    private fun getChannelLogo(channel: ChannelPresenter.Channel): Triple<Int, Int, Int> {
        return when (channel) {
            ChannelPresenter.Channel.Classic -> {
                Triple(R.drawable.nula_channel1, R.drawable.skip_channel1, R.drawable.pause_channel1)
            }
            ChannelPresenter.Channel.Ch2 -> {
                Triple(R.drawable.nula_channel2, R.drawable.skip_channel2, R.drawable.pause_channel2)
            }
            ChannelPresenter.Channel.Smoky -> {
                Triple(R.drawable.nula_channel3, R.drawable.skip_channel3, R.drawable.pause_channel3)
            }
        }
    }
}
