package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.entity.CurrentSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RadioModelView(
        private val playlistReposetory: PlaylistRepository,
        private val channelPresenter: ChannelPresenter
) : ViewModel() {
    private var playing = false

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
        playing = true
    }

    suspend fun nextChannel() {
        if (playing) {
            channelPresenter.nextChannel()
            playlistReposetory.setChannel(channelPresenter.currentChannel)
            playlistReposetory.fetchCurrentPlaylist()

                channelData.postValue(channelPresenter.currentChannel)

        }
        playing = true
    }

    fun pauseRadio() {
        pauseData.value = Unit
        playing = false
    }

}