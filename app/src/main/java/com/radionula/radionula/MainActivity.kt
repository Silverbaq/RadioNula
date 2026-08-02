package com.radionula.radionula

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.radionula.radionula.core.ui.NulaApp
import com.radionula.radionula.core.ui.theme.NulaTheme

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Drops the SplashTheme window background used for the cold-start window.
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar_FullScreen)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // The app has always run with the status bar hidden - that came from
        // android:windowFullscreen, which no longer takes effect once the
        // content is Compose. Asking the insets controller directly is the
        // supported way to get the same window back.
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.statusBars())
        }

        setContent {
            NulaTheme { NulaApp() }
        }
    }
}
