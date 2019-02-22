package com.radionula.radionula

import android.app.Application
import android.content.Context

import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.data.db.databaseModule
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.networkavaliable.connectionModule
import com.radionula.radionula.radio.playlistModule
import com.radionula.services.mediaplayer.radioPlayerModule

import org.koin.android.ext.android.startKoin

/**
 * Created by silverbaq on 12/6/15.
 */
class MyApp : Application() {

    override fun onCreate() {
        // TODO Auto-generated method stub
        super.onCreate()

        startKoin(this, listOf(playlistModule, radioPlayerModule, connectionModule, databaseModule))
    }

    companion object {
        var isPlaying: Boolean = false
        var reconnect = false
    }
}
