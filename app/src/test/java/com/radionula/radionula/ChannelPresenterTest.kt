package com.radionula.radionula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.radionula.radionula.radio.ChannelPresenter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ChannelPresenterTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()
    lateinit var channelPresenter : ChannelPresenter

    @Before
    fun before() {
        channelPresenter= ChannelPresenter()
    }

    @Test
    fun classic_is_default_channel_test(){
        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Classic)
    }

    @Test
    fun select_publishes_the_channel_at_that_index(){
        val observer = mock<Observer<ChannelPresenter.Channel>>()
        channelPresenter.observeChannel().observeForever(observer)

        channelPresenter.select(ChannelPresenter.Channel.Ch2.ordinal)

        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Ch2)
        verify(observer).onChanged(ChannelPresenter.Channel.Ch2)
    }

    @Test
    fun select_returns_the_selected_channel(){
        assertEquals(
                ChannelPresenter.Channel.Smoky,
                channelPresenter.select(ChannelPresenter.Channel.Smoky.ordinal)
        )
    }

    @Test
    fun select_falls_back_to_classic_for_an_unknown_index(){
        channelPresenter.select(ChannelPresenter.Channel.Smoky.ordinal)

        channelPresenter.select(99)

        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Classic)
    }

    @Test
    fun channel_ordinals_match_the_player_playlist_order(){
        // The service queues Channel.entries as media items, so the ordinal is
        // the media item index the controller seeks to.
        assertEquals(0, ChannelPresenter.Channel.Classic.ordinal)
        assertEquals(1, ChannelPresenter.Channel.Ch2.ordinal)
        assertEquals(2, ChannelPresenter.Channel.Smoky.ordinal)
    }
}
