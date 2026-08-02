package com.radionula.radionula

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.core.util.AndroidConnectivityMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class AndroidConnectivityMonitorTest {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var capabilities: NetworkCapabilities
    private lateinit var monitor: AndroidConnectivityMonitor

    @Before
    fun before() {
        connectivityManager = mock()
        capabilities = mock()
        val network = mock<Network>()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        whenever(connectivityManager.getNetworkCapabilities(network)).thenReturn(capabilities)

        monitor = AndroidConnectivityMonitor(connectivityManager)
    }

    @Test
    fun `emits true straight away when the active network has internet`() = runTest {
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .thenReturn(true)

        assertTrue(monitor.isOnline.first())
    }

    @Test
    fun `emits false straight away when the active network has no internet`() = runTest {
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .thenReturn(false)

        assertFalse(monitor.isOnline.first())
    }

    /** The callback outlives the collector otherwise, which is what onInactive used to prevent. */
    @Test
    fun `unregisters the callback when collection ends`() = runTest {
        whenever(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            .thenReturn(true)

        monitor.isOnline.first()

        verify(connectivityManager).unregisterNetworkCallback(
            any<ConnectivityManager.NetworkCallback>()
        )
    }
}
