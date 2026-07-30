package com.radionula.radionula.data

import androidx.lifecycle.LiveData
import com.radionula.radionula.data.db.entity.CurrentSong
import com.radionula.radionula.model.NulaTrack
import com.radionula.radionula.radio.ChannelPresenter
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository{
    fun currentPlaylist(): Flow<List<NulaTrack>>
    fun currentSong(): Flow<CurrentSong>
    suspend fun fetchCurrentPlaylist()
    fun setChannel(channel: ChannelPresenter.Channel)
    fun autoFetchPlaylist()

    /**
     * Ends the listening session: stops polling and forgets everything heard,
     * including the replay caches the flows hand to new subscribers.
     *
     * This repository is a Koin single, so it outlives the activity - and the
     * process outlives a swipe-away while the media session is still bound.
     */
    fun clearSession()
}