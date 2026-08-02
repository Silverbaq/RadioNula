package com.radionula.radionula.features.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.radionula.radionula.resources.Res
import com.radionula.radionula.resources.background
import com.radionula.radionula.resources.ico_delete
import com.radionula.radionula.resources.ico_search
import com.radionula.radionula.domain.model.NulaTrack
import com.radionula.radionula.core.ui.theme.AdapterFavoriteBackground
import com.radionula.radionula.core.ui.theme.Brown
import com.radionula.radionula.core.ui.theme.FadedWhite
import com.radionula.radionula.core.ui.theme.NulaTheme
import com.radionula.radionula.core.ui.theme.NulaWhite

/** fragment_favorits.xml and its adapter_playlist.xml rows. */
@Composable
fun FavoritesScreen(
    tracks: List<NulaTrack>,
    onRemove: (NulaTrack) -> Unit,
    onSearch: (NulaTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Column(Modifier.fillMaxSize()) {
            Text(
                text = "FAVORITES",
                color = NulaWhite,
                modifier = Modifier.padding(start = 10.dp, top = 16.dp),
            )
            Box(
                Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FadedWhite)
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(tracks, key = { it.id }) { track ->
                    FavoriteRow(
                        track = track,
                        onRemove = { onRemove(track) },
                        onSearch = { onSearch(track) },
                    )
                }
            }
        }
    }
}

/**
 * remove_favorit.xml used to be inflated into the row's container on every tap
 * and never removed on rebind, so recycled rows stacked scrims. It is state now.
 */
@Composable
private fun FavoriteRow(
    track: NulaTrack,
    onRemove: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showActions by rememberSaveable(track.id) { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clickable { showActions = !showActions }) {
            Row(Modifier.padding(start = 12.dp, top = 12.dp)) {
                AsyncImage(
                    model = track.image,
                    contentDescription = "Cover image",
                    modifier = Modifier.size(75.dp).padding(2.dp),
                )
                Column(Modifier.padding(start = 16.dp, top = 16.dp)) {
                    Text(
                        text = track.artist,
                        color = Brown,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = track.title,
                        color = NulaWhite,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Box(
                Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(FadedWhite)
            )
        }

        if (showActions) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(AdapterFavoriteBackground)
                    .clickable { showActions = false }
            ) {
                Image(
                    painter = painterResource(Res.drawable.ico_search),
                    contentDescription = "Search",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 89.dp)
                        .size(30.dp)
                        .clickable(onClick = onSearch),
                )
                Image(
                    painter = painterResource(Res.drawable.ico_delete),
                    contentDescription = "Remove from favorite",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 30.dp)
                        .size(30.dp)
                        .clickable(onClick = onRemove),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FavoritesScreenPreview() = NulaTheme {
    FavoritesScreen(
        tracks = listOf(
            NulaTrack(
                "Session Victim",
                "Taste Of Life",
                "",
                id = 1
            ),
            NulaTrack(
                "The Experience",
                "Of The Unaligned",
                "",
                id = 2
            ),
        ),
        onRemove = {},
        onSearch = {},
    )
}
