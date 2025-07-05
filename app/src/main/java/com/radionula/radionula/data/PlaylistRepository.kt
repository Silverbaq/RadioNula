package com.radionula.radionula.data

import androidx.lifecycle.LiveData
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.ChannelPresenter
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository{
    fun currentPlaylist(): Flow<List<NulaTrack>>
    fun currentSong(): Flow<CurrentSong>
    suspend fun fetchCurrentPlaylist()
    fun setChannel(channel: ChannelPresenter.Channel)
    fun autoFetchPlaylist()
}