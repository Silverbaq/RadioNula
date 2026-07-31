package com.radionula.radionula.features.player

import androidx.annotation.DrawableRes
import com.radionula.radionula.R
import com.radionula.radionula.core.util.ChannelPresenter

/** The three per-channel drawables the player swaps together. */
data class ChannelArt(
    @param:DrawableRes val logo: Int,
    @param:DrawableRes val skip: Int,
    @param:DrawableRes val pause: Int,
)

/**
 * Lives in :app, not in the ViewModel: these are R.drawable ints, and R is
 * Android-only. The ViewModel reports which channel is live and the screen
 * decides what that looks like.
 */
fun ChannelPresenter.Channel.art(): ChannelArt = when (this) {
    ChannelPresenter.Channel.Classic -> ChannelArt(
        R.drawable.nula_channel1, R.drawable.skip_channel1, R.drawable.pause_channel1
    )
    ChannelPresenter.Channel.Ch2 -> ChannelArt(
        R.drawable.nula_channel2, R.drawable.skip_channel2, R.drawable.pause_channel2
    )
    ChannelPresenter.Channel.Smoky -> ChannelArt(
        R.drawable.nula_channel3, R.drawable.skip_channel3, R.drawable.pause_channel3
    )
}
