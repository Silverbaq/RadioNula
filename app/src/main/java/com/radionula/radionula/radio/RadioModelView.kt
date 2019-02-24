package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.entity.CurrentSong

class RadioModelView(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter
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


    suspend fun fetchPlaylist() {
        playlistReposetory.fetchCurrentPlaylist()
    }

    suspend fun autoFetchPlaylist(){
        playlistReposetory.autoFetchPlaylist()
    }

    fun tuneIn() {
        tuneInData.value = Unit
        tuneInData.value = null
        playingData.value = true
    }

    suspend fun nextChannel() {
        if (playingData.value == true) {
            channelPresenter.nextChannel()
            playlistReposetory.setChannel(channelPresenter.currentChannel)
            playlistReposetory.fetchCurrentPlaylist()
            channelData.postValue(channelPresenter.currentChannel)
        }
        playingData.postValue(true)
    }

    fun pauseRadio() {
        pauseData.value = Unit
        pauseData.value = null
        playingData.value = false
    }

}