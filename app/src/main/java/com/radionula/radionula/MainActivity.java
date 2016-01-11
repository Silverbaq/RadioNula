package com.radionula.radionula;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.radionula.fragments.PlayerFragment;
import com.radionula.interfaces.IControls;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements IControls, Observer {

    DrawerLayout mDrawer;
    NavigationView nvDrawer;
    ImageView navButton;

    // Fragments
    PlayerFragment playerFragment;

    // Mediaplayer
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Start to observe the playlist repository
        MyApp.get_playlistRepository().addObserver(this);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        navButton = (ImageView)findViewById(R.id.nav_Button);

        playerFragment = (PlayerFragment)getSupportFragmentManager().findFragmentById(R.id.activityMain_fragmentPlayer);

        // Sets playlist
        playerFragment.SetPlaylist(MyApp.get_playlistRepository().getPlaylist());
        playerFragment.SetVinylImage(MyApp.get_playlistRepository().getPlaylist().get(0).getImage());

        // Mediaplayer
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        makeMediaPlayerReady(getString(R.string.classic_radiostream_path));


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

        setupDrawerContent(nvDrawer);


    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position
        switch (menuItem.getItemId()) {
            case R.id.nav_Radio_Player:
                Toast.makeText(this,"Radio Player", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_Favorites:
                Toast.makeText(this,"Favorites", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_Comments:
                Toast.makeText(this,"Comments", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }


    private void makeMediaPlayerReady(String url) {
        mp.reset();
        try {
            mp.setDataSource(url);
            mp.prepare();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void Skip() {
        // TODO: Refactor
        playerFragment.StartVinyl();

        mp.start();
    }

    @Override
    public void Pause() {
        playerFragment.StopVinyl();
        mp.pause();
    }

    @Override
    public void update(Observable observable, Object data) {
        // updates playlist
        playerFragment.UpdatePlaylist(MyApp.get_playlistRepository().getPlaylist());
        playerFragment.SetVinylImage(MyApp.get_playlistRepository().getPlaylist().get(0).getImage());
    }
}
