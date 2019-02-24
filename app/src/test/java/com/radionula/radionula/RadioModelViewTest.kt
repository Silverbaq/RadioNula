package com.radionula.radionula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.data.PlaylistRepository
import com.radionula.radionula.radio.ChannelPresenter
import com.radionula.radionula.radio.RadioModelView
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest

@RunWith(JUnit4::class)
class RadioModelViewTest : KoinTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val playlistRepository: PlaylistRepository = mock()
    private val channelPresenter: ChannelPresenter = mock()
    private lateinit var modelView: RadioModelView
    //val modelView: RadioModelView by inject()

    @Before
    fun before() {
        modelView = RadioModelView(playlistRepository, channelPresenter)
    }


    @Test
    fun tuneIn_sets_playing_to_true() {
        val observer = mock<Observer<Boolean>>()
        modelView.observePlaying().observeForever(observer)

        modelView.tuneIn()

        verify(observer).onChanged(true)
    }

    @Test
    fun pause_sets_playing_to_false() {
        val observer = mock<Observer<Boolean>>()
        modelView.observePlaying().observeForever(observer)

        modelView.pauseRadio()

        verify(observer).onChanged(false)
    }

    @Test
    fun nextChannel_shift_to_other_channel_when_tunedin(){
        //whenever(modelView.play).thenReturn(ChannelPresenter.Channel.Classic)

        val observer = mock<Observer<ChannelPresenter.Channel>>()
        modelView.observeCurrentChannel().observeForever(observer)

        modelView.tuneIn()
        runBlocking {
            modelView.nextChannel()
        }
        verify(channelPresenter, times(1)).nextChannel()
    }

    @Test
    fun nextChannel_do_not_shift_to_other_channel_when_not_tunedin(){
        val observer = mock<Observer<ChannelPresenter.Channel>>()
        modelView.observeCurrentChannel().observeForever(observer)

        runBlocking {
            modelView.nextChannel()
        }
        verify(channelPresenter, times(0)).nextChannel()
    }

}