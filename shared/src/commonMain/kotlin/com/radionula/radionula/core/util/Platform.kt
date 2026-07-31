package com.radionula.radionula.core.util

/**
 * The two things this project needs from the platform that Kotlin common does
 * not provide. Kept in one file so the list stays visible and short.
 */

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
