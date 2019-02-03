package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.entity.CurrentSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RadioModelView(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter
) : ViewModel() {

    private val tuneInData = MutableLiveData<Unit>()
    private val pauseData = MutableLiveData<Unit>()
    private val channelData = MutableLiveData<ChannelPresenter.Channel>()

    fun observeCurrentSong(): LiveData<CurrentSong> = playlistReposetory.getCurrentSong()
    fun observePlaylist(): LiveData<MutableList<CurrentSong>> = playlistReposetory.getCurrentPlaylist()
    fun observeTuneIn(): LiveData<Unit> = tuneInData
    fun observePause(): LiveData<Unit> = pauseData
    fun observeCurrentChannel(): LiveData<ChannelPresenter.Channel> = channelData


    suspend fun fetchPlaylist() {
        playlistReposetory.fetchCurrentPlaylist()
    }

    fun tuneIn() {
        tuneInData.value = Unit
    }

    suspend fun nextChannel() {
        channelPresenter.nextChannel()
        playlistReposetory.setChannel(channelPresenter.currentChannel)
        playlistReposetory.fetchCurrentPlaylist()

    }

    fun pauseRadio() {
        pauseData.value = Unit
    }

}