package com.radionula.radionula;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.radionula.com.radionula.model.NulaTrack;

import java.util.List;

/**
 * Created by silverbaq on 12/7/15.
 */
public class PlaylistAdapter extends BaseAdapter {
    List<NulaTrack> tracks;
    Activity activity;

    public PlaylistAdapter(Activity activity, List<NulaTrack> playlist){
        this.tracks = playlist;
        this.activity = activity;
    }


    @Override
    public int getCount() {
        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder{
        ImageView image;
        TextView artist;
        TextView title;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();

            convertView = activity.getLayoutInflater().inflate(R.layout.adapter_playlist, parent, false);

            // Checks if it's the first, and current number playing
            if (position == 0){
                holder.image = (ImageView) convertView.findViewById(R.id.adapter_playlist_ivPlayingCover);
                holder.artist = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlayingArtist);
                holder.title = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlayingTitle);

                convertView.findViewById(R.id.adapter_playlist_llNowPlaying).setVisibility(View.GONE);

            } else {
                holder.image = (ImageView) convertView.findViewById(R.id.adapter_playlist_ivPlaylistCover);
                holder.artist = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlaylistArtist);
                holder.title = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlaylistTitle);

                convertView.findViewById(R.id.adapter_playlist_llPlaylist).setVisibility(View.GONE);
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NulaTrack item = (NulaTrack) getItem(position);

        holder.artist.setText(item.getArtist());
        holder.title.setText(item.getTitel());

        //TODO: Image from url to ImageView
        //TODO: Animation on image

        return convertView;
    }
}
