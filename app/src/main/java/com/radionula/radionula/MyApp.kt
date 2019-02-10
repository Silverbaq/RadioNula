package com.radionula.radionula

import android.app.Application
import android.content.Context

import com.androidquery.AQuery
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.radionula.radionula.Util.NulaDatabase
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.playlistModule
import com.radionula.services.mediaplayer.radioPlayerModule

import java.util.ArrayList

import org.koin.android.ext.android.startKoin

/**
 * Created by silverbaq on 12/6/15.
 */
class MyApp : Application() {

    override fun onCreate() {
        // TODO Auto-generated method stub
        super.onCreate()

        // Create global configuration and initialize ImageLoader with this config
        val config = ImageLoaderConfiguration.Builder(this).build()
        ImageLoader.getInstance().init(config)

        database = NulaDatabase(this)
        aquery = AQuery(this)

        //isPlaying = false
        //tunedIn = false

        startKoin(this, listOf(playlistModule, radioPlayerModule))
    }

    companion object {

        lateinit var aquery: AQuery
        private var imageLoader: ImageLoader? = null

        var isPlaying: Boolean = false
        var reconnect = false

        private var database: NulaDatabase? = null

        fun getImageLoader(): ImageLoader? {
            if (imageLoader == null) {
                imageLoader = ImageLoader.getInstance()
            }
            return imageLoader
        }

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
