package com.radionula.radionula.features.player

import com.radionula.radionula.features.favorites.FavoritesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Both ViewModels, wired the same way on both platforms. */
val playlistModule = module {
    viewModel { RadioViewModel(get(), get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
}
