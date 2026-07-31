package com.radionula.radionula.core.di

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import com.radionula.radionula.data.nulaHttpClient
import com.radionula.radionula.data.repository.PlaylistRepositoryImpl
import com.radionula.radionula.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Everything the shared module can wire up on its own.
 *
 * ViewModel bindings stay in :app: Koin's viewModel { } DSL would pull
 * koin-core-viewmodel into commonMain, and there is no second platform
 * consuming it yet.
 */
val sharedModule = module {
    factory<CoroutineScope>(named("ioScope")) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    single { nulaHttpClient() }
    single { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> { PlaylistNetworkDataSourceImpl(get()) }

    single { ChannelPresenter() }
    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistNetworkDataSource = get(),
            coroutineScope = get(named("ioScope")),
        )
    }

    single<SQLiteDriver> { BundledSQLiteDriver() }
    // The database path comes from :app - it needs a Context.
    single { NulaDatabase(get(), get(named("databasePath"))) }
}
