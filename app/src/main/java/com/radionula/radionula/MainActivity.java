package com.radionula.radionula;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.radionula.model.PlaylistRepository;
import com.radionula.interfaces.IControls;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements IControls {

    private DrawerLayout mDrawer;
    ImageView navButton;

    // Playlist
    PlaylistRepository playlistReposetory = new PlaylistRepository();

    // Fragments
    TopFragment topFragment;
    ControlsFragment controlFragment;
    PlaylistFragment playlistFragment;

    // Mediaplayer
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (ImageView)findViewById(R.id.nav_Button);

        topFragment = (TopFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_fragmentTop);
        controlFragment = (ControlsFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_fragmentControls);
        playlistFragment = (PlaylistFragment)getSupportFragmentManager().findFragmentById(R.id.activityMain_fragmentPlaylist);

        // TODO: Move update playlist to better place
        playlistReposetory.updatePlaylist(getString(R.string.classic_rrs));

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void Skip() {
        topFragment.StartVinyl();
        playlistFragment.SetPlaylist(playlistReposetory.getPlaylist());
        mp.start();
    }

    @Override
    public void Pause() {
        topFragment.StopVinyl();
        mp.pause();
    }
}
