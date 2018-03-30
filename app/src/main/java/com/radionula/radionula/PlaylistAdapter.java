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
import com.radionula.radionula.model.NulaTrack;


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
        RelativeLayout container;
        boolean clicked;
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

        holder.clicked = false;

        // OnClick - Add to favorite
        final View finalConvertView = convertView;
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (adapterType == AdapterType.ADD) {

                    View rlFavorit = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.add_favorit, holder.container);

                    if (!holder.clicked) {


                        ImageView ivFavorit = (ImageView) rlFavorit.findViewById(R.id.ivAddFavorit);

                        // Click on favorite icon
                        ivFavorit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MyApp.addToFavorites(item);
                                //MyApp.SaveUserFavorites(activity);
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

                    if (!holder.clicked) {


                        ImageView ivFavorit = (ImageView) rlFavorit.findViewById(R.id.ivRemoveFavorit);

                        // Click on favorite icon
                        ivFavorit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MyApp.RemoveFavorit(item);
                                //MyApp.SaveUserFavorites(activity);

                                holder.container.removeAllViews();
                                tracks.remove(item);
                                notifyDataSetChanged();

                                Toast.makeText(activity, "Removed " + item.getTitel(), Toast.LENGTH_SHORT).show();
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
}
