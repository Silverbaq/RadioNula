package com.radionula.radionula.core.util

class ChannelPresenter {
    /**
     * [xmlPath] is the channel's "recently played" RSS feed, relative to
     * https://radionula.com/ - the old api.radionula.com JSON API is gone.
     *
     * [commentsUrl] is the Remark42 thread identifier and must stay byte-for-byte
     * what radionula.com sends, or the app opens a different conversation than
     * the website.
     *
     * Declaration order is the player's playlist order, so a channel's ordinal
     * is its media item index.
     */
    enum class Channel(
        val url: String,
        val xmlPath: String,
        val displayName: String,
        val commentsUrl: String
    ) {
        Classic(
            "https://strm.radionula.com/channel4",
            "recently_played_ch4.xml",
            "Classic",
            "https://radionula.com/"
        ),
        Ch2(
            "https://strm.radionula.com/channel5",
            "recently_played_ch5.xml",
            "Organic",
            "https://radionula.com/organic"
        ),
        Smoky(
            "https://strm.radionula.com/channel6",
            "recently_played_ch6.xml",
            "Beatz",
            "https://radionula.com/beatz"
        )
    }

    var currentChannel: Channel = Channel.Classic
        private set

    /**
     * The player owns which channel is live - it can be changed from the
     * notification or a headset - so the index comes from there.
     */
    fun select(index: Int): Channel {
        currentChannel = Channel.entries.getOrElse(index) { Channel.Classic }
        return currentChannel
    }
}
