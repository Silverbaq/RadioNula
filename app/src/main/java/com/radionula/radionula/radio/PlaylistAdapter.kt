package com.radionula.radionula.radio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.radionula.radionula.R
import com.radionula.radionula.model.NulaTrack

class PlaylistAdapter(
        private val items: MutableList<NulaTrack> = mutableListOf(),
        private val clickListener: FavoritesListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_header, parent, false)
                PlaylistHeaderViewHolder(view)
            }
            BODY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_header, parent, false)
                PlaylistBodyViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_track, parent, false)
                NulaTrackViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder.itemViewType) {
            HEADER -> {
                (holder as PlaylistHeaderViewHolder).bind()
            }
            BODY -> {
                (holder as PlaylistBodyViewHolder).bind()
            }
            else -> {
                (holder as PlaylistAdapter.NulaTrackViewHolder).bind(item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun update(items: List<NulaTrack>) {
        this.items.clear()
        this.items.addAll(items)
        if (this.items.isNotEmpty())
            this.items.add(0, NulaTrack.EMPTY)
        if (this.items.size > 2)
            this.items.add(2, NulaTrack.EMPTY)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) HEADER else if (position == 2) BODY else NULA_TRACK
    }

    inner class NulaTrackViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val cover = view.findViewById<ImageView>(R.id.adapter_playlist_ivPlaylistCover)
        val artist = view.findViewById<TextView>(R.id.adapter_playlist_tvPlaylistArtist)
        val title = view.findViewById<TextView>(R.id.adapter_playlist_tvPlaylistTitle)
        val favoriteLayout = view.findViewById<View>(R.id.addFavoriteLayout)
        val addFavorite = view.findViewById<ImageView>(R.id.ivAddFavorit)

        fun bind(item: NulaTrack) {
            cover.load(item.image)
            artist.text = item.artist
            title.text = item.title
            favoriteLayout.visibility = View.GONE
            bindOnClick()
            bindOnFavoriteClicked(item)
        }

        fun bindOnFavoriteClicked(item: NulaTrack) {
            addFavorite.setOnClickListener {
                clickListener.onAddFavoriteClicked(item)
                favoriteLayout.visibility = View.GONE
            }
        }

        fun bindOnClick() {
            view.setOnClickListener {
                favoriteLayout.visibility = View.VISIBLE
            }
            favoriteLayout.setOnClickListener {
                favoriteLayout.visibility = View.GONE
            }
        }
    }

    inner class PlaylistHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader = view.findViewById<View>(R.id.list_header_textview) as TextView

        fun bind() {
            tvHeader.text = HEADER_TEXT
        }
    }

    inner class PlaylistBodyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader = view.findViewById<View>(R.id.list_header_textview) as TextView

        fun bind() {
            tvHeader.text = BODY_TEXT
        }
    }

    companion object {
        private const val HEADER = 1
        private const val BODY = 2
        private const val NULA_TRACK = 3

        private const val HEADER_TEXT = "NOW PLAYING"
        private const val BODY_TEXT = "PLAYLIST HISTORY"
    }
}