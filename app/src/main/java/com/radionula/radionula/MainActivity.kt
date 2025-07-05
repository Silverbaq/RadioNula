package com.radionula.radionula

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.radionula.radionula.databinding.ActivityMainBinding
import com.radionula.radionula.networkavaliable.ConnectionViewModel
import com.radionula.radionula.networkavaliable.NoConnectionFragment
import com.radionula.services.mediaplayer.MediaplayerPresenter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    val connectionView: ConnectionViewModel by viewModel()

    private var mWakeLock: PowerManager.WakeLock? = null

    private lateinit var host: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityMainToolbar.navButton.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        setupDrawerContent(binding.nvView)

        // Navigation component
        host = NavHostFragment.create(R.navigation.nula_navigation)
        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, host)
                .setPrimaryNavigationFragment(host).commit()

        // Network state
        connectionView.connectionData.observe(this, Observer { connection ->
            if (!connection) {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, NoConnectionFragment())
                        .commit()
            } else {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, host)
                        .setPrimaryNavigationFragment(host).commit()
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
        // Create a new fragment and specify the planet to show based on
        // position
        when (menuItem.itemId) {
            R.id.nav_Radio_Player -> {
                host.navController.navigateUp()
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_Favorites -> {
                host.navController.navigateUp()
                host.navController.navigate(R.id.action_radioFragment_to_favoritesFragment, null)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_Comments -> {
                host.navController.navigateUp()
                host.navController.navigate(R.id.action_radioFragment_to_commentsFragment, null)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            else -> {
                // Do nothing
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Makes sure the music keeps playing after the screen is off.
        try {
            mWakeLock?.acquire()
        } catch (ex: Exception) {

        }

    }

    override fun onResume() {
        super.onResume()
            try {
                mWakeLock?.release()

            } catch (ex: Exception) {

            }
    }

    override fun onDestroy() {
        val playerPresenter: MediaplayerPresenter by inject()
        playerPresenter.pauseRadio()
        super.onDestroy()
    }

    companion object {
        private val TAG = "MainActivity"
    }

}
