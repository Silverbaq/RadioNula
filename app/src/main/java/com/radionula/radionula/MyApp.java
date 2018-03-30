package com.radionula.radionula;

import android.app.Application;
import android.content.Context;

import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.radionula.radionula.Util.NulaDatabase;
import com.radionula.radionula.model.NulaTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silverbaq on 12/6/15.
 */
public class MyApp extends Application {

    public static AQuery aquery;
    private static ImageLoader imageLoader;

    public static boolean tunedIn;
    public static boolean isPlaying;
    public static boolean reconnect = false;

    private static NulaDatabase database;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        database = new NulaDatabase(this);
        aquery = new AQuery(this);

        isPlaying = false;
        tunedIn = false;
    }

    public static ImageLoader getImageLoader(){
        if (imageLoader == null){
            imageLoader = ImageLoader.getInstance();
        }
        return imageLoader;
    }


    public static void addToFavorites(NulaTrack track){
        database.insertTrack(track);
    }

    public static List<NulaTrack> LoadUserFavorites(Context context){
        return database.selectAllTracks();
    }


    public static void RemoveFavorit(NulaTrack item) {
        database.remoteTrack(item);
    }
}
