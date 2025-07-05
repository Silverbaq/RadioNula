package com.radionula.radionula.data

import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import com.radionula.radionula.data.network.response.PlaylistResponse
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.ChannelPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

class PlaylistRepositoryImpl(
    private val playlistNetworkDataSource: PlaylistNetworkDataSource,
    private val coroutineScope: CoroutineScope,
) : PlaylistRepository {
    private var _currentChannel: ChannelPresenter.Channel = ChannelPresenter.Channel.Classic
    private var _currentSong: CurrentSong? = null

    private val _currentSongFlow = MutableSharedFlow<CurrentSong>()
    private val _cachedPlaylist = mutableListOf<CurrentSong>()

    private val _playlist = MutableSharedFlow<PlaylistResponse?>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistFlow: Flow<List<NulaTrack>> =
        _playlist.mapLatest { newPlaylist: PlaylistResponse? ->
            val song: CurrentSong? = when (_currentChannel) {
                ChannelPresenter.Channel.Classic -> {
                    newPlaylist?.classics?.currentSong
                }

                ChannelPresenter.Channel.Ch2 -> {
                    newPlaylist?.ch2?.currentSong
                }

                ChannelPresenter.Channel.Smoky -> {
                    newPlaylist?.smoky?.currentSong
                }
            }

            if (song != _currentSong) {
                _currentSong = song
                _currentSong?.let { currentSong ->
                    _currentSongFlow.emit(currentSong)
                    _cachedPlaylist.add(0, currentSong)
                }
            }

            _cachedPlaylist.map { NulaTrack(it.artist, it.title, it.cover) }
        }

    override fun currentSong(): Flow<CurrentSong> {
        return _currentSongFlow
    }

    override suspend fun fetchCurrentPlaylist() {
        val playlist = playlistNetworkDataSource.fetchPlaylist()
        _playlist.emit(playlist)
    }

    override fun autoFetchPlaylist() {
        coroutineScope.launch {
            while (true) {
                val playlist = playlistNetworkDataSource.fetchPlaylist()
                _playlist.emit(playlist)
                delay(20_000)
            }
        }
    }

    override fun currentPlaylist(): Flow<List<NulaTrack>> {
        return playlistFlow
    }

    override fun setChannel(channel: ChannelPresenter.Channel) {
        _currentChannel = channel
    }
}