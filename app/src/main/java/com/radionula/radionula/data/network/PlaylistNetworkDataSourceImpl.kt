package com.radionula.radionula.data.network

import android.util.Log
import com.radionula.radionula.core.exceptions.NoConnectivityException
import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.domain.model.NulaTrack

class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: PlaylistApiService
) : PlaylistNetworkDataSource {

    override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>? {
        try {
            val xml = apiPlaylistApiService
                    .getPlaylist(channel.xmlPath, System.currentTimeMillis())
                    .use { it.string() }
            return RecentlyPlayedParser.parse(xml)
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internet")
        } catch (e: Exception) {
            Log.e("Playlist", "Could not read ${channel.xmlPath}", e)
        }
        return null
    }
}
