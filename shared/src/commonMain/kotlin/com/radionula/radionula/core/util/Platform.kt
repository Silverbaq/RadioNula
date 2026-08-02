package com.radionula.radionula.core.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * The three things this project needs from the platform that Kotlin common does
 * not provide. Kept in one file so the list stays visible and short.
 */

/**
 * Where blocking work goes - the sqlite driver's calls, and the feed fetch.
 *
 * Not `Dispatchers.IO` directly: it is JVM-only. On Kotlin/Native (checked
 * against coroutines 1.10.2 and 1.11.0) `Dispatchers.IO` is `internal`, so
 * naming it in commonMain does not compile once an iOS target exists.
 */
expect val ioDispatcher: CoroutineDispatcher

/** Error logging. On Android this is the same Log.e output as before. */
expect fun logError(tag: String, message: String, cause: Throwable? = null)

/**
 * Wall-clock milliseconds since the epoch, for the feed's cache-buster query.
 *
 * kotlin.time.Clock would cover this in common code, but it is still an
 * experimental stdlib API - an expect fun avoids the opt-in and the churn when
 * it stabilises.
 */
expect fun epochMillis(): Long
