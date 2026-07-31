package com.radionula.radionula.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * The app has always been dark and has never followed the system palette, so
 * there is one scheme and no dynamic colour. Most of the surface is artwork
 * rather than tinted Material components - the scheme only has to cover the
 * drawer, the list text and the scrims.
 */
private val NulaColorScheme = darkColorScheme(
    primary = Brown,
    onPrimary = NulaWhite,
    background = BackgroundBlue,
    onBackground = NulaWhite,
    surface = BackgroundBlue,
    onSurface = NulaWhite,
    surfaceVariant = DarkBlue,
    onSurfaceVariant = NulaWhite,
)

@Composable
fun NulaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NulaColorScheme,
        typography = NulaTypography,
        content = content,
    )
}
