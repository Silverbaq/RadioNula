package com.radionula.radionula.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.radionula.internal.NoConnectivityException
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.response.PlaylistResponse

class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: PlaylistApiService
) : PlaylistNetworkDataSource {
    private val _downloadedPlaylist = MutableLiveData<PlaylistResponse>()
    override val downloadedPlaylist: LiveData<PlaylistResponse>
        get() = _downloadedPlaylist

    override suspend fun fetchPlaylist() {
        try {
            val fetchedPlaylist = apiPlaylistApiService
                    .getPlaylist()
                    .await()
            _downloadedPlaylist.postValue(fetchedPlaylist)
        } catch (e: NoConnectivityException){
            Log.e("Connectivity", "No internet")
        } catch (e: Exception){
            Log.e("Connectivity", "A network Exception was thrown!")
        }
    }
}