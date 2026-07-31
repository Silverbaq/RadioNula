package com.radionula.radionula

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.data.repository.PlaylistRepositoryImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.domain.model.NulaTrack
import com.radionula.radionula.core.util.ChannelPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class PlaylistRepositoryImplTest {

    private val dataSource: PlaylistNetworkDataSource = mock()

    private fun repository(scope: TestScope) = PlaylistRepositoryImpl(dataSource, scope)

    private fun feed(vararg titles: String) = titles.map { NulaTrack("Artist", it, "cover-$it") }

    @Test
    fun `the playlist is only what this session heard, not the feed history`() = runTest {
        // The feed carries the current track plus ten played before the app opened.
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Current", "Older", "Oldest"))
        val repository = repository(this)

        repository.fetchCurrentPlaylist()

        val playlist = repository.currentPlaylist().first()
        assertEquals(listOf("Current"), playlist.map { it.title })
    }

    @Test
    fun `a new track is prepended to the session history`() = runTest {
        val repository = repository(this)
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("First"))
        repository.fetchCurrentPlaylist()

        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Second"))
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Second", "First"), repository.currentPlaylist().first().map { it.title })
    }

    @Test
    fun `the same track fetched twice is not repeated`() = runTest {
        val repository = repository(this)
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Same"))

        repository.fetchCurrentPlaylist()
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Same"), repository.currentPlaylist().first().map { it.title })
    }

    @Test
    fun `clearSession leaves a new subscriber with nothing replayed`() = runTest {
        val repository = repository(this)
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Heard last time"))
        repository.fetchCurrentPlaylist()

        repository.clearSession()

        // This is the reopened-app case: the repository outlives the activity, so
        // a fresh ViewModel must not be handed the previous session's replay.
        assertNull(withTimeoutOrNull(100) { repository.currentPlaylist().first() })
        assertNull(withTimeoutOrNull(100) { repository.currentSong().first() })
    }

    @Test
    fun `history restarts from empty after clearSession`() = runTest {
        val repository = repository(this)
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Heard last time"))
        repository.fetchCurrentPlaylist()
        repository.clearSession()

        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Heard this time"))
        repository.fetchCurrentPlaylist()

        assertEquals(
                listOf("Heard this time"),
                repository.currentPlaylist().first().map { it.title }
        )
    }

    @Test
    fun `clearSession stops the polling loop`() = runTest {
        val repository = repository(this)
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Playing"))

        repository.autoFetchPlaylist()
        repository.clearSession()

        // A live poll loop would keep this scope busy and runTest would never finish.
    }

    @Test
    fun `a failed fetch leaves the session untouched`() = runTest {
        val repository = repository(this)
        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic))
                .thenReturn(feed("Playing"))
        repository.fetchCurrentPlaylist()

        whenever(dataSource.fetchPlaylist(ChannelPresenter.Channel.Classic)).thenReturn(null)
        repository.fetchCurrentPlaylist()

        assertEquals(listOf("Playing"), repository.currentPlaylist().first().map { it.title })
    }
}
