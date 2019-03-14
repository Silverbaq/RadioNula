package com.radionula.radionula.radio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ChannelPresenter {
    enum class Channel(val url: String) {
        Classic("http://streaming.radionula.com:8800/classics"),
        Ch2("http://streaming.radionula.com:8800/channel2"),
        Smoky("http://streaming.radionula.com:8800/lounge")
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
