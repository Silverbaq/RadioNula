package com.radionula.radionula.core.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable

/**
 * IgnoringVisibility: the status bar is hidden, but fitsSystemWindows still
 * reserved its full height for the old toolbar, and the app's whole vertical
 * rhythm was built on that. The cutout is unioned in for devices where it is
 * the taller of the two.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun topBarInsets(): WindowInsets =
    WindowInsets.statusBarsIgnoringVisibility.union(WindowInsets.displayCutout)
