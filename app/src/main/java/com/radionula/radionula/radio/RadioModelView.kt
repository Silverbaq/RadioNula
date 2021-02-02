package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.model.NulaTrack
import com.radionula.services.mediaplayer.MediaplayerPresenter

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

    fun addFavoriteClicked(track: NulaTrack) {
        nulaDatabase.insertTrack(track)
        _favoriteAdded.postValue(track.titel)
    }

}