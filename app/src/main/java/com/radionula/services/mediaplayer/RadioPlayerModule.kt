package com.radionula.services.mediaplayer

import org.koin.dsl.module.module

val radioPlayerModule = module {
    single { RadioPlayer(get()) }
    single { MediaplayerPresenter(get()) }
}