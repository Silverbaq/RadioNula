package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.radionula.radionula.R
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.model.NulaTrack
import com.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RadioViewModel(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter,
        private val mediaplayerPresenter: MediaplayerPresenter,
        private val nulaDatabase: NulaDatabase
) : ViewModel() {
    private val playingData = MutableLiveData<Boolean>()

    private val tuneInData = MutableLiveData<Unit>()
    private val pauseData = MutableLiveData<Unit>()
    private val channelData = MutableLiveData<Triple<Int, Int, Int>>()
    private val _favoriteAdded = MutableLiveData<String>()

    val currentSong: LiveData<CurrentSong> = playlistReposetory.getCurrentSong()
    val playlist: LiveData<List<NulaTrack>> = Transformations.map(playlistReposetory.getCurrentPlaylist()) { it -> it.map { NulaTrack(it.artist, it.title, it.cover) } }
    val tunedIn: LiveData<Unit> = tuneInData
    val isPlaying: LiveData<Boolean> = playingData
    val pause: LiveData<Unit> = pauseData
    val currentChannelResources: LiveData<Triple<Int, Int, Int>> = channelData
    val getsNoizy: LiveData<Unit> = mediaplayerPresenter.getsNoizy
    val favoriteAdded: LiveData<String> = _favoriteAdded

    fun autoFetchPlaylist() {
        GlobalScope.launch {
            playlistReposetory.autoFetchPlaylist()
        }
    }

    fun tuneIn() {
        tuneInData.postValue(Unit)
        playingData.postValue(true)
        mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.url)
        fetchPlaylist()
    }

    fun nextChannel() {
        GlobalScope.async {
            if (playingData.value == true) {
                channelPresenter.nextChannel()
                playlistReposetory.setChannel(channelPresenter.currentChannel)
                playlistReposetory.fetchCurrentPlaylist()
                channelData.postValue(getChannelLogo(channelPresenter.currentChannel))
            }
            playingData.postValue(true)
            mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.url)
        }
    }

    fun pauseRadio() {
        pauseData.postValue(Unit)
        playingData.postValue(false)
        mediaplayerPresenter.pauseRadio()
    }

    fun addFavoriteClicked(track: NulaTrack) {
        nulaDatabase.insertTrack(track)
        _favoriteAdded.postValue(track.title)
    }

    private fun fetchPlaylist() {
        GlobalScope.launch {
            playlistReposetory.fetchCurrentPlaylist()
        }
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