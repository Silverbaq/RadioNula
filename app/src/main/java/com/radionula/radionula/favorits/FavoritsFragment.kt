package com.radionula.radionula.favorits

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.radionula.radionula.MyApp
import com.radionula.radionula.PlaylistAdapter
import com.radionula.radionula.R
import com.radionula.radionula.data.db.NulaDatabase
import kotlinx.android.synthetic.main.fragment_favorits.*
import org.koin.android.ext.android.inject

/**
 * A simple [Fragment] subclass.
 */
class FavoritsFragment : Fragment() {
    val nulaDatabase: NulaDatabase by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorits, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_favorites_lvFavorites.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = MyAdapter(nulaDatabase, context = requireContext())
        }

        val artistFont = Typeface.createFromAsset(activity!!.assets, "fonts/Roboto-Regular.ttf")
        fragment_favorites_tvHeader.typeface = artistFont
    }
}
