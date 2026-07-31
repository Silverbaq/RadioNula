package com.radionula.radionula.features.player

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playlistModule = module {
    viewModel { RadioViewModel(get(), get(), get(), get()) }
}
