package com.radionula.radionula.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radionula.radionula.R
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.CLASSIC_ART
import com.radionula.radionula.radio.PlayerUiState
import com.radionula.radionula.ui.components.SectionHeader
import com.radionula.radionula.ui.theme.AdapterFavoriteBackground
import com.radionula.radionula.ui.theme.BackgroundBlue
import com.radionula.radionula.ui.theme.Brown
import com.radionula.radionula.ui.theme.FadedWhite
import com.radionula.radionula.ui.theme.NulaTheme
import com.radionula.radionula.ui.theme.NulaWhite
import com.radionula.radionula.ui.theme.Roboto
import com.radionula.radionula.ui.theme.RobotoLight

/**
 * fragment_player.xml.
 *
 * The old layout stacked three siblings in a FrameLayout: a two-weight column
 * holding the turntable and the playlist, then a full-size centred row of
 * playback controls, then a full-size centred tune-in button. Because both
 * overlays are centred in the whole screen, the buttons land exactly on the
 * seam between the two halves - which is the look, not an accident.
 */
@Composable
fun PlayerScreen(
    state: PlayerUiState,
    onTuneIn: () -> Unit,
    onSkip: () -> Unit,
    onPause: () -> Unit,
    onAddFavorite: (NulaTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(Brown)) {
        Column(Modifier.fillMaxSize()) {
            Turntable(
                cover = state.cover,
                logo = state.channelArt.logo,
                spinning = state.isPlaying,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
            Playlist(
                tracks = state.tracks,
                onAddFavorite = onAddFavorite,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }

        if (!state.showTuneIn) {
            Row(
                Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(state.channelArt.skip),
                    contentDescription = "Skip to next",
                    modifier = Modifier.width(130.dp).height(80.dp).clickable(onClick = onSkip),
                )
                Spacer(Modifier.width(10.dp))
                Image(
                    painter = painterResource(state.channelArt.pause),
                    contentDescription = "Pause",
                    modifier = Modifier.width(130.dp).height(80.dp).clickable(onClick = onPause),
                )
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.tune_in),
                    contentDescription = "Tune in",
                    modifier = Modifier.width(250.dp).clickable(onClick = onTuneIn),
                )
            }
        }
    }
}

/** fragment_player.xml's negative margins on the turntable's inner box. */
private val SIDE_OVERHANG = 75.dp
private val BOTTOM_OVERHANG = 275.dp

/**
 * The record sat in a box inset by -75dp left and right and -275dp below, so it
 * overflows the top half on three sides and is clipped by it. The cover is
 * centred in that oversized box, which is why it hangs off the bottom edge.
 */
@Composable
private fun Turntable(
    cover: String,
    logo: Int,
    spinning: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.clipToBounds()) {
        val (wobble, spin) = vinylRotation(spinning)

        Box(
            Modifier
                // required*, not width/height: the box is deliberately larger
                // than its parent, and plain size modifiers would be coerced
                // back into the parent's constraints and shrink the record.
                // They also centre the overflow, which is already right
                // horizontally - the XML's -75dp side margins were symmetric.
                .requiredWidth(maxWidth + SIDE_OVERHANG * 2)
                .requiredHeight(maxHeight + BOTTOM_OVERHANG)
                // Vertically the overhang was bottom-only, so undo half of the
                // centring to put the box's top back on the parent's top.
                .offset(y = BOTTOM_OVERHANG / 2)
        ) {
            Image(
                painter = painterResource(R.drawable.record),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(wobble),
            )
            AsyncImage(
                model = cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
                    .rotate(spin)
                    .clip(CircleShape),
            )
        }

        // Drawn last, which is what the fragment's bringToFront() was for.
        Image(
            painter = painterResource(logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopStart,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Returns the record's and the cover's rotation.
 *
 * The transition is only composed while playing: stopVinyl() used to cancel the
 * View animations, and an always-running infinite transition would both keep
 * drawing frames behind a paused radio and never let a Compose test go idle.
 */
@Composable
private fun vinylRotation(spinning: Boolean): Pair<Float, Float> {
    if (!spinning) return 0f to 0f

    val transition = rememberInfiniteTransition(label = "vinyl")
    // RotateAnimation(0f, 1f) reversing every 50ms - a wobble, not a spin.
    val wobble by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wobble",
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )
    return wobble to spin
}

@Composable
private fun Playlist(
    tracks: List<NulaTrack>,
    onAddFavorite: (NulaTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(BackgroundBlue)) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        LazyColumn(Modifier.fillMaxSize()) {
            if (tracks.isNotEmpty()) {
                item { SectionHeader("NOW PLAYING") }
                item { TrackRow(tracks.first(), onAddFavorite) }
                if (tracks.size > 1) {
                    item { SectionHeader("PLAYLIST HISTORY") }
                    items(tracks.drop(1)) { TrackRow(it, onAddFavorite) }
                }
            }
        }
        // Sits above the list, like the ConstraintLayout's last child did.
        Image(
            painter = painterResource(R.drawable.shadow),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopStart,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * item_playlist_track.xml plus its add_favorit.xml include: tapping the row
 * reveals a scrim with a heart, tapping the scrim dismisses it again.
 */
@Composable
private fun TrackRow(
    track: NulaTrack,
    onAddFavorite: (NulaTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    // rememberSaveable keyed on the row's identity, so recycling a row while
    // scrolling cannot carry another track's open scrim with it.
    var showAdd by rememberSaveable(track.artist, track.title) { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clickable { showAdd = true }) {
            Row(Modifier.padding(start = 12.dp, top = 12.dp)) {
                AsyncImage(
                    model = track.image,
                    contentDescription = "Album cover",
                    modifier = Modifier.size(75.dp).padding(2.dp),
                )
                Column(Modifier.padding(start = 16.dp, top = 16.dp)) {
                    Text(
                        text = track.artist,
                        color = Brown,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = Roboto,
                    )
                    Text(
                        text = track.title,
                        color = NulaWhite,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = RobotoLight,
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

        if (showAdd) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(AdapterFavoriteBackground)
                    .clickable { showAdd = false }
            ) {
                Image(
                    painter = painterResource(R.drawable.ico_favorite),
                    contentDescription = "Add to favorite",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 30.dp)
                        .size(50.dp)
                        .clickable {
                            onAddFavorite(track)
                            showAdd = false
                        },
                )
            }
        }
    }
}

@Preview(widthDp = 411, heightDp = 850)
@Composable
private fun PlayerScreenTunedOutPreview() = NulaTheme {
    PlayerScreen(PlayerUiState(), {}, {}, {}, {})
}

@Preview(widthDp = 411, heightDp = 850)
@Composable
private fun PlayerScreenPlayingPreview() = NulaTheme {
    PlayerScreen(
        state = PlayerUiState(
            showTuneIn = false,
            isPlaying = true,
            tracks = listOf(
                NulaTrack("Session Victim", "Taste Of Life", ""),
                NulaTrack("The Experience", "Of The Unaligned", ""),
            ),
            channelArt = CLASSIC_ART,
        ),
        onTuneIn = {}, onSkip = {}, onPause = {}, onAddFavorite = {},
    )
}
