package com.radionula.radionula.features.player

import com.radionula.radionula.data.network.PlaylistFeedParser
import com.radionula.radionula.data.network.RecentlyPlayedParser
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playlistModule = module {
    // RecentlyPlayedParser still lives here, not in :shared - see the
    // PlaylistFeedParser doc comment. Resolved when Task 5 moves it.
    single<PlaylistFeedParser> { RecentlyPlayedParser::parse }
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}
