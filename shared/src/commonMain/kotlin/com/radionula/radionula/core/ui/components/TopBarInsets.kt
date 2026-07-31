package com.radionula.radionula.core.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable

/**
 * The inset the top bar has to leave for the system.
 *
 * Per-platform because the two windows are not the same: Android hides the
 * status bar but the layout has always reserved its full height, and iOS has a
 * safe area that must be respected.
 */
@Composable
expect fun topBarInsets(): WindowInsets
