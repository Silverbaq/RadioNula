package com.radionula.radionula.data.network

import android.util.Log
import com.radionula.radionula.core.exceptions.NoConnectivityException
import com.radionula.radionula.core.util.ChannelPresenter

class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: com.radionula.radionula.data.PlaylistApiService
) : com.radionula.radionula.data.network.PlaylistNetworkDataSource {

    override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<com.radionula.radionula.domain.model.NulaTrack>? {
        try {
            val xml = apiPlaylistApiService
                    .getPlaylist(channel.xmlPath, System.currentTimeMillis())
                    .use { it.string() }
            return _root_ide_package_.com.radionula.radionula.data.network.RecentlyPlayedParser.parse(xml)
        } catch (e: NoConnectivityException) {
            Log.e("Connectivity", "No internet")
        } catch (e: Exception) {
            Log.e("Playlist", "Could not read ${channel.xmlPath}", e)
        }
        return null
    }
}
