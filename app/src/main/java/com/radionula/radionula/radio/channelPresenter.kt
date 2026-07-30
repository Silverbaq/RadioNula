package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ChannelPresenter {
    /**
     * [xmlPath] is the channel's "recently played" RSS feed, relative to
     * https://radionula.com/ - the old api.radionula.com JSON API is gone.
     */
    enum class Channel(val url: String, val xmlPath: String) {
        Classic("https://strm.radionula.com/channel4", "recently_played_ch4.xml"),
        Ch2("https://strm.radionula.com/channel5", "recently_played_ch5.xml"),
        Smoky("https://strm.radionula.com/channel6", "recently_played_ch6.xml")
    }
    var currentChannel: Channel = Channel.Classic
    private val channelData =  MutableLiveData<Channel>()
    fun observeChannel(): LiveData<Channel> = channelData

    init {
        currentChannel = Channel.Classic
        channelData.postValue(currentChannel)
    }

    fun nextChannel(){
        if (currentChannel == Channel.Classic) {
            currentChannel = Channel.Ch2
            channelData.postValue(currentChannel)
        }
        else if (currentChannel == Channel.Ch2) {
            currentChannel = Channel.Smoky
            channelData.postValue(currentChannel)
        }
        else {
            currentChannel = Channel.Classic
            channelData.postValue(currentChannel)
        }
    }
}
