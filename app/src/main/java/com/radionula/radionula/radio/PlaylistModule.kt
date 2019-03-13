package com.radionula.radionula.radio

import android.content.Context
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.PlaylistRepositoryImpl
import com.radionula.radionula.data.network.ConnectivityInterceptor
import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import com.radionula.services.mediaplayer.MediaplayerPresenter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module


val playlistModule = module {
    single { ChannelPresenter() }

    single<ConnectivityInterceptorImpl> { ConnectivityInterceptorImpl(androidContext()) }
    single<PlaylistApiService> { PlaylistApiService(get()) }
    single<PlaylistNetworkDataSource> { PlaylistNetworkDataSourceImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    viewModel { RadioModelView(get(), get(), get()) }
}