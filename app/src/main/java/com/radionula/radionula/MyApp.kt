package com.radionula.radionula

import android.app.Application
import android.content.Context

import com.radionula.radionula.util.NulaDatabase
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

        database = NulaDatabase(this)

        startKoin(this, listOf(playlistModule, radioPlayerModule, connectionModule))
    }

    companion object {

        var isPlaying: Boolean = false
        var reconnect = false

        private var database: NulaDatabase? = null

        fun addToFavorites(track: NulaTrack) {
            database!!.insertTrack(track)
        }

        fun LoadUserFavorites(context: Context): List<NulaTrack> {
            return database!!.selectAllTracks()
        }

        fun RemoveFavorit(item: NulaTrack) {
            database?.remoteTrack(item)
        }
    }
}
