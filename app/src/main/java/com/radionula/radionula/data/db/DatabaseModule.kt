package com.radionula.radionula.data.db

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val databaseModule = module {
    single { NulaDatabase(androidContext()) }
}