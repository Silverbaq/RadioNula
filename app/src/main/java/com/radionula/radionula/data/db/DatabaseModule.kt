package com.radionula.radionula.data.db

import com.radionula.radionula.features.favorites.FavoritesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { NulaDatabase(androidContext()) }
    viewModel { FavoritesViewModel(get()) }
}