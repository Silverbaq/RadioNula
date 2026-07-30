package com.radionula.radionula.data

import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.ChannelPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class PlaylistRepositoryImpl(
    private val playlistNetworkDataSource: PlaylistNetworkDataSource,
    private val coroutineScope: CoroutineScope,
) : PlaylistRepository {
    private var _currentChannel: ChannelPresenter.Channel = ChannelPresenter.Channel.Classic
    private var _currentSong: CurrentSong? = null
    private var autoFetchJob: Job? = null

    /** Only what has actually been heard since the app was opened. */
    private val sessionHistory = mutableListOf<NulaTrack>()

    // replay = 1 so observers attaching after a fetch still see the latest feed.
    private val _currentSongFlow = MutableSharedFlow<CurrentSong>(replay = 1)
    private val _playlist = MutableSharedFlow<List<NulaTrack>>(replay = 1)

    override fun currentSong(): Flow<CurrentSong> = _currentSongFlow

    override fun currentPlaylist(): Flow<List<NulaTrack>> = _playlist

    override suspend fun fetchCurrentPlaylist() {
        val tracks = playlistNetworkDataSource.fetchPlaylist(_currentChannel) ?: return

        // Only the feed's first item is used: the ten history entries it carries
        // were played before the app was even open, and the playlist is meant to
        // be what this session has heard.
        val current = tracks.firstOrNull() ?: return
        val song = CurrentSong(current.artist, current.image, current.title)
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
