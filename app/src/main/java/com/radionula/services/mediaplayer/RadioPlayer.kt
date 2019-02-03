package com.radionula.services.mediaplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.radionula.radionula.MyApp
import com.radionula.radionula.R


class RadioPlayer(private val context: Context) {

    private val mpNoize: MediaPlayer = MediaPlayer.create(context, R.raw.radionoise)
    private lateinit var exoPlayer: ExoPlayer

    init {
        mpNoize.isLooping = true
    }

    private fun prepareExoPlayerFromURL(uri: Uri) {

        val trackSelector = DefaultTrackSelector()
        val loadControl = DefaultLoadControl()

        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl)

        val bandwidthMeter = DefaultBandwidthMeter()
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayer2example"), bandwidthMeter)
        val extractorsFactory = DefaultExtractorsFactory()
        val audioSource = ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null)
        exoPlayer.addListener(eventListener)

        exoPlayer.prepare(audioSource)
    }


    private val eventListener = object : ExoPlayer.EventListener {
        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


        override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {}

        override fun onLoadingChanged(isLoading: Boolean) {
            Log.i(TAG, "onLoadingChanged")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + playWhenReady.toString()
                    + " playbackState = " + playbackState)
            when (playbackState) {
                ExoPlayer.STATE_ENDED -> {
                    //Stop playback and return to start position
                    //setPlayPause(false)
                    exoPlayer.seekTo(0)
                }
                ExoPlayer.STATE_READY -> stopRadioNoize()
                ExoPlayer.STATE_BUFFERING -> Log.i(TAG, "Playback buffering!")
                ExoPlayer.STATE_IDLE -> Log.i(TAG, "ExoPlayer idle!")
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.i(TAG, "onPlaybackError: " + error.message)
        }

        override fun onPositionDiscontinuity() {
            Log.i(TAG, "onPositionDiscontinuity")
        }
    }

    fun tuneIn(radioUrl: String) {
        startRadioNoize()

        // ExoPlayer
        prepareExoPlayerFromURL(Uri.parse(radioUrl))
        playRadio()
    }

    fun playRadio(){
        exoPlayer.playWhenReady = true
    }

    fun pauseRadio(){
        exoPlayer.playWhenReady = false
    }

    fun startRadioNoize(){
        mpNoize.start()
    }

    fun stopRadioNoize(){
        mpNoize.stop()
    }

    companion object {
        const val TAG = "RadioPlayer"
    }

}