package com.radionula.radionula.data.network

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.domain.model.NulaTrack

interface PlaylistNetworkDataSource {
    /** Newest track first, or null when the feed could not be fetched or parsed. */
    suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>?
}

/**
 * Parses a "recently played" XML feed into tracks, newest first.
 *
 * Bound to `RecentlyPlayedParser::parse` from :app's Koin module.
 * RecentlyPlayedParser itself still lives in :app (it uses javax.xml /
 * org.w3c.dom, JVM-only APIs) - and :app already depends on :shared, so
 * :shared cannot depend back on :app to call it directly without a circular
 * module dependency. Injecting the parse function is the seam that lets
 * PlaylistNetworkDataSourceImpl live in commonMain anyway. Resolved in
 * Task 5, when the parser itself moves into shared and this alias collapses
 * back into a direct call.
 */
typealias PlaylistFeedParser = (xml: String) -> List<NulaTrack>
