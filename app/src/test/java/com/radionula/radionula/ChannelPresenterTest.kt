package com.radionula.radionula

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.radionula.radionula.radio.ChannelPresenter
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
    fun can_play_next_channel_test(){
        val observer = mock<Observer<ChannelPresenter.Channel>>()
        channelPresenter.observeChannel().observeForever(observer)

        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Classic)

        channelPresenter.nextChannel()

        verify(observer).onChanged(ChannelPresenter.Channel.Ch2)
    }

    @Test
    fun channelPresenter_can_loop_test(){
        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Classic)

        channelPresenter.nextChannel()
        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Ch2)
        channelPresenter.nextChannel()
        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Smoky)
        channelPresenter.nextChannel()
        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Classic)

    }

}
