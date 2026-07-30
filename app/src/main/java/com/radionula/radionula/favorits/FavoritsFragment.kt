package com.radionula.radionula.favorits

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.databinding.FragmentFavoritsBinding
import com.radionula.radionula.model.NulaTrack
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FavoritsFragment : Fragment() {
    private lateinit var binding: FragmentFavoritsBinding

    val nulaDatabase: NulaDatabase by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentFavoritsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentFavoritesLvFavorites.layoutManager = LinearLayoutManager(activity)

        val artistFont = Typeface.createFromAsset(requireContext().assets, "fonts/Roboto-Regular.ttf")
        binding.fragmentFavoritesTvHeader.typeface = artistFont

        // Scoped to the view, so a query still in flight when the screen closes
        // is cancelled instead of landing on a dead binding.
        viewLifecycleOwner.lifecycleScope.launch {
            val tracks = nulaDatabase.selectAllTracks().toMutableList()
            binding.fragmentFavoritesLvFavorites.adapter =
                MyAdapter(tracks, ::removeFavorite, requireContext())
        }
    }

    private fun removeFavorite(track: NulaTrack) {
        viewLifecycleOwner.lifecycleScope.launch { nulaDatabase.removeTrack(track) }
    }
}
