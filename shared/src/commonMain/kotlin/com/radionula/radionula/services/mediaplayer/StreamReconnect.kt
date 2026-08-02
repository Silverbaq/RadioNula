package com.radionula.radionula.services.mediaplayer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Retries a stream that died, once there is a network to retry on.
 *
 * A live stream that drops - airplane mode, a tunnel, a station restart - leaves
 * both players in an error state they never leave on their own, so the listener
 * had to tap the transport controls again. Both platforms report that error
 * differently but recover the same way, so the policy lives here.
 *
 * Two things gate the retry. The delay is the floor: a retry that fails reports a
 * new error and schedules the next one, so without it a station that is simply
 * down would spin. The timeout covers an error that was never about connectivity
 * - the device is online, [isOnline] has nothing new to say, and the retry has to
 * happen anyway.
 */
class StreamReconnect(
    private val isOnline: Flow<Boolean>,
    private val scope: CoroutineScope,
    private val retry: () -> Unit,
) {
    private var job: Job? = null

    /** Ignored while a retry is already pending, so a burst of errors is one retry. */
    fun schedule() {
        if (job?.isActive == true) return
        job = scope.launch {
            delay(RETRY_DELAY_MILLIS)
            // firstOrNull, not first: a monitor whose flow has completed would
            // otherwise throw here instead of letting the retry through.
            withTimeoutOrNull(WAIT_FOR_NETWORK_MILLIS) { isOnline.firstOrNull { it } }
            retry()
        }
    }

    /** The listener pressed pause, or was interrupted - do not resurrect playback. */
    fun cancel() {
        job?.cancel()
        job = null
    }

    private companion object {
        const val RETRY_DELAY_MILLIS = 5_000L
        const val WAIT_FOR_NETWORK_MILLIS = 30_000L
    }
}
