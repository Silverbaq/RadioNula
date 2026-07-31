package com.radionula.radionula.services.mediaplayer

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val radioPlayerModule = module {
    single { TuningNoise(androidContext()) }
    single<MediaPlayerController> { MediaplayerPresenter(androidContext()) }
}
