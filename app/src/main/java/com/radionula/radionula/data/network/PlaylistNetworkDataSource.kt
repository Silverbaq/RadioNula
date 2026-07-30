package com.radionula.radionula.data.network

import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.ChannelPresenter

interface PlaylistNetworkDataSource {
    /** Newest track first, or null when the feed could not be fetched or parsed. */
    suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>?
}
