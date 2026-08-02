package com.radionula.radionula.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

/** The notch and the status bar, which iOS never lets an app draw under. */
@Composable
actual fun topBarInsets(): WindowInsets = WindowInsets.safeDrawing
