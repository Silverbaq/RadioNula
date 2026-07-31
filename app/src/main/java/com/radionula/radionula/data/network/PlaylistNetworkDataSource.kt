package com.radionula.radionula.data.network

import com.radionula.radionula.core.util.ChannelPresenter

interface PlaylistNetworkDataSource {
    /** Newest track first, or null when the feed could not be fetched or parsed. */
    suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<com.radionula.radionula.domain.model.NulaTrack>?
}
