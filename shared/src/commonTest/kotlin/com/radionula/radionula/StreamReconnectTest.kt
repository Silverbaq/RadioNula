package com.radionula.radionula

import com.radionula.radionula.services.mediaplayer.StreamReconnect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * advanceTimeBy rather than advanceUntilIdle throughout: the 30s
 * wait-for-network timeout retries on its own, so idling the scheduler would
 * pass every one of these for the wrong reason.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StreamReconnectTest {

    @Test
    fun retries_once_the_network_comes_back() = runTest {
        val online = MutableStateFlow(false)
        var retries = 0
        val reconnect = StreamReconnect(online, this) { retries++ }

        reconnect.schedule()
        advanceTimeBy(10_000)
        assertEquals(0, retries, "retried while still offline")

        online.value = true
        runCurrent()
        assertEquals(1, retries)
    }

    @Test
    fun waits_before_the_first_retry() = runTest {
        val online = MutableStateFlow(true)
        var retries = 0
        val reconnect = StreamReconnect(online, this) { retries++ }

        reconnect.schedule()
        advanceTimeBy(4_000)
        assertEquals(0, retries, "a station that is down would be hammered")

        advanceTimeBy(2_000)
        assertEquals(1, retries)
    }

    @Test
    fun a_burst_of_errors_is_one_retry() = runTest {
        val online = MutableStateFlow(true)
        var retries = 0
        val reconnect = StreamReconnect(online, this) { retries++ }

        repeat(5) { reconnect.schedule() }
        advanceTimeBy(10_000)

        assertEquals(1, retries)
    }

    @Test
    fun retries_even_when_the_monitor_says_nothing() = runTest {
        var retries = 0
        val reconnect = StreamReconnect(emptyFlow(), this) { retries++ }

        reconnect.schedule()
        advanceUntilIdle()

        assertEquals(1, retries)
    }

    @Test
    fun cancel_stops_a_pending_retry() = runTest {
        val online = MutableStateFlow(false)
        var retries = 0
        val reconnect = StreamReconnect(online, this) { retries++ }

        reconnect.schedule()
        reconnect.cancel()
        online.value = true
        advanceUntilIdle()

        assertEquals(0, retries)
    }
}
