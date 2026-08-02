package com.radionula.radionula.core.di

import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.radionula.radionula.core.util.AndroidConnectivityMonitor
import com.radionula.radionula.core.util.AndroidWebSearch
import com.radionula.radionula.core.util.ConnectivityMonitor
import com.radionula.radionula.core.util.WebSearch
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * The bindings that need a Context, which is the only reason they are not in
 * :shared's sharedModule.
 */
val androidModule = module {
    // getDatabasePath("NulaDB") is the exact file SQLiteOpenHelper used, which
    // is what makes an existing install's favourites just appear.
    single(named("databasePath")) {
        androidContext().getDatabasePath("NulaDB").absolutePath
    }
    single<WebSearch> { AndroidWebSearch(androidContext()) }
    single<ConnectivityMonitor> {
        AndroidConnectivityMonitor(androidContext().getSystemService<ConnectivityManager>()!!)
    }
}
