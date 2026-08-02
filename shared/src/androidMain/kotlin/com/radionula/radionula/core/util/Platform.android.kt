package com.radionula.radionula.core.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun logError(tag: String, message: String, cause: Throwable?) {
    if (cause == null) Log.e(tag, message) else Log.e(tag, message, cause)
}

actual fun epochMillis(): Long = System.currentTimeMillis()
