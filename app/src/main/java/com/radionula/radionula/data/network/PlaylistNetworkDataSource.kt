package com.radionula.radionula.data.network

import com.radionula.radionula.data.network.response.PlaylistResponse

interface PlaylistNetworkDataSource {
    suspend fun fetchPlaylist(): PlaylistResponse?
}