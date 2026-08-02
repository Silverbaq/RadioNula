package com.radionula.radionula.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.domain.model.NulaTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The list used to be loaded by the fragment and mutated in place by the
 * adapter. It lives here now so a query still in flight when the screen closes
 * cannot land on a dead view.
 */
class FavoritesViewModel(private val nulaDatabase: NulaDatabase) : ViewModel() {

    private val _tracks = MutableStateFlow<List<NulaTrack>>(emptyList())
    val tracks: StateFlow<List<NulaTrack>> = _tracks.asStateFlow()

    init {
        refresh()
    }

    fun remove(track: NulaTrack) {
        viewModelScope.launch {
            nulaDatabase.removeTrack(track)
            refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch { _tracks.value = nulaDatabase.selectAllTracks() }
    }
}