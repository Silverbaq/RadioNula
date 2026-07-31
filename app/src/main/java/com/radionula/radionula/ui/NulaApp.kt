package com.radionula.radionula.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.radionula.radionula.R
import com.radionula.radionula.favorits.FavoritesViewModel
import com.radionula.radionula.radio.ChannelPresenter
import com.radionula.radionula.radio.RadioViewModel
import com.radionula.radionula.ui.comments.CommentsScreen
import com.radionula.radionula.ui.components.NoConnectionOverlay
import com.radionula.radionula.ui.components.NulaTopBar
import com.radionula.radionula.ui.favorites.FavoritesScreen
import com.radionula.radionula.ui.player.PlayerScreen
import com.radionula.radionula.ui.theme.BackgroundBlue
import com.radionula.radionula.ui.theme.FadedWhite
import com.radionula.radionula.ui.theme.NulaTheme
import com.radionula.radionula.ui.theme.NulaWhite
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect

private const val ROUTE_PLAYER = "player"
private const val ROUTE_FAVORITES = "favorites"
private const val ROUTE_COMMENTS = "comments"

/**
 * activity_main.xml: a drawer, a toolbar above the nav host, and the
 * no-connection view as an overlay on top of the host rather than a
 * replacement for it - swapping the host out used to destroy the back stack.
 */
@Composable
fun NulaApp(
    connected: Boolean,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun closeDrawer() = scope.launch { drawerState.close() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NulaDrawerSheet(
                onPlayer = {
                    // Navigating by destination works from anywhere; the old
                    // nav actions were declared on radioFragment only.
                    navController.popBackStack(ROUTE_PLAYER, inclusive = false)
                    closeDrawer()
                },
                onFavorites = {
                    navController.navigate(ROUTE_FAVORITES)
                    closeDrawer()
                },
                onComments = {
                    navController.navigate(ROUTE_COMMENTS)
                    closeDrawer()
                },
            )
        },
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxSize().background(BackgroundBlue)) {
            NulaTopBar(onNavClick = { scope.launch { drawerState.open() } })
            Box(Modifier.fillMaxSize()) {
                NavHost(navController, startDestination = ROUTE_PLAYER) {
                    composable(ROUTE_PLAYER) { PlayerRoute() }
                    composable(ROUTE_FAVORITES) { FavoritesRoute() }
                    composable(ROUTE_COMMENTS) {
                        val channelPresenter = koinInject<ChannelPresenter>()
                        CommentsScreen(channelPresenter.currentChannel)
                    }
                }
                if (!connected) NoConnectionOverlay()
            }
        }
    }
}

@Composable
private fun PlayerRoute(viewModel: RadioViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.favoriteAdded.collect { title ->
            Toast.makeText(context, "Added $title to favorites", Toast.LENGTH_LONG).show()
        }
    }

    PlayerScreen(
        state = state,
        onTuneIn = {
            viewModel.tuneIn()
            viewModel.autoFetchPlaylist()
        },
        onSkip = viewModel::nextChannel,
        onPause = viewModel::pauseRadio,
        onAddFavorite = viewModel::addFavoriteClicked,
    )
}

@Composable
private fun FavoritesRoute(viewModel: FavoritesViewModel = koinViewModel()) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    FavoritesScreen(tracks = tracks, onRemove = viewModel::remove)
}

/** nav_header.xml plus the draw_view.xml menu, which was three single-item groups. */
@Composable
private fun NulaDrawerSheet(
    onPlayer: () -> Unit,
    onFavorites: () -> Unit,
    onComments: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = BackgroundBlue,
        // The NavigationView drew under the status bar; the M3 sheet insets
        // itself by default, which pushed the whole menu down.
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.width(200.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Image(
                    painter = painterResource(R.drawable.nula_logo_info),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            DrawerItem("RADIO PLAYER", onPlayer)
            DrawerItem("FAVORITES", onFavorites)
            DrawerItem("COMMENTS", onComments)

            Box(
                Modifier.fillMaxSize().padding(bottom = 20.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Text("radionula@gmail.com", color = NulaWhite)
            }
        }
    }
}

@Composable
private fun DrawerItem(label: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(label, color = NulaWhite)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(FadedWhite))
    }
}

@Preview(widthDp = 200, heightDp = 700)
@Composable
private fun NulaDrawerSheetPreview() = NulaTheme {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
        NulaDrawerSheet({}, {}, {})
    }
}
