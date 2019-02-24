package com.radionula.radionula

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView
import com.radionula.radionula.comments.CommentsFragment
import com.radionula.radionula.favorits.FavoritsFragment
import com.radionula.radionula.networkavaliable.NoConnectionFragment
import com.radionula.radionula.networkavaliable.ConnectionViewModel
import com.radionula.radionula.radio.PlayerFragment
import com.radionula.radionula.util.PhoneStateLiveData
import com.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    // Fragments
    val playerFragment: PlayerFragment = PlayerFragment()
    val favoritsFragment: FavoritsFragment = FavoritsFragment()
    val commentsFragment: CommentsFragment = CommentsFragment()

    val connectionView: ConnectionViewModel by viewModel()

    // Mediaplayer
    private val mediaplayerPresenter: MediaplayerPresenter by inject()
    private var mWakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        nav_Button.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }

        setupDrawerContent(nvView)

        //
        // Call State
        PhoneStateLiveData(
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        ).observe(this, Observer { idle -> if (!idle) pause() })

        //
        // Network state
        connectionView.connectionData.observe(this, Observer { connection ->
            if (!connection) {
                replaceFragment(NoConnectionFragment())
            } else {
                replaceFragment(playerFragment)
            }
        })

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

        //networkStateReceiver?.removeListener(this)

        // Makes sure the music keeps playing after the screen is off.
        try {
            mWakeLock!!.acquire()
            if (mediaplayerPresenter.isPlaying())
                playerFragment.StartVinyl()
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

    fun pause() {
        playerFragment.StopVinyl()
        mediaplayerPresenter.pauseRadio()
    }

    fun tuneIn() {
        mediaplayerPresenter.tuneIn()
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

    companion object {
        private val TAG = "MainActivity"
    }

}
