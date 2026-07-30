package com.radionula.radionula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.radio.ChannelPresenter
import com.radionula.radionula.radio.RadioViewModel
import com.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class RadioViewModelTest  {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val playlistRepository: PlaylistRepository = mock()
    private val channelPresenter: ChannelPresenter = mock()
    private val mediaplayerPresenter: MediaplayerPresenter = mock()
    private val nulaDatabase: NulaDatabase = mock()
    private lateinit var radioViewModel: RadioViewModel

    @Before
    fun before() {
        // viewModelScope runs on Dispatchers.Main, which does not exist on the JVM.
        // Unconfined so the launched blocks finish before the assertions run.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        whenever(channelPresenter.currentChannel).thenReturn(ChannelPresenter.Channel.Classic)
        // The ViewModel wraps both repository flows in asLiveData() on construction,
        // so they have to be stubbed before it is built.
        whenever(playlistRepository.currentSong()).thenReturn(emptyFlow())
        whenever(playlistRepository.currentPlaylist()).thenReturn(emptyFlow())
        radioViewModel = RadioViewModel(playlistRepository, channelPresenter, mediaplayerPresenter, nulaDatabase)
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun tuneIn_tuneIn_IsTriggered() {
        radioViewModel.tuneIn()
        val actual = radioViewModel.tunedIn.value
        Assert.assertEquals(Unit, actual)
    }

    @Test
    fun tuneIn_isPlaying_returnTrue() {
        radioViewModel.tuneIn()
        val actual = radioViewModel.isPlaying.value
        Assert.assertEquals(true, actual)
    }

    @Test
    fun tuneIn_mediaStream_tunedIn() {
        radioViewModel.tuneIn()
        verify(mediaplayerPresenter).tuneIn(channelPresenter.currentChannel.url)
    }

    @Test
    fun pauseRadio_pauseData_hasValue() {
        radioViewModel.pauseRadio()
        val actual = radioViewModel.pause.value
        Assert.assertEquals(Unit, actual)
    }

    @Test
    fun tuneIn_sets_playing_to_true() {
        val observer = mock<Observer<Boolean>>()

        radioViewModel.isPlaying.observeForever(observer)
        radioViewModel.tuneIn()

        verify(observer).onChanged(true)
        verify(mediaplayerPresenter, times(1)).tuneIn(ChannelPresenter.Channel.Classic.url)
    }

    @Test
    fun pause_sets_playing_to_false() {
        val observer = mock<Observer<Boolean>>()

        radioViewModel.isPlaying.observeForever(observer)
        radioViewModel.pauseRadio()

        verify(observer).onChanged(false)
        verify(mediaplayerPresenter, times(1)).pauseRadio()
    }

    @Test
    fun nextChannel_shift_to_other_channel_when_tunedin(){
        radioViewModel.tuneIn()

        runBlocking {
            radioViewModel.nextChannel()
        }

        verify(channelPresenter, times(1)).nextChannel()
        verify(mediaplayerPresenter, times(2)).tuneIn(ChannelPresenter.Channel.Classic.url)
    }

    @Test
    fun nextChannel_do_not_shift_to_other_channel_when_not_tunedin(){
        radioViewModel.nextChannel()

        verify(channelPresenter, times(0)).nextChannel()
        verify(mediaplayerPresenter, times(1)).tuneIn(ChannelPresenter.Channel.Classic.url)
    }
}