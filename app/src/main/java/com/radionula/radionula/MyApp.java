package com.radionula.radionula;

import android.app.Application;

import com.androidquery.AQuery;
import com.radionula.model.PlaylistRepository;

/**
 * Created by silverbaq on 12/6/15.
 */
public class MyApp extends Application {

    public static AQuery aquery;
    private static PlaylistRepository _playlistRepository;


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        aquery = new AQuery(this);
        _playlistRepository = new PlaylistRepository();
    }


    public static PlaylistRepository get_playlistRepository(){
        return _playlistRepository;
    }

    public static void SaveUserFavorites(){

    }

    public static void LoadUserFavorites(){

    }
}
