package com.radionula.radionula.features.player

import com.radionula.radionula.resources.Res
import com.radionula.radionula.resources.nula_channel1
import com.radionula.radionula.resources.nula_channel2
import com.radionula.radionula.resources.nula_channel3
import com.radionula.radionula.resources.pause_channel1
import com.radionula.radionula.resources.pause_channel2
import com.radionula.radionula.resources.pause_channel3
import com.radionula.radionula.resources.skip_channel1
import com.radionula.radionula.resources.skip_channel2
import com.radionula.radionula.resources.skip_channel3
import com.radionula.radionula.core.util.ChannelPresenter
import org.jetbrains.compose.resources.DrawableResource

/** The three per-channel drawables the player swaps together. */
data class ChannelArt(
    val logo: DrawableResource,
    val skip: DrawableResource,
    val pause: DrawableResource,
)

/**
 * Not in the ViewModel: these are drawables, and the ViewModel only reports
 * which channel is live. The screen decides what that looks like.
 */
fun ChannelPresenter.Channel.art(): ChannelArt = when (this) {
    ChannelPresenter.Channel.Classic -> ChannelArt(
        Res.drawable.nula_channel1, Res.drawable.skip_channel1, Res.drawable.pause_channel1
    )
    ChannelPresenter.Channel.Ch2 -> ChannelArt(
        Res.drawable.nula_channel2, Res.drawable.skip_channel2, Res.drawable.pause_channel2
    )
    ChannelPresenter.Channel.Smoky -> ChannelArt(
        Res.drawable.nula_channel3, Res.drawable.skip_channel3, Res.drawable.pause_channel3
    )
}
