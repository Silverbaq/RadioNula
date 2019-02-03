package com.radionula.radionula.data

import androidx.lifecycle.LiveData
import com.radionula.radionula.data.db.entity.CurrentSong

interface PlaylistRepository{
    fun getCurrentPlaylist(): LiveData<MutableList<CurrentSong>>
    fun getCurrentSong(): LiveData<CurrentSong>
    suspend fun fetchCurrentPlaylist()
    fun setChannel(channel: String)
}