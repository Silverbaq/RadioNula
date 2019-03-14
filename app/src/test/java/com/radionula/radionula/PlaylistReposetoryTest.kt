package com.radionula.radionula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.PlaylistRepositoryImpl
import com.radionula.radionula.data.network.PlaylistNetworkDataSource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PlaylistReposetoryTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val playlistNetworkDataSource = mock<PlaylistNetworkDataSource>()
    lateinit var playlistRepository: PlaylistRepository

    @Before
    fun before(){
        playlistRepository = PlaylistRepositoryImpl(playlistNetworkDataSource)
    }


    @Test
    fun getCurrentPlaylist_Test(){
        // TODO: Make test
    }

    @Test
    fun setChannel_Test(){
        // TODO: Make test
    }

    @Test
    fun fetchCurrentPlaylist_Test(){
        // TODO: Make test
    }

    @Test
    fun autoFetchPlaylist_Test(){
        // TODO: Make test
    }
}