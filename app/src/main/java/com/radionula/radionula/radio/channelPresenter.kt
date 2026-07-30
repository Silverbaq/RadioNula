package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ChannelPresenter {
    /**
     * [xmlPath] is the channel's "recently played" RSS feed, relative to
     * https://radionula.com/ - the old api.radionula.com JSON API is gone.
     *
     * Declaration order is the player's playlist order, so a channel's ordinal
     * is its media item index.
     */
    enum class Channel(val url: String, val xmlPath: String, val displayName: String) {
        Classic("https://strm.radionula.com/channel4", "recently_played_ch4.xml", "Classic"),
        Ch2("https://strm.radionula.com/channel5", "recently_played_ch5.xml", "Organic"),
        Smoky("https://strm.radionula.com/channel6", "recently_played_ch6.xml", "Beatz")
    }

    var currentChannel: Channel = Channel.Classic
        private set

    private val channelData = MutableLiveData<Channel>()
    fun observeChannel(): LiveData<Channel> = channelData

    init {
        select(Channel.Classic.ordinal)
    }

    /**
     * The player owns which channel is live - it can be changed from the
     * notification or a headset - so the index comes from there.
     */
    fun select(index: Int): Channel {
        val channel = Channel.entries.getOrElse(index) { Channel.Classic }
        currentChannel = channel
        channelData.postValue(channel)
        return channel
    }
}
