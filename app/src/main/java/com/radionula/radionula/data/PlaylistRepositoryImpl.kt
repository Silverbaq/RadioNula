package com.radionula.radionula.data

import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.ChannelPresenter
import kotlinx.coroutines.CoroutineScope
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

    // replay = 1 so observers attaching after a fetch still see the latest feed.
    private val _currentSongFlow = MutableSharedFlow<CurrentSong>(replay = 1)
    private val _playlist = MutableSharedFlow<List<NulaTrack>>(replay = 1)

    override fun currentSong(): Flow<CurrentSong> = _currentSongFlow

    override fun currentPlaylist(): Flow<List<NulaTrack>> = _playlist

    override suspend fun fetchCurrentPlaylist() {
        val tracks = playlistNetworkDataSource.fetchPlaylist(_currentChannel) ?: return
        _playlist.emit(tracks)

        // The feed's first item is what is playing right now, the rest is history.
        val song = tracks.firstOrNull()?.let { CurrentSong(it.artist, it.image, it.title) } ?: return
        if (song != _currentSong) {
            _currentSong = song
            _currentSongFlow.emit(song)
        }
    }

    override fun autoFetchPlaylist() {
        coroutineScope.launch {
            while (true) {
                fetchCurrentPlaylist()
                delay(30_000)
            }
        }
    }

    override fun setChannel(channel: ChannelPresenter.Channel) {
        if (channel == _currentChannel) return
        _currentChannel = channel
        _currentSong = null
    }
}
