package com.radionula.services.mediaplayer

import com.radionula.radionula.radio.BecomingNoisyReceiver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val radioPlayerModule = module {
    single { RadioPlayer(get()) }
    single { BecomingNoisyReceiver() }
    single { MediaplayerPresenter(androidContext(), get()) }
}