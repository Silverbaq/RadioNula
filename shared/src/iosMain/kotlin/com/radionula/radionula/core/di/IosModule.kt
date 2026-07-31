package com.radionula.radionula.core.di

import com.radionula.radionula.core.util.ConnectivityMonitor
import com.radionula.radionula.core.util.IosConnectivityMonitor
import com.radionula.radionula.core.util.IosWebSearch
import com.radionula.radionula.core.util.WebSearch
import com.radionula.radionula.features.player.playlistModule
import com.radionula.radionula.services.mediaplayer.IosMediaPlayerController
import com.radionula.radionula.services.mediaplayer.MediaPlayerController
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

/**
 * The iOS half of what :app provides on Android.
 *
 * Only the database path so far. MediaPlayerController and the ViewModel
 * bindings arrive with the player and the UI.
 */
val iosModule = module {
    single(named("databasePath")) {
        val documents = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true,
        ).first() as String
        "$documents/NulaDB"
    }
    single<ConnectivityMonitor> { IosConnectivityMonitor() }
    single<WebSearch> { IosWebSearch() }
    single<MediaPlayerController> { IosMediaPlayerController(get()) }
}

/** Called from Swift at app start - Koin's DSL is not usable from Objective-C. */
fun initKoin() {
    startKoin { modules(sharedModule, playlistModule, iosModule) }
}
