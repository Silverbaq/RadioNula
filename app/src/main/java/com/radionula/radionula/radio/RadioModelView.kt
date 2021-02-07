package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.model.NulaTrack
import com.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RadioModelView(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter,
        private val mediaplayerPresenter: MediaplayerPresenter,
        private val nulaDatabase: NulaDatabase
) : ViewModel() {
    private val playingData = MutableLiveData<Boolean>()

    private val tuneInData = MutableLiveData<Unit>()
    private val pauseData = MutableLiveData<Unit>()
    private val channelData = MutableLiveData<ChannelPresenter.Channel>()
    private val _favoriteAdded = MutableLiveData<String>()

    fun observeCurrentSong(): LiveData<CurrentSong> = playlistReposetory.getCurrentSong()
    fun observePlaylist(): LiveData<MutableList<CurrentSong>> = playlistReposetory.getCurrentPlaylist()
    fun observeTuneIn(): LiveData<Unit> = tuneInData
    fun observePlaying(): LiveData<Boolean> = playingData
    fun observePause(): LiveData<Unit> = pauseData
    fun observeCurrentChannel(): LiveData<ChannelPresenter.Channel> = channelData
    fun observeGetsNoizy(): LiveData<Unit> = mediaplayerPresenter.getsNoizy
    val favoriteAdded: LiveData<String> = _favoriteAdded

    fun fetchPlaylist() {
        GlobalScope.launch {
            playlistReposetory.fetchCurrentPlaylist()
        }
    }

    fun autoFetchPlaylist() {
        GlobalScope.launch {
            playlistReposetory.autoFetchPlaylist()
        }
    }

    fun tuneIn() {
        tuneInData.postValue(Unit)
        playingData.postValue(true)
        mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.url)
    }

    fun nextChannel() {
        GlobalScope.async {
            if (playingData.value == true) {
                channelPresenter.nextChannel()
                playlistReposetory.setChannel(channelPresenter.currentChannel)
                playlistReposetory.fetchCurrentPlaylist()
                channelData.postValue(channelPresenter.currentChannel)
            }
            playingData.postValue(true)
            mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.url)
        }
    }

    fun pauseRadio() {
        pauseData.value = Unit
        pauseData.value = null
        playingData.value = false
        mediaplayerPresenter.pauseRadio()
    }

    fun addFavoriteClicked(track: NulaTrack) {
        nulaDatabase.insertTrack(track)
        _favoriteAdded.postValue(track.title)
    }

}