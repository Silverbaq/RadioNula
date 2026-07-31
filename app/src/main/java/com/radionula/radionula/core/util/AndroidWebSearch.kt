package com.radionula.radionula.core.util

import android.app.SearchManager
import android.content.Context
import android.content.Intent

/** ACTION_WEB_SEARCH, so the query lands in whatever search app the user has. */
class AndroidWebSearch(private val context: Context) : WebSearch {
    override fun search(query: String) {
        context.startActivity(
            Intent(Intent.ACTION_WEB_SEARCH)
                .putExtra(SearchManager.QUERY, query)
                // Started from the application context, which has no task of its own.
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
