package com.radionula;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.radionula.radionula.MainActivity;
import com.radionula.radionula.interfaces.IControls;
import com.radionula.radionula.MyApp;
import com.radionula.radionula.R;
import com.radionula.radionula.model.Constants;

public class MediaPlayerService extends Service {
    private static final String TAG = "MediaPlayerService";
    public static boolean IS_SERVICE_RUNNING = false;


    private AudioPlayer audioPlayer= new AudioPlayer();

    public MediaPlayerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Start Foreground Intent ");
            showNotification();
            audioPlayer.TuneIn();
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            audioPlayer.Pause();
            stopSelf();
        }

        /*
        else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
            Log.i(TAG, "Clicked Previous");

            Toast.makeText(this, "Clicked Previous!", Toast.LENGTH_SHORT)
                    .show();
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(TAG, "Clicked Play");

            Toast.makeText(this, "Clicked Play!", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(TAG, "Clicked Next");

            Toast.makeText(this, "Clicked Next!", Toast.LENGTH_SHORT).show();
        }
        */
        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        /*
        Intent pauseIntent = new Intent(this, MediaPlayerService.class);
        pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
        PendingIntent ppauseIntent = PendingIntent.getService(this, 0,
                pauseIntent, 0);

        Intent playIntent = new Intent(this, MediaPlayerService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MediaPlayerService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);
        */

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ico_favorite);

        Notification notification = new NotificationCompat.Builder(this, "my_channel")
                .setContentTitle("Radio Nula")
                .setTicker("Radio Nula")
                //.setContentText("My song")
                .setSmallIcon(R.drawable.ico_favorite)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
                //.addAction(android.R.drawable.ic_media_pause, "Pause",
                //        ppauseIntent)
                //.addAction(android.R.drawable.ic_media_play, "Play",
                //        pplayIntent)
                //.addAction(android.R.drawable.ic_media_next, "Next",
                //        pnextIntent).build();
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    class AudioPlayer implements IControls {
        private ExoPlayer exoPlayer;

        private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.i(TAG, "onLoadingChanged");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady)
                        + " playbackState = " + playbackState);
                switch (playbackState) {
                    case ExoPlayer.STATE_ENDED:
                        Log.i(TAG, "Playback ended!");
                        //Stop playback and return to start position
                        setPlayPause(false);
                        exoPlayer.seekTo(0);
                        break;
                    case ExoPlayer.STATE_READY:
                        mpNoize.stop();

                        //Log.i(TAG,"ExoPlayer ready! pos: "+exoPlayer.getCurrentPosition()
                        //        +" max: "+stringForTime((int)exoPlayer.getDuration()));

                        break;
                    case ExoPlayer.STATE_BUFFERING:
                        Log.i(TAG, "Playback buffering!");
                        break;
                    case ExoPlayer.STATE_IDLE:
                        Log.i(TAG, "ExoPlayer idle!");
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.i(TAG, "onPlaybackError: " + error.getMessage());
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.i(TAG, "onPositionDiscontinuity");
            }
        };
        private MediaPlayer mpNoize;

        @Override
        public void Pause() {
            setPlayPause(false);
        }

        @Override
        public void TuneIn() {
            // Play TuneIn noice sound
            mpNoize = MediaPlayer.create(getApplicationContext(), R.raw.radionoise);
            mpNoize.setLooping(true);
            mpNoize.start();

            // ExoPlayer
            prepareExoPlayerFromURL(Uri.parse(getString(R.string.classic_radiostream_path)));
            setPlayPause(true);
            MyApp.tunedIn = true;
        }

        @Override
        public void UpdatePlaylist() {

        }

        private void setPlayPause(boolean play) {
            exoPlayer.setPlayWhenReady(play);
            MyApp.isPlaying = play;
        }

        private void prepareExoPlayerFromURL(Uri uri) {

            TrackSelector trackSelector = new DefaultTrackSelector();

            LoadControl loadControl = new DefaultLoadControl();

            exoPlayer = ExoPlayerFactory.newSimpleInstance(MediaPlayerService.this, trackSelector, loadControl);

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(MediaPlayerService.this, Util.getUserAgent(MediaPlayerService.this, "exoplayer2example"), null);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource audioSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
            exoPlayer.addListener(eventListener);

            exoPlayer.prepare(audioSource);
        }

    }

}
