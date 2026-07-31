package com.radionula.radionula.features.player

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.ConnectivityInterceptorImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl

val playlistModule = module {
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
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}
