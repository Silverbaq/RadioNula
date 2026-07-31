package com.radionula.radionula.data.db

import com.radionula.radionula.features.favorites.FavoritesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val databaseModule = module {
    // getDatabasePath("NulaDB") is the exact file SQLiteOpenHelper used, which
    // is what makes an existing install's favourites just appear.
    single(named("databasePath")) {
        androidContext().getDatabasePath("NulaDB").absolutePath
    }
    viewModel { FavoritesViewModel(get()) }
}
