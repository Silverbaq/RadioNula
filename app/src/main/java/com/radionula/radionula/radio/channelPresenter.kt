package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ChannelPresenter {
    enum class Channel {
        Classic,
        Ch2,
        Smoky
    }
    var currentChannel: Channel
    val channelData =  MutableLiveData<Channel>()
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
