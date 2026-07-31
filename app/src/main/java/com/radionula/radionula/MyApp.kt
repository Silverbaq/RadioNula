package com.radionula.radionula

import android.app.Application
import com.radionula.radionula.core.di.appModule
import com.radionula.radionula.data.db.databaseModule
import com.radionula.radionula.features.player.playlistModule
import com.radionula.radionula.services.mediaplayer.radioPlayerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Created by silverbaq on 12/6/15.
 */
class MyApp : Application() {

    override fun onCreate() {
        // TODO Auto-generated method stub
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MyApp)
            modules(appModule, playlistModule, radioPlayerModule, databaseModule)
        }
    }
}