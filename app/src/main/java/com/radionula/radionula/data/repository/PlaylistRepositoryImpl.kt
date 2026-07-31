package com.radionula.radionula.data.repository

import com.radionula.radionula.core.util.ChannelPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class PlaylistRepositoryImpl(
    private val playlistNetworkDataSource: com.radionula.radionula.data.network.PlaylistNetworkDataSource,
    private val coroutineScope: CoroutineScope,
) : com.radionula.radionula.domain.repository.PlaylistRepository {
    private var _currentChannel: ChannelPresenter.Channel = ChannelPresenter.Channel.Classic
    private var _currentSong: com.radionula.radionula.data.db.entity.CurrentSong? = null
    private var autoFetchJob: Job? = null

    /** Only what has actually been heard since the app was opened. */
    private val sessionHistory = mutableListOf<com.radionula.radionula.domain.model.NulaTrack>()

    // replay = 1 so observers attaching after a fetch still see the latest feed.
    private val _currentSongFlow = MutableSharedFlow<com.radionula.radionula.data.db.entity.CurrentSong>(replay = 1)
    private val _playlist = MutableSharedFlow<List<com.radionula.radionula.domain.model.NulaTrack>>(replay = 1)

    override fun currentSong(): Flow<com.radionula.radionula.data.db.entity.CurrentSong> = _currentSongFlow

    override fun currentPlaylist(): Flow<List<com.radionula.radionula.domain.model.NulaTrack>> = _playlist

    override suspend fun fetchCurrentPlaylist() {
        val tracks = playlistNetworkDataSource.fetchPlaylist(_currentChannel) ?: return

        // Only the feed's first item is used: the ten history entries it carries
        // were played before the app was even open, and the playlist is meant to
        // be what this session has heard.
        val current = tracks.firstOrNull() ?: return
        val song = _root_ide_package_.com.radionula.radionula.data.db.entity.CurrentSong(
            current.artist,
            current.image,
            current.title
        )
        if (song == _currentSong) return

        _currentSong = song
        _currentSongFlow.emit(song)

        sessionHistory.add(0, current)
        _playlist.emit(sessionHistory.toList())
    }

    override fun autoFetchPlaylist() {
        // Tune-in is reachable more than once now, and every call used to start
        // another endless poll loop.
        if (autoFetchJob?.isActive == true) return
        autoFetchJob = coroutineScope.launch {
            while (true) {
                fetchCurrentPlaylist()
                delay(30_000)
            }
        }
    }

    override fun clearSession() {
        autoFetchJob?.cancel()
        autoFetchJob = null
        sessionHistory.clear()
        _currentSong = null
        // Without this the flows keep replaying the last track and playlist to
        // the next subscriber, which is what made a reopened app look busy.
        _currentSongFlow.resetReplayCache()
        _playlist.resetReplayCache()
    }

    override fun setChannel(channel: ChannelPresenter.Channel) {
        // _currentSong is deliberately kept: it is the de-duplication key, and
        // clearing it would re-add the same track to the session history when
        // two channels happen to be playing it.
        _currentChannel = channel
    }
}