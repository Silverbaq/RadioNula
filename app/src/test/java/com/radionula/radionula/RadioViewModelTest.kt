package com.radionula.radionula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.core.domain.repository.PlaylistRepository
import com.radionula.radionula.core.data.db.NulaDatabase
import com.radionula.radionula.core.domain.model.NulaTrack
import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.features.player.RadioViewModel
import com.radionula.radionula.services.mediaplayer.MediaplayerPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.anyInt

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

    private val isPlayingFlow = MutableStateFlow(false)
    private val channelIndexFlow = MutableStateFlow(0)

    @Before
    fun before() {
        // viewModelScope runs on Dispatchers.Main, which does not exist on the JVM.
        // Unconfined so the launched blocks finish before the assertions run.
        Dispatchers.setMain(UnconfinedTestDispatcher())

        whenever(channelPresenter.currentChannel).thenReturn(ChannelPresenter.Channel.Classic)
        whenever(channelPresenter.select(anyInt())).thenReturn(ChannelPresenter.Channel.Classic)
        // The ViewModel wraps the repository and player flows on construction,
        // so they all have to be stubbed before it is built.
        whenever(playlistRepository.currentSong()).thenReturn(emptyFlow())
        whenever(playlistRepository.currentPlaylist()).thenReturn(emptyFlow())
        whenever(mediaplayerPresenter.isPlaying).thenReturn(isPlayingFlow)
        whenever(mediaplayerPresenter.channelIndex).thenReturn(channelIndexFlow)

        radioViewModel = RadioViewModel(playlistRepository, channelPresenter, mediaplayerPresenter, nulaDatabase)
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun a_cold_start_fetches_nothing() = runTest {
        // The playlist is what this session has heard, so before tuning in there
        // is nothing to show.
        verify(playlistRepository, never()).fetchCurrentPlaylist()
    }

    @Test
    fun tuneIn_asks_the_player_for_the_current_channel_index() {
        radioViewModel.tuneIn()

        verify(mediaplayerPresenter).tuneIn(ChannelPresenter.Channel.Classic.ordinal)
    }

    @Test
    fun the_tune_in_button_shows_on_a_cold_start() {
        assertEquals(true, observedTuneInButton())
    }

    @Test
    fun the_tune_in_button_never_returns_after_tuning_in() {
        val tuneInVisible = observeTuneInButton()

        radioViewModel.tuneIn()
        assertEquals(false, tuneInVisible())

        // Pausing and skipping must not bring it back.
        isPlayingFlow.value = true
        isPlayingFlow.value = false
        channelIndexFlow.value = ChannelPresenter.Channel.Ch2.ordinal

        assertEquals(false, tuneInVisible())
    }

    @Test
    fun playback_started_outside_the_app_also_hides_the_tune_in_button() {
        val tuneInVisible = observeTuneInButton()

        // e.g. resumed from the notification while the UI was gone.
        isPlayingFlow.value = true

        assertEquals(false, tuneInVisible())
    }

    /** uiState is shared Eagerly, so its value is live without a collector. */
    private fun observeTuneInButton(): () -> Boolean = { radioViewModel.uiState.value.showTuneIn }

    private fun observedTuneInButton(): Boolean = observeTuneInButton()()

    @Test
    fun pauseRadio_only_pauses() {
        isPlayingFlow.value = true

        radioViewModel.pauseRadio()

        verify(mediaplayerPresenter).pauseRadio()
        verify(mediaplayerPresenter, never()).tuneIn(anyInt())
    }

    @Test
    fun nextChannel_skips_channel_while_playing() {
        isPlayingFlow.value = true

        radioViewModel.nextChannel()

        verify(mediaplayerPresenter, times(1)).nextChannel()
        verify(mediaplayerPresenter, never()).tuneIn(anyInt())
    }

    @Test
    fun nextChannel_starts_the_current_channel_while_stopped() {
        radioViewModel.nextChannel()

        verify(mediaplayerPresenter).tuneIn(ChannelPresenter.Channel.Classic.ordinal)
        verify(mediaplayerPresenter, never()).nextChannel()
    }

    @Test
    fun nextChannel_is_the_way_back_from_a_pause() {
        radioViewModel.tuneIn()
        isPlayingFlow.value = true
        radioViewModel.pauseRadio()
        isPlayingFlow.value = false

        radioViewModel.nextChannel()

        // Same channel resumed, not skipped past.
        verify(mediaplayerPresenter, times(2)).tuneIn(ChannelPresenter.Channel.Classic.ordinal)
        verify(mediaplayerPresenter, never()).nextChannel()
    }

    @Test
    fun isPlaying_follows_the_player_not_the_ui() {
        // Nothing in the ViewModel was touched: the player reports this itself,
        // which is how an audio-focus or notification pause reaches the UI.
        isPlayingFlow.value = true

        assertEquals(true, radioViewModel.uiState.value.isPlaying)
    }

    @Test
    fun a_channel_change_refreshes_the_feed_once_tuned_in() = runTest {
        whenever(channelPresenter.select(ChannelPresenter.Channel.Smoky.ordinal))
                .thenReturn(ChannelPresenter.Channel.Smoky)
        radioViewModel.tuneIn()

        channelIndexFlow.value = ChannelPresenter.Channel.Smoky.ordinal

        verify(channelPresenter).select(ChannelPresenter.Channel.Smoky.ordinal)
        verify(playlistRepository).setChannel(ChannelPresenter.Channel.Smoky)
        verify(playlistRepository, times(1)).fetchCurrentPlaylist()
    }

    @Test
    fun a_channel_change_before_tuning_in_still_does_not_fetch() = runTest {
        whenever(channelPresenter.select(ChannelPresenter.Channel.Ch2.ordinal))
                .thenReturn(ChannelPresenter.Channel.Ch2)

        channelIndexFlow.value = ChannelPresenter.Channel.Ch2.ordinal

        verify(playlistRepository).setChannel(ChannelPresenter.Channel.Ch2)
        verify(playlistRepository, never()).fetchCurrentPlaylist()
    }

    @Test
    fun autoFetchPlaylist_is_delegated_to_the_repository() {
        radioViewModel.autoFetchPlaylist()

        verify(playlistRepository).autoFetchPlaylist()
    }

    @Test
    fun addFavoriteClicked_stores_the_track_off_the_main_thread() = runTest {
        radioViewModel.addFavoriteClicked(NulaTrack.EMPTY)

        // insertTrack is suspend now, so this only verifies once the coroutine
        // the ViewModel launched has run.
        verify(nulaDatabase).insertTrack(NulaTrack.EMPTY)
        verify(mediaplayerPresenter, never()).tuneIn(anyInt())
    }
}
