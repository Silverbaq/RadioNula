package com.radionula.radionula.favorits

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radionula.radionula.MyApp
import com.radionula.radionula.R
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.model.NulaTrack
import kotlinx.android.synthetic.main.adapter_playlist.view.*

class MyAdapter(private val nulaDatabase: NulaDatabase, val context: Context) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val myDataset: MutableList<NulaTrack> = nulaDatabase.selectAllTracks()

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val cover = view.adapter_playlist_ivPlaylistCover
        val artist = view.adapter_playlist_tvPlaylistArtist
        val title = view.adapter_playlist_tvPlaylistTitle
        val container = view.adapter_playlist_rlItem
        var clicked = false
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_playlist, parent, false))
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.artist.text = myDataset[position].artist
        holder.title.text = myDataset[position].titel

        Glide.with(context).load(myDataset[position].image).into(holder.cover)

        holder.container.setOnClickListener(View.OnClickListener {

            val rlFavorit = LayoutInflater.from(context).inflate(R.layout.remove_favorit, holder.container)

            if (!holder.clicked) {

                val ivFavorit = rlFavorit.findViewById<View>(R.id.ivRemoveFavorit) as ImageView
                val ivSearch = rlFavorit.findViewById<View>(R.id.ivSearchFavorit) as ImageView

                // Click on favorite icon
                ivFavorit.setOnClickListener {
                    nulaDatabase.remoteTrack(myDataset[position])

                    holder.container.removeAllViews()
                    myDataset.remove(myDataset[position])
                    notifyDataSetChanged()

                    Toast.makeText(context, "Removed " + myDataset[position].artist, Toast.LENGTH_SHORT).show()
                }

                // Click on search icon
                ivSearch.setOnClickListener {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    val keyword = myDataset[position].artist + " " + myDataset[position].titel
                    intent.putExtra(SearchManager.QUERY, keyword)
                    context.startActivity(intent)
                }

                holder.clicked = true

            } else {
                holder.container.removeAllViews()
                holder.clicked = false
            }

        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}