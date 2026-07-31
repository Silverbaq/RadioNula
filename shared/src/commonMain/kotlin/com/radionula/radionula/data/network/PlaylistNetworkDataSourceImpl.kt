package com.radionula.radionula.data.network

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.core.util.epochMillis
import com.radionula.radionula.core.util.logError
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.domain.model.NulaTrack

class PlaylistNetworkDataSourceImpl(
        private val apiPlaylistApiService: PlaylistApiService
) : PlaylistNetworkDataSource {

    override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>? {
        // Returns null on *any* failure, deliberately. The repository's
        // early-return on null is what keeps a failed poll from clearing the
        // session or publishing a stale track, and being offline is just one
        // more failure now that the connectivity interceptor is gone.
        try {
            val xml = apiPlaylistApiService.getPlaylist(channel.xmlPath, epochMillis())
            return RecentlyPlayedParser.parse(xml)
        } catch (e: Exception) {
            logError("Playlist", "Could not read ${channel.xmlPath}", e)
        }
        return null
    }
}
