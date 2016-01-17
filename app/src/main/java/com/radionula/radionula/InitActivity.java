package com.radionula.radionula;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.Observable;
import java.util.Observer;

public class InitActivity extends AppCompatActivity implements Observer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        //Start to observe the playlist repository
        MyApp.get_playlistRepository().addObserver(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.get_playlistRepository().getPlaylist().size() != 0)
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //finish();

    }

    @Override
    protected void onStop() {
        MyApp.get_playlistRepository().deleteObserver(this);
        super.onStop();
    }
}
