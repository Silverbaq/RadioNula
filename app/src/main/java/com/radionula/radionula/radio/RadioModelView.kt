package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.data.db.entity.History

class RadioModelView(
        private val playlistReposetory: PlaylistRepository
) : ViewModel() {
    private var currentChannel = "classic"
    private val currentSong: MutableLiveData<CurrentSong> = MutableLiveData()
    private val history: MutableLiveData<History> = MutableLiveData()

    fun observeCurrentSong(): LiveData<CurrentSong> = playlistReposetory.getCurrentSong()
    fun observePlaylist(): LiveData<MutableList<CurrentSong>> = playlistReposetory.getCurrentPlaylist()

    suspend fun fetchPlaylist(){
        playlistReposetory.fetchCurrentPlaylist()
    }

    fun tuneIn(){
        TODO("implement function")
    }

    fun nextChannel(){
        TODO("implement function")
    }

    fun pauseRadio(){
        TODO("implement function")
    }

}