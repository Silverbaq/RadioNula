package com.radionula.radionula;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.radionula.model.NulaTrack;


import java.io.Serializable;
import java.util.List;

/**
 * Created by silverbaq on 12/7/15.
 */
public class PlaylistAdapter extends BaseAdapter {
    List<NulaTrack> tracks;
    Activity activity;

    public PlaylistAdapter(Activity activity, List<NulaTrack> playlist) {
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

    private static class ViewHolder {
        ImageView image;
        TextView artist;
        TextView title;
        RelativeLayout container;
    }

    public void updateList(List<NulaTrack> list) {
        tracks = list;
        synchronized (tracks) {
            tracks.notifyAll();
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = activity.getLayoutInflater().inflate(R.layout.adapter_playlist, parent, false);

            holder.image = (ImageView) convertView.findViewById(R.id.adapter_playlist_ivPlaylistCover);
            holder.artist = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlaylistArtist);
            holder.title = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlaylistTitle);
            holder.container = (RelativeLayout) convertView.findViewById(R.id.adapter_playlist_rlItem);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final NulaTrack item = (NulaTrack) getItem(position);

        holder.artist.setText(item.getArtist());
        holder.title.setText(item.getTitel());
        MyApp.aquery.id(holder.image).image(item.getImage(), true, true, 0, 0, null, AQuery.FADE_IN);
        //MyApp.getImageLoader().displayImage(item.getImage(), holder.image);

        Typeface artistFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf");
        holder.artist.setTypeface(artistFont);

        Typeface titleFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Light.ttf");
        holder.title.setTypeface(titleFont);

        // OnClick - Add to favorite
        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                View rlFavorit = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.add_favorit, holder.container);
                ImageView ivFavorit = (ImageView) rlFavorit.findViewById(R.id.ivAddFavorit);

                // Click on favorite icon
                ivFavorit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(activity, item.getArtist(), Toast.LENGTH_LONG).show();
                    }
                });


            }
        });


        return convertView;
    }
}
