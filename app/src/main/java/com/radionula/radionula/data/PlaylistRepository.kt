package com.radionula.radionula.data

import androidx.lifecycle.LiveData
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.radio.ChannelPresenter

interface PlaylistRepository{
    fun getCurrentPlaylist(): LiveData<MutableList<CurrentSong>>
    fun getCurrentSong(): LiveData<CurrentSong>
    suspend fun fetchCurrentPlaylist()
    fun setChannel(channel: ChannelPresenter.Channel)
}