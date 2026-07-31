package com.radionula.radionula

import com.radionula.radionula.core.util.ChannelPresenter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ChannelPresenterTest {

    lateinit var channelPresenter : ChannelPresenter

    @BeforeTest
    fun before() {
        channelPresenter= ChannelPresenter()
    }

    @Test
    fun classic_is_default_channel_test(){
        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Classic)
    }

    @Test
    fun select_switches_to_the_channel_at_that_index(){
        channelPresenter.select(ChannelPresenter.Channel.Ch2.ordinal)

        assert(channelPresenter.currentChannel == ChannelPresenter.Channel.Ch2)
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
    fun comments_urls_match_the_website_threads(){
        // These are the exact remark_config.url values radionula.com sends. Change
        // one and the app silently opens a different, empty conversation.
        assertEquals("https://radionula.com/", ChannelPresenter.Channel.Classic.commentsUrl)
        assertEquals("https://radionula.com/organic", ChannelPresenter.Channel.Ch2.commentsUrl)
        assertEquals("https://radionula.com/beatz", ChannelPresenter.Channel.Smoky.commentsUrl)
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
