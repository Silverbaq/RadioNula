package com.radionula.radionula

import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.util.ConnectivityLiveData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ConnectionLiveDataTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var activeNetwork: NetworkInfo
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectionLiveData: ConnectivityLiveData

    @Before
    fun before() {
        connectivityManager = mock()
        activeNetwork = mock()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(activeNetwork)
        doNothing().whenever(connectivityManager).unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())

        connectionLiveData = ConnectivityLiveData(connectivityManager)
    }

    @Test
    fun currentlyConnected__observe__receiveTrue() {
        whenever(activeNetwork.isConnected).thenReturn(true)

        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        verify(observer).onChanged(true)
    }

    @Test
    fun currentlyNotConnected__observe__receiveFalse() {
        whenever(activeNetwork.isConnected).thenReturn(false)

        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        verify(observer).onChanged(false)
    }

    /*
    @Test
    fun currentlyConnected__becomeConnected__receiveTrue() {
        whenever(activeNetwork.isConnected).thenReturn(true)

        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        verify(observer).onChanged(true)
        reset(observer)

        captureNetworkCallback()

        networkCallback.onAvailable(mock())

        verify(observer).onChanged(true)
    }

    @Test
    fun currentlyNotConnected__becomeConnected__receiveTrue() {
        whenever(activeNetwork.isConnected).thenReturn(false)

        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        verify(observer).onChanged(false)
        reset(observer)

        captureNetworkCallback()

        networkCallback.onAvailable(mock())

        verify(observer).onChanged(true)
    }


    @Test
    fun currentlyConnected__becomeNotConnected__receiveFalse() {
        whenever(activeNetwork.isConnected).thenReturn(true)

        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        verify(observer).onChanged(true)
        reset(observer)

        captureNetworkCallback()

        networkCallback.onLost(mock())

        verify(observer).onChanged(false)
    }

    @Test
    fun currentlyNotConnected__becomeNotConnected__receiveFalse() {
        whenever(activeNetwork.isConnected).thenReturn(false)

        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        verify(observer).onChanged(false)
        reset(observer)

        captureNetworkCallback()

        networkCallback.onLost(mock())

        verify(observer).onChanged(false)
    }

    @Test
    fun nothing__becomeActive__registerCallback() {
        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)

        captureNetworkCallback()
    }
*/
    @Test
    fun nothing__becomeInactive__registerCallback() {
        val observer = mock<Observer<Boolean>>()
        connectionLiveData.observeForever(observer)
        connectionLiveData.removeObserver(observer)

        verify(connectivityManager).unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
    }

}