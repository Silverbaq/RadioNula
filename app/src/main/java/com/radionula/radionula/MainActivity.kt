package com.radionula.radionula

import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.radionula.radionula.databinding.ActivityMainBinding
import com.radionula.radionula.util.ConnectivityLiveData

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var connectionData: ConnectivityLiveData

    private val navController: NavController
        get() = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.activityMainToolbar.navButton.setOnClickListener {
            binding.drawerLayout.openDrawer(
                GravityCompat.START
            )
        }

        setupDrawerContent(binding.nvView)

        connectionData =
            ConnectivityLiveData(this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)

        // The nav host is declared in the layout and stays mounted for the life
        // of the activity: the no-connection state is only an overlay on top of
        // it, so the NavController and the back stack survive going offline.
        connectionData.observe(this) { connected ->
            binding.noConnectionOverlay.root.isVisible = !connected
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    fun selectDrawerItem(menuItem: MenuItem) {
        // Navigating by destination works from any current destination. The
        // actions do not - they are declared on radioFragment only, so using
        // them from e.g. the favourites screen throws.
        when (menuItem.itemId) {
            R.id.nav_Radio_Player -> navController.popBackStack(R.id.radioFragment, false)
            R.id.nav_Favorites -> navController.navigate(R.id.favoritesFragment)
            R.id.nav_Comments -> navController.navigate(R.id.commentsFragment)
            else -> Unit
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }
}
