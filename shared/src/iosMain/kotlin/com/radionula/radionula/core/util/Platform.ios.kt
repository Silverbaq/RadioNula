package com.radionula.radionula.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.Foundation.NSDate
import platform.Foundation.NSLog
import platform.Foundation.timeIntervalSince1970

/**
 * Native has no separate IO pool exposed publicly - Default is a real
 * multi-threaded pool, which is what the blocking sqlite calls need.
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default

actual fun logError(tag: String, message: String, cause: Throwable?) {
    // %s and not the message itself: a stream title or feed body could contain
    // a % and NSLog would read it as a format specifier.
    if (cause == null) NSLog("%s: %s", tag, message)
    else NSLog("%s: %s (%s)", tag, message, cause.toString())
}

actual fun epochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
