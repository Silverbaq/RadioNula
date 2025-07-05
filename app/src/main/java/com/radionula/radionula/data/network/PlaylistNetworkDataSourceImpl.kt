package com.radionula.radionula.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.radionula.internal.NoConnectivityException
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.response.PlaylistResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: PlaylistApiService
) : PlaylistNetworkDataSource {

    override suspend fun fetchPlaylist(): PlaylistResponse? {
        try {
            val fetchedPlaylist = apiPlaylistApiService.getPlaylist()
            return fetchedPlaylist
        } catch (e: NoConnectivityException){
            Log.e("Connectivity", "No internet")
        } catch (e: Exception){
            Log.e("Connectivity", "A network Exception was thrown!")
        }
        return null
    }
}