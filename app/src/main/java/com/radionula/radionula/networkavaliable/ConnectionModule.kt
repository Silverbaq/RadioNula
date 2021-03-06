package com.radionula.radionula.networkavaliable

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val connectionModule = module {
    single { ConnectionViewModel(androidContext()) }
}