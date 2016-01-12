package com.radionula.radionula;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.androidquery.AQuery;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.radionula.model.NulaTrack;
import com.radionula.model.PlaylistRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silverbaq on 12/6/15.
 */
public class MyApp extends Application {

    public static AQuery aquery;
    private static ImageLoader imageLoader;
    private static PlaylistRepository _playlistRepository;
    private static List<NulaTrack> _favorites;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);


        aquery = new AQuery(this);
        _playlistRepository = new PlaylistRepository();
        _favorites = new ArrayList<>();
    }

    public static ImageLoader getImageLoader(){
        if (imageLoader == null){
            imageLoader = ImageLoader.getInstance();
        }
        return imageLoader;
    }


    public static PlaylistRepository get_playlistRepository(){
        return _playlistRepository;

    }

    public static void addToFavorites(NulaTrack track){
        _favorites.add(track);
    }

    public static void SaveUserFavorites(Context context){
        SharedPreferences mPrefs = context.getSharedPreferences("Nula", MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(_favorites);
        prefsEditor.putString("favorites", json);
        prefsEditor.commit();
    }

    public static List<NulaTrack> LoadUserFavorites(Context context){
        SharedPreferences mPrefs = context.getSharedPreferences("Nula",MODE_PRIVATE);

        Gson gson = new Gson();
        String json = mPrefs.getString("favorites", "");
        _favorites = gson.fromJson(json, new TypeToken<List<NulaTrack>>(){}.getType());
        return _favorites;
    }


}
