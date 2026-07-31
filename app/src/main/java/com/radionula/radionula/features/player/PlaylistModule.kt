package com.radionula.radionula.features.player

import com.radionula.radionula.core.util.ChannelPresenter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import com.radionula.radionula.data.repository.PlaylistRepositoryImpl
import com.radionula.radionula.domain.repository.PlaylistRepository

val playlistModule = module {
    single { ChannelPresenter() }
    single {
        ConnectivityInterceptorImpl(
            androidContext()
        )
    }
    single { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> {
        PlaylistNetworkDataSourceImpl(
            get()
        )
    }
    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistNetworkDataSource = get(),
            coroutineScope = get(named("ioScope"))
        )
    }
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}