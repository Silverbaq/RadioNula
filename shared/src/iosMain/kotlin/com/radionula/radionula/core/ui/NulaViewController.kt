package com.radionula.radionula.core.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.radionula.radionula.core.ui.theme.NulaTheme
import platform.UIKit.UIViewController

/** The whole app, as one view controller for Swift to present. */
fun NulaViewController(): UIViewController = ComposeUIViewController {
    NulaTheme { NulaApp() }
}
