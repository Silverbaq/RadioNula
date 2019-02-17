package com.radionula.radionula

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.radionula.radionula.fragments.CommentsFragment
import com.radionula.radionula.favorits.FavoritsFragment
import com.radionula.radionula.fragments.NoConnectionFragment
import com.radionula.radionula.model.NetworkStateReceiver
import com.radionula.radionula.model.NetworkStateReceiver.NetworkStateReceiverListener
import com.radionula.radionula.radio.PlayerFragment
import com.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : BaseActivity(), NetworkStateReceiverListener {

    // Fragments
    val playerFragment: PlayerFragment = PlayerFragment()
    val favoritsFragment: FavoritsFragment = FavoritsFragment()
    val commentsFragment: CommentsFragment = CommentsFragment()

    private var networkStateReceiver: NetworkStateReceiver? = null

    // Mediaplayer
    private var mediaplayerPresenter: MediaplayerPresenter? = null
    private var mWakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        nav_Button.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }

        mediaplayerPresenter = MediaplayerPresenter(this)

        replaceFragment(playerFragment)

        setupDrawerContent(nvView)

        //
        // Call State
        val TelephonyMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        TelephonyMgr.listen(TeleListener(),
                PhoneStateListener.LISTEN_CALL_STATE)

        //
        // Network state
        networkStateReceiver = NetworkStateReceiver(this)
        networkStateReceiver?.addListener(this)
        this.registerReceiver(networkStateReceiver, IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION))

        //
        // WakeLock
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, "$TAG:$TAG")

    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    fun selectDrawerItem(menuItem: MenuItem) {
        transaction = supportFragmentManager.beginTransaction()

        // Create a new fragment and specify the planet to show based on
        // position
        when (menuItem.itemId) {
            R.id.nav_Radio_Player -> {
                replaceFragment(playerFragment)

                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_Favorites -> {
                replaceFragment(favoritsFragment)

                drawer_layout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_Comments -> {
                replaceFragment(commentsFragment)

                drawer_layout.closeDrawer(GravityCompat.START)
            }
            else -> {
            }
        }
    }

    override fun onPause() {
        super.onPause()

        networkStateReceiver?.removeListener(this)

        // Makes sure the music keeps playing after the screen is off.
        try {
            mWakeLock!!.acquire()
        } catch (ex: Exception) {

        }

    }

    //
    // Make sure this is the method with just `Bundle` as the signature
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
    }


    override fun onResume() {
        super.onResume()

        // If screen is backon while music is playing, release the lock
        if (MyApp.isPlaying)
            try {
                mWakeLock!!.release()
            } catch (ex: Exception) {

            }
    }

    fun Pause() {
        playerFragment.StopVinyl()
        mediaplayerPresenter?.pauseRadio()
    }

    fun TuneIn() {
        mediaplayerPresenter?.tuneIn()
        playerFragment.StartVinyl()
    }

    /**
     * Back button listener.
     * Will close the application if the back button pressed twice.
     */
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    //
    // When the internet connection is reestablished
    override fun onNetworkAvailable() {
        try {
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            findViewById<View>(R.id.activityMain_toolbar).layoutParams = layoutParams
            //findViewById(R.id.activityMain_toolbar).setVisibility(View.VISIBLE);
            replaceFragment(playerFragment)

            if (MyApp.reconnect) {
                // Starts music player once agian.
                TuneIn()
                MyApp.reconnect = false
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

    }

    //
    // If there is no internet connection
    override fun onNetworkUnavailable() {
        try {
            val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
            findViewById<View>(R.id.activityMain_toolbar).layoutParams = layoutParams

            val fragment = NoConnectionFragment()
            replaceFragment(fragment)

        } catch (ex: Exception) {
            Log.e(TAG, ex.message)
        }

    }

    //
    // Detects the call state of the phone. Will pause music if phone rings.
    internal inner class TeleListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            super.onCallStateChanged(state, incomingNumber)
            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                }
                TelephonyManager.CALL_STATE_RINGING ->
                    // Pauses music
                    Pause()
                else -> {
                }
            }// TODO: Needs to restart music after a phone call. (If it was playing).
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }

}
