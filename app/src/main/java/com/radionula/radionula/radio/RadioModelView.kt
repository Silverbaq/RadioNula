package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.services.mediaplayer.MediaplayerPresenter

class RadioModelView(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter,
        private val mediaplayerPresenter: MediaplayerPresenter
) : ViewModel() {
    private val playingData = MutableLiveData<Boolean>()

    private val tuneInData = MutableLiveData<Unit>()
    private val pauseData = MutableLiveData<Unit>()
    private val channelData = MutableLiveData<ChannelPresenter.Channel>()

    fun observeCurrentSong(): LiveData<CurrentSong> = playlistReposetory.getCurrentSong()
    fun observePlaylist(): LiveData<MutableList<CurrentSong>> = playlistReposetory.getCurrentPlaylist()
    fun observeTuneIn(): LiveData<Unit> = tuneInData
    fun observePlaying(): LiveData<Boolean> = playingData
    fun observePause(): LiveData<Unit> = pauseData
    fun observeCurrentChannel(): LiveData<ChannelPresenter.Channel> = channelData
    fun observeGetsNoizy(): LiveData<Unit> = mediaplayerPresenter.getsNoizy

    suspend fun fetchPlaylist() {
        playlistReposetory.fetchCurrentPlaylist()
    }

    suspend fun autoFetchPlaylist(){
        playlistReposetory.autoFetchPlaylist()
    }

    fun tuneIn() {
        tuneInData.value = Unit
        playingData.value = true
        mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.url)
    }

    suspend fun nextChannel() {
        if (playingData.value == true) {
            channelPresenter.nextChannel()
            playlistReposetory.setChannel(channelPresenter.currentChannel)
            playlistReposetory.fetchCurrentPlaylist()
            channelData.postValue(channelPresenter.currentChannel)
        }
        playingData.postValue(true)
        mediaplayerPresenter.tuneIn(channelPresenter.currentChannel.url)
    }

    fun pauseRadio() {
        pauseData.value = Unit
        pauseData.value = null
        playingData.value = false
        mediaplayerPresenter.pauseRadio()
    }

}