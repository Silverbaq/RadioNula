package com.radionula.radionula.core.util

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Requires ACCESS_NETWORK_STATE.
 *
 * The old ConnectivityLiveData registered in onActive and unregistered in
 * onInactive; callbackFlow's awaitClose is the same lifecycle, tied to the
 * collector instead of to LiveData's observer count.
 */
class AndroidConnectivityMonitor(
    private val connectivityManager: ConnectivityManager,
) : ConnectivityMonitor {

    override val isOnline: Flow<Boolean> = callbackFlow {
        // Seed it, so the overlay does not wait for the first network change.
        trySend(connectivityManager.activeNetwork?.let(::hasInternet) == true)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
        }
        // registerDefaultNetworkCallback is API 24; minSdk is 23.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
        }
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun hasInternet(network: Network): Boolean =
        connectivityManager.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
