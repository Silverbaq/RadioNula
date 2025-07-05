package com.radionula.radionula.radio

import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.PlaylistRepositoryImpl
import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val playlistModule = module {
    single { ChannelPresenter() }
    single { ConnectivityInterceptorImpl(androidContext()) }
    single { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> { PlaylistNetworkDataSourceImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(
        playlistNetworkDataSource = get(),
        coroutineScope = get(named("ioScope"))
    ) }
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}