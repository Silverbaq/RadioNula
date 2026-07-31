package com.radionula.radionula.core.util

import kotlinx.coroutines.flow.Flow

/**
 * Whether the device has a network, for the no-connection overlay.
 *
 * Resolved through Koin rather than expect/actual, for the same reason
 * MediaPlayerController is: the Android implementation needs a Context.
 */
interface ConnectivityMonitor {
    /** Emits the current state on collection, then every change. */
    val isOnline: Flow<Boolean>
}
