package com.radionula.radionula.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

class PlaylistApiService(private val client: HttpClient) {

    /**
     * Fetches a "recently played" RSS feed, e.g. "recently_played_ch4.xml".
     * [cacheBuster] mirrors what radionula.com itself sends - the feeds are
     * static files and get cached aggressively otherwise.
     */
    suspend fun getPlaylist(xmlPath: String, cacheBuster: Long): String =
        client.get(xmlPath) {
            header(HttpHeaders.CacheControl, "no-store")
            parameter("t", cacheBuster)
        }.bodyAsText()
}

private const val BASE_URL = "https://radionula.com/"

fun nulaHttpClient(): HttpClient = HttpClient {
    // A non-2xx must throw rather than be returned as if it were a valid body -
    // PlaylistNetworkDataSourceImpl's catch-all turns that throw into the null
    // the repository relies on to ignore a failed fetch.
    expectSuccess = true
    defaultRequest { url(BASE_URL) }
}
