package com.radionula.radionula.data.network

import androidx.lifecycle.LiveData
import com.radionula.radionula.data.network.response.PlaylistResponse

interface PlaylistNetworkDataSource {
    val downloadedPlaylist: LiveData<PlaylistResponse>

    suspend fun fetchPlaylist()
}