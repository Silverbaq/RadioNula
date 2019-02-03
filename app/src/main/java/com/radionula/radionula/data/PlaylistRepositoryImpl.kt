package com.radionula.radionula.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.response.PlaylistResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
        private val playlistNetworkDataSource: PlaylistNetworkDataSource
) : PlaylistRepository {

    private var currentChannel = "classic"

    private val currentSong = MutableLiveData<CurrentSong>()
    private val playlist = MutableLiveData<MutableList<CurrentSong>>()

    init {
        playlistNetworkDataSource.downloadedPlaylist.observeForever { newPlaylist ->
            when (currentChannel) {
                "classic" -> {
                    currentSong.value = newPlaylist.classics.currentSong
                    playlist.value?.add(newPlaylist.classics.currentSong)
                }
                "ch2" -> {
                    currentSong.value = newPlaylist.ch2.currentSong
                    playlist.value?.add(newPlaylist.ch2.currentSong)
                }
                "smoky" -> {
                    currentSong.value = newPlaylist.smoky.currentSong
                    playlist.value?.add(newPlaylist.smoky.currentSong)
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

    override fun getCurrentPlaylist(): LiveData<MutableList<CurrentSong>> {
        return playlist
    }

    override fun setChannel(channel: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}