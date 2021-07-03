package com.radionula.radionula.radio

import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.PlaylistRepositoryImpl
import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playlistModule = module {
    single { ChannelPresenter() }
    single { ConnectivityInterceptorImpl(androidContext()) }
    single { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> { PlaylistNetworkDataSourceImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}