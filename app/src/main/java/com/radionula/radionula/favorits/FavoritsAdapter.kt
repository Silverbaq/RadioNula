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
import coil.load
import com.radionula.radionula.R
import com.radionula.radionula.data.db.NulaDatabase
import com.radionula.radionula.databinding.AdapterPlaylistBinding
import com.radionula.radionula.model.NulaTrack

class MyAdapter(private val nulaDatabase: NulaDatabase, val context: Context) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val myDataset: MutableList<NulaTrack> = nulaDatabase.selectAllTracks()

    class MyViewHolder(binding: AdapterPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        // Holds the TextView that will add each animal to
        val cover = binding.adapterPlaylistIvPlaylistCover
        val artist = binding.adapterPlaylistTvPlaylistArtist
        val title = binding.adapterPlaylistTvPlaylistTitle
        val container = binding.adapterPlaylistRlItem
        var clicked = false
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        val binding = AdapterPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.artist.text = myDataset[position].artist
        holder.title.text = myDataset[position].title

        //Glide.with(context).load(myDataset[position].image).into(holder.cover)
        holder.cover.load(myDataset[position].image)

        holder.container.setOnClickListener {
            val rlFavorit =
                LayoutInflater.from(context).inflate(R.layout.remove_favorit, holder.container)

            if (!holder.clicked) {

                val ivFavorit = rlFavorit.findViewById<View>(R.id.ivRemoveFavorit) as ImageView
                val ivSearch = rlFavorit.findViewById<View>(R.id.ivSearchFavorit) as ImageView
                val item = myDataset[position]

                // Click on favorite icon
                ivFavorit.setOnClickListener {
                    nulaDatabase.remoteTrack(item)

                    holder.container.removeAllViews()

                    myDataset.remove(item)
                    notifyDataSetChanged()

                    Toast.makeText(context, "Removed " + item.artist, Toast.LENGTH_SHORT).show()
                }

                // Click on search icon
                ivSearch.setOnClickListener {
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    val keyword = item.artist + " " + item.title
                    intent.putExtra(SearchManager.QUERY, keyword)
                    context.startActivity(intent)
                }

                holder.clicked = true

            } else {
                holder.container.removeAllViews()
                holder.clicked = false
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}