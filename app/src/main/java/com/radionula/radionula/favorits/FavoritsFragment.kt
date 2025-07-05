package com.radionula.radionula.favorits

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.databinding.FragmentFavoritsBinding
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
        binding.fragmentFavoritesLvFavorites.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = MyAdapter(nulaDatabase, context = requireContext())
        }
        val artistFont = Typeface.createFromAsset(requireContext().assets, "fonts/Roboto-Regular.ttf")
        binding.fragmentFavoritesTvHeader.typeface = artistFont
    }
}
