package com.radionula.radionula.data.network.response

import com.radionula.radionula.data.db.entity.Ch2
import com.radionula.radionula.data.db.entity.Classics
import com.radionula.radionula.data.db.entity.Smoky

data class PlaylistResponse(
        val ch2: Ch2,
        val classics: Classics,
        val smoky: Smoky
)