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

    companion object {
        private val TAG = "MediaPlayerService"
        var IS_SERVICE_RUNNING = false
    }

}
