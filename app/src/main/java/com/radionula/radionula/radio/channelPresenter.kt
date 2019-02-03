package com.radionula.radionula.radio

class ChannelPresenter {
    enum class Channel {
        Classic,
        Ch2,
        Smoky
    }
    var currentChannel: Channel

    init {
        currentChannel = Channel.Classic
    }

    fun nextChannel(){
        if (currentChannel == Channel.Classic)
            currentChannel = Channel.Ch2
        else if (currentChannel == Channel.Ch2)
            currentChannel = Channel.Smoky
        else
            currentChannel = Channel.Classic
    }
}
