package com.radionula.radionula.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.radio.ChannelPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaylistRepositoryImpl(
        private val playlistNetworkDataSource: PlaylistNetworkDataSource
) : PlaylistRepository {

    private var currentChannel: ChannelPresenter.Channel = ChannelPresenter.Channel.Classic

    private val currentSong = MutableLiveData<CurrentSong>()
    private val playlist = MutableLiveData<MutableList<CurrentSong>>().apply { postValue(mutableListOf()) }

    init {
        playlistNetworkDataSource.downloadedPlaylist.observeForever { newPlaylist ->
            when (currentChannel) {
                ChannelPresenter.Channel.Classic -> {
                    if (currentSong.value != newPlaylist.classics.currentSong) {
                        currentSong.value = newPlaylist.classics.currentSong
                        val temp = playlist.value
                        temp?.add(0, newPlaylist.classics.currentSong)
                        playlist.value = temp
                    }
                }
                ChannelPresenter.Channel.Ch2 -> {
                    if (currentSong.value != newPlaylist.ch2.currentSong) {
                        currentSong.value = newPlaylist.ch2.currentSong
                        val temp = playlist.value
                        temp?.add(0, newPlaylist.ch2.currentSong)
                        playlist.value = temp
                    }
                }
                ChannelPresenter.Channel.Smoky -> {
                    if (currentSong.value != newPlaylist.smoky.currentSong) {
                        currentSong.value = newPlaylist.smoky.currentSong
                        val temp = playlist.value
                        temp?.add(0, newPlaylist.smoky.currentSong)
                        playlist.value = temp
                    }
                }
            }
        }
    }

    override fun getCurrentSong(): LiveData<CurrentSong> {
        return currentSong
    }

    override suspend fun fetchCurrentPlaylist() {
        GlobalScope.launch(Dispatchers.IO) {
            playlistNetworkDataSource.fetchPlaylist()
        }
    }

    override suspend fun autoFetchPlaylist(){
        GlobalScope.launch(Dispatchers.IO) {
            while(true){
                playlistNetworkDataSource.fetchPlaylist()

                delay(20_000)
            }
        }
    }

    override fun getCurrentPlaylist(): LiveData<MutableList<CurrentSong>> {
        return playlist
    }

    override fun setChannel(channel: ChannelPresenter.Channel) {
        currentChannel = channel
    }
}