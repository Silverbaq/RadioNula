package com.radionula.radionula

import com.radionula.radionula.data.db.NulaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {
    single { NulaDatabase(androidContext()) }
    factory<CoroutineScope> (named("default")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    factory<CoroutineScope> (named("main")) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    factory<CoroutineScope> (named("ioScope")) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

}