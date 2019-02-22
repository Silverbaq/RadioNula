package com.radionula.radionula;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.radionula.radionula.model.NulaTrack;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by silverbaq on 12/7/15.
 */
public class PlaylistAdapter extends BaseAdapter {
    public enum AdapterType {
        ADD, REMOVE
    }

    List<NulaTrack> tracks;
    Activity activity;
    AdapterType adapterType;


    public PlaylistAdapter(Activity activity, List<NulaTrack> playlist, AdapterType type) {
        this.tracks = playlist;
        this.activity = activity;
        this.adapterType = type;
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
        FrameLayout container;
        boolean clicked;
    }

    public void updateList(List<NulaTrack> list) {
        tracks = list;
        synchronized (tracks) {
            tracks.notifyAll();
        }
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = activity.getLayoutInflater().inflate(R.layout.adapter_playlist, parent, false);

            holder.image = (ImageView) convertView.findViewById(R.id.adapter_playlist_ivPlaylistCover);
            holder.artist = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlaylistArtist);
            holder.title = (TextView) convertView.findViewById(R.id.adapter_playlist_tvPlaylistTitle);
            holder.container = convertView.findViewById(R.id.adapter_playlist_rlItem);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final NulaTrack item = (NulaTrack) getItem(position);

        holder.artist.setText(item.getArtist());
        holder.title.setText(item.getTitel());
        //MyApp.Companion.getAquery().id(holder.image).image(item.getImage(), true, true, 0, 0, null, AQuery.FADE_IN);
        //MyApp.getImageLoader().displayImage(item.getImage(), holder.image);
        Glide.with(activity).load(item.getImage()).into(holder.image);

        Typeface artistFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf");
        holder.artist.setTypeface(artistFont);

        Typeface titleFont = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Light.ttf");
        holder.title.setTypeface(titleFont);

        holder.clicked = false;

        // OnClick - Add to favorite
        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (adapterType == AdapterType.ADD) {

                    View rlFavorit = activity.getLayoutInflater().inflate(R.layout.add_favorit, holder.container);

                    if (!holder.clicked) {


                        ImageView ivFavorit = (ImageView) rlFavorit.findViewById(R.id.ivAddFavorit);

                        // Click on favorite icon
                        ivFavorit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MyApp.Companion.addToFavorites(item);
                                holder.container.removeAllViews();
                                Toast.makeText(activity, "Added " +  item.getTitel() + " to favorites", Toast.LENGTH_LONG).show();
                            }
                        });

                        holder.clicked = true;
                    } else {
                        holder.container.removeAllViews();
                        holder.clicked = false;
                    }
                } else if (adapterType == AdapterType.REMOVE){
                    View rlFavorit = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.remove_favorit, holder.container);

                    // clean selected
                    for(int i = 0; i < parent.getChildCount(); i++){
                        View tmp = parent.getChildAt(i);
                        ViewHolder tmpHolder = (ViewHolder) tmp.getTag();
                        if (tmpHolder.clicked){
                            tmpHolder.container.removeAllViews();
                            if(i != position)
                                tmpHolder.clicked = false;
                        }
                    }


                    if (!holder.clicked) {

                        ImageView ivFavorit = (ImageView) rlFavorit.findViewById(R.id.ivRemoveFavorit);
                        ImageView ivSearch = (ImageView) rlFavorit.findViewById(R.id.ivSearchFavorit);

                        // Click on favorite icon
                        ivFavorit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MyApp.Companion.RemoveFavorit(item);

                                holder.container.removeAllViews();
                                tracks.remove(item);
                                notifyDataSetChanged();

                                Toast.makeText(activity, "Removed " + item.getTitel(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Click on search icon
                        ivSearch.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                String keyword= item.getArtist() + " " + item.getTitel();
                                intent.putExtra(SearchManager.QUERY, keyword);
                                activity.startActivity(intent);
                            }
                        });

                        holder.clicked = true;
                    } else {
                        holder.container.removeAllViews();
                        holder.clicked = false;
                    }
                }
            }
        });


        return convertView;
    }


    void resetAdapter(){
        List<NulaTrack> tmpList = new ArrayList<>();
        tmpList.addAll(tracks);
        tracks.clear();
        tracks.addAll(tmpList);
        notifyDataSetChanged();
    }

}
