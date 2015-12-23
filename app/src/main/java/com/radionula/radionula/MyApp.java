package com.radionula.radionula;

import android.app.Application;

import com.androidquery.AQuery;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.radionula.model.PlaylistRepository;

/**
 * Created by silverbaq on 12/6/15.
 */
public class MyApp extends Application {

    public static AQuery aquery;
    private static ImageLoader imageLoader;
    private static PlaylistRepository _playlistRepository;


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);


        aquery = new AQuery(this);
        _playlistRepository = new PlaylistRepository();
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

    public static void SaveUserFavorites(){

    }

    public static void LoadUserFavorites(){

    }
}
