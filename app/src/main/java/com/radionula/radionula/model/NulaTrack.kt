package com.radionula.radionula.model

class NulaTrack(val artist: String, val title: String, val image: String, val id: Int = -1,) {
    companion object {
        val EMPTY = NulaTrack("","","")
    }
}