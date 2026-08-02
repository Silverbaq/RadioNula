package com.radionula.radionula

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.data.PlaylistApiService
import com.radionula.radionula.data.network.PlaylistNetworkDataSourceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * [PlaylistNetworkDataSourceImpl] is the only thing standing between a failed
 * network call and [com.radionula.radionula.data.repository.PlaylistRepositoryImpl]
 * clearing the session or publishing a stale track - its contract is that
 * *any* failure returns null rather than throwing. A MockEngine stands in for
 * the real connection so that contract is pinned without a device.
 */
class PlaylistNetworkDataSourceImplTest {

    private val feedXml = """
        <rss version="0.92">
          <channel>
            <item>
              <title>Izit - Make Way For The Solos</title>
              <image><url>https://tracks.radionula.com/covers/izit.jpg</url></image>
            </item>
          </channel>
        </rss>
    """.trimIndent()

    private fun dataSource(engine: MockEngine): PlaylistNetworkDataSourceImpl {
        val client = HttpClient(engine) {
            // Mirrors nulaHttpClient(): a non-2xx must throw so it is exercised
            // by the same catch-all fetchPlaylist relies on in production.
            expectSuccess = true
            defaultRequest { url("https://test.example/") }
        }
        return PlaylistNetworkDataSourceImpl(PlaylistApiService(client))
    }

    @Test
    fun a_successful_fetch_returns_the_parsed_tracks() = runTest {
        val engine = MockEngine { respond(feedXml, HttpStatusCode.OK) }

        val tracks = dataSource(engine).fetchPlaylist(ChannelPresenter.Channel.Classic)

        assertEquals(listOf("Izit"), tracks?.map { it.artist })
    }

    @Test
    fun a_network_failure_returns_null_instead_of_throwing() = runTest {
        val engine = MockEngine { throw IllegalStateException("connection refused") }

        val tracks = dataSource(engine).fetchPlaylist(ChannelPresenter.Channel.Classic)

        assertNull(tracks)
    }

    @Test
    fun a_non_2xx_response_returns_null() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.InternalServerError) }

        val tracks = dataSource(engine).fetchPlaylist(ChannelPresenter.Channel.Classic)

        assertNull(tracks)
    }

    @Test
    fun malformed_xml_returns_null() = runTest {
        val engine = MockEngine { respond("this is not xml at all", HttpStatusCode.OK) }

        val tracks = dataSource(engine).fetchPlaylist(ChannelPresenter.Channel.Classic)

        assertNull(tracks)
    }
}
