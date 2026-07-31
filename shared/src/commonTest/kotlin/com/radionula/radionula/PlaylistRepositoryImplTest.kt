package com.radionula.radionula

import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.repository.PlaylistRepositoryImpl
import com.radionula.radionula.domain.model.NulaTrack
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistRepositoryImplTest {

    /**
     * Replaces the Mockito mock. Only one method to stub, and the tests only
     * ever need "what does the next fetch return", so a mutable property is
     * the whole fake.
     */
    private class FakeNetworkDataSource : PlaylistNetworkDataSource {
        var nextResult: List<NulaTrack>? = null

        override suspend fun fetchPlaylist(channel: ChannelPresenter.Channel): List<NulaTrack>? =
            nextResult
    }

    private val dataSource = FakeNetworkDataSource()

    private fun repository(scope: TestScope) = PlaylistRepositoryImpl(dataSource, scope)

    private fun feed(vararg titles: String) = titles.map { NulaTrack("Artist", it, "cover-$it") }

    @Test
    fun the_playlist_is_only_what_this_session_heard_not_the_feed_history() = runTest {
        // The feed carries the current track plus ten played before the app opened.
        dataSource.nextResult = feed("Current", "Older", "Oldest")
        val repository = repository(this)

        repository.fetchCurrentPlaylist()

        val playlist = repository.currentPlaylist().first()
        assertEquals(listOf("Current"), playlist.map { it.title })
    }

    @Test
    fun a_new_track_is_prepended_to_the_session_history() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("First")
        repository.fetchCurrentPlaylist()

        dataSource.nextResult = feed("Second")
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Second", "First"), repository.currentPlaylist().first().map { it.title })
    }

    @Test
    fun the_same_track_fetched_twice_is_not_repeated() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Same")

        repository.fetchCurrentPlaylist()
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Same"), repository.currentPlaylist().first().map { it.title })
    }

    @Test
    fun clearSession_leaves_a_new_subscriber_with_nothing_replayed() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Heard last time")
        repository.fetchCurrentPlaylist()

        repository.clearSession()

        // This is the reopened-app case: the repository outlives the activity, so
        // a fresh ViewModel must not be handed the previous session's replay.
        assertNull(withTimeoutOrNull(100) { repository.currentPlaylist().first() })
        assertNull(withTimeoutOrNull(100) { repository.currentSong().first() })
    }

    @Test
    fun history_restarts_from_empty_after_clearSession() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Heard last time")
        repository.fetchCurrentPlaylist()
        repository.clearSession()

        dataSource.nextResult = feed("Heard this time")
        repository.fetchCurrentPlaylist()

        assertEquals(
            listOf("Heard this time"),
            repository.currentPlaylist().first().map { it.title }
        )
    }

    @Test
    fun clearSession_stops_the_polling_loop() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Playing")

        repository.autoFetchPlaylist()
        repository.clearSession()

        // A live poll loop would keep this scope busy and runTest would never finish.
    }

    @Test
    fun a_failed_fetch_leaves_the_session_untouched() = runTest {
        val repository = repository(this)
        dataSource.nextResult = feed("Playing")
        repository.fetchCurrentPlaylist()

        dataSource.nextResult = null
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Playing"), repository.currentPlaylist().first().map { it.title })
    }
}
