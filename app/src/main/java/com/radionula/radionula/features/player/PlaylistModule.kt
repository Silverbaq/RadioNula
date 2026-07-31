package com.radionula.radionula.features.player

import com.radionula.radionula.core.util.ChannelPresenter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val playlistModule = module {
    single { ChannelPresenter() }
    single {
        _root_ide_package_.com.radionula.radionula.data.network.ConnectivityInterceptorImpl(
            androidContext()
        )
    }
    single { _root_ide_package_.com.radionula.radionula.data.PlaylistApiService(get()) }
    single<com.radionula.radionula.data.network.PlaylistNetworkDataSource> {
        _root_ide_package_.com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl(
            get()
        )
    }
    single<com.radionula.radionula.domain.repository.PlaylistRepository> {
        _root_ide_package_.com.radionula.radionula.data.repository.PlaylistRepositoryImpl(
            playlistNetworkDataSource = get(),
            coroutineScope = get(named("ioScope"))
        )
    }
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}