package com.radionula.radionula.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.radionula.radionula.resources.Res
import com.radionula.radionula.resources.background
import com.radionula.radionula.resources.nav
import com.radionula.radionula.resources.nula_intro_logo
import com.radionula.radionula.core.ui.theme.Brown
import com.radionula.radionula.core.ui.theme.FadedBlack
import com.radionula.radionula.core.ui.theme.FadedWhite
import com.radionula.radionula.core.ui.theme.NulaTheme
import com.radionula.radionula.core.ui.theme.NulaWhite

/**
 * toolbar.xml. The bar drew under the status bar via fitsSystemWindows, which
 * is what the inset padding reproduces now that the whole app is edge to edge.
 */
@Composable
fun NulaTopBar(onNavClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .background(Brown)
            .windowInsetsPadding(topBarInsets().only(WindowInsetsSides.Top))
            .heightIn(min = 56.dp)
    ) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                // The icon sat 16dp in (the Toolbar's content inset) and 10dp
                // down (its own margin). The 48dp box centres a 30dp icon, so
                // subtract that 9dp from each to keep the artwork put while
                // giving the control a real touch target.
                .padding(start = 16.dp - 9.dp, top = 10.dp - 9.dp)
                .size(48.dp)
                .clickable(onClick = onNavClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.nav),
                contentDescription = "Open navigation",
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

/** list_header.xml - the "NOW PLAYING" / "PLAYLIST HISTORY" separators. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 50.dp)
    ) {
        Text(text = text, color = NulaWhite)
        Box(
            Modifier
                .padding(top = 5.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(FadedWhite)
        )
    }
}

/**
 * fragment_no_connection.xml. Artwork, a scrim over it, and a small brown card -
 * dialog_shape.xml, which is a 5dp-radius brown rectangle with a 1dp dark border.
 */
@Composable
fun NoConnectionOverlay(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Image(
            painter = painterResource(Res.drawable.nula_intro_logo),
            contentDescription = null,
            modifier = Modifier.align(Alignment.Center),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(FadedBlack),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .width(250.dp)
                    .height(75.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Brown),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("There is no internet connection", color = Color.Black)
                Text("try re-connecting", color = Color.Black)
            }
        }
    }
}

@Preview
@Composable
private fun NulaTopBarPreview() = NulaTheme { NulaTopBar(onNavClick = {}) }

@Preview
@Composable
private fun SectionHeaderPreview() = NulaTheme { SectionHeader("NOW PLAYING") }

@Preview
@Composable
private fun NoConnectionOverlayPreview() = NulaTheme { NoConnectionOverlay() }
