package com.radionula.radionula.core.util

/**
 * Hands a track title to whatever the platform searches the web with.
 *
 * An interface resolved through Koin, not expect/actual, because the Android
 * implementation needs a Context - same as [ConnectivityMonitor].
 */
fun interface WebSearch {
    fun search(query: String)
}
