package com.radionula.radionula.radio

import com.radionula.radionula.model.NulaTrack

interface FavoritesListener {
    fun onAddFavoriteClicked(track: NulaTrack)
}