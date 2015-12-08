package com.radionula.radionula;

import android.app.Application;

import com.androidquery.AQuery;

/**
 * Created by silverbaq on 12/6/15.
 */
public class MyApp extends Application {

    public static AQuery aquery;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        aquery = new AQuery(this);
    }


    public static void SaveUserFavorites(){

    }

    public static void LoadUserFavorites(){

    }
}
