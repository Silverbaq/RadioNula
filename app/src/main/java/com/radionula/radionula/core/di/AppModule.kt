package com.radionula.radionula.core.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module


val appModule = module {
    // NulaDatabase lives in databaseModule; it was declared in both, and Koin
    // silently let the later definition win.
    factory<CoroutineScope> (named("default")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    factory<CoroutineScope> (named("main")) { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    factory<CoroutineScope> (named("ioScope")) { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

}