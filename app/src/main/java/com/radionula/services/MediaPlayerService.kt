package com.radionula.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.radionula.radionula.MainActivity
import com.radionula.radionula.R
import com.radionula.radionula.model.Constants
import com.radionula.services.mediaplayer.RadioPlayer
import org.koin.android.ext.android.inject

class MediaPlayerService : Service() {
    private val radioPlayer: RadioPlayer by inject()

    //private val audioPlayer = AudioPlayer()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            Log.i(TAG, "Received Start Foreground Intent ")
            showNotification()
            radioPlayer.tuneIn(intent.getStringExtra("radioUrl"))
        } else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            Log.i(TAG, "Received Stop Foreground Intent")
            stopForeground(true)
            radioPlayer.pauseRadio()
            radioPlayer.stopRadioNoize()
            stopSelf()
        } else if (intent.action == Constants.ACTION.NEXT_ACTION){
            Log.i(TAG, "Received Play next Foreground Intent")
            showNotification()
            radioPlayer.pauseRadio()
            radioPlayer.tuneIn(intent.getStringExtra("radioUrl"))
        }


        return Service.START_STICKY
    }

    private fun showNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val icon = BitmapFactory.decodeResource(resources,
                R.drawable.ico_favorite)

        val notification = NotificationCompat.Builder(this, "my_channel")
                .setContentTitle("Radio Nula")
                .setTicker("Radio Nula")
                //.setContentText("My song")
                .setSmallIcon(R.drawable.ico_favorite)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build()

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification)

    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

/*
    internal inner class AudioPlayer : IControls {
        private var exoPlayer: ExoPlayer? = null

        private val eventListener = object : ExoPlayer.EventListener {
            override fun onTimelineChanged(timeline: Timeline, manifest: Any) {}

            override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.i(TAG, "onLoadingChanged")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + playWhenReady.toString()
                        + " playbackState = " + playbackState)
                when (playbackState) {
                    ExoPlayer.STATE_ENDED -> {
                        Log.i(TAG, "Playback ended!")
                        //Stop playback and return to start position
                        setPlayPause(false)
                        exoPlayer!!.seekTo(0)
                    }
                    ExoPlayer.STATE_READY -> radioPlayer.stopRadioNoize()
                    ExoPlayer.STATE_BUFFERING -> Log.i(TAG, "Playback buffering!")
                    ExoPlayer.STATE_IDLE -> Log.i(TAG, "ExoPlayer idle!")
                }//Log.i(TAG,"ExoPlayer ready! pos: "+exoPlayer.getCurrentPosition()
                //        +" max: "+stringForTime((int)exoPlayer.getDuration()));
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                Log.i(TAG, "onPlaybackError: " + error.message)
            }

            override fun onPositionDiscontinuity() {
                Log.i(TAG, "onPositionDiscontinuity")
            }
        }
        //private val mpNoize: MediaPlayer = MediaPlayer.create(applicationContext, R.raw.radionoise)

        override fun Pause() {
            setPlayPause(false)
        }

        override fun TuneIn() {
            // Play TuneIn noice sound
            //mpNoize!!.isLooping = true
            //radioPlayer.startRadioNoize()

            // ExoPlayer
            prepareExoPlayerFromURL(Uri.parse(getString(R.string.classic_radiostream_path)))
            setPlayPause(true)
            MyApp.tunedIn = true
        }

        override fun UpdatePlaylist() {

        }

        private fun setPlayPause(play: Boolean) {
            exoPlayer!!.playWhenReady = play
            MyApp.setIsPlaying(play)
        }

        private fun prepareExoPlayerFromURL(uri: Uri) {

            val trackSelector = DefaultTrackSelector()

            val loadControl = DefaultLoadControl()

            exoPlayer = ExoPlayerFactory.newSimpleInstance(this@MediaPlayerService, trackSelector, loadControl)

            val dataSourceFactory = DefaultDataSourceFactory(this@MediaPlayerService, Util.getUserAgent(this@MediaPlayerService, "exoplayer2example"), null)
            val extractorsFactory = DefaultExtractorsFactory()
            val audioSource = ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null)
            exoPlayer!!.addListener(eventListener)

            exoPlayer!!.prepare(audioSource)
        }

    }
*/
    companion object {
        private val TAG = "MediaPlayerService"
        var IS_SERVICE_RUNNING = false
    }

}
