package com.radionula.radionula;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.radionula.MediaPlayerService;
import com.radionula.radionula.fragments.CommentsFragment;
import com.radionula.radionula.fragments.FavoritsFragment;
import com.radionula.radionula.fragments.NoConnectionFragment;
import com.radionula.radionula.fragments.PlayerFragment;
import com.radionula.radionula.interfaces.IControls;
import com.radionula.radionula.model.Constants;
import com.radionula.radionula.model.NetworkStateReceiver;
import com.radionula.radionula.model.NetworkStateReceiver.NetworkStateReceiverListener;
import com.radionula.radionula.model.PlaylistRepository;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements IControls, Observer, NetworkStateReceiverListener {
    private static String TAG = "MainActivity";

    DrawerLayout mDrawer;
    NavigationView nvDrawer;
    ImageView navButton;
    FrameLayout flFragments;

    // Backend
    private static PlaylistRepository _playlistRepository;

    // Fragments
    PlayerFragment playerFragment;
    FavoritsFragment favoritsFragment;
    CommentsFragment commentsFragment;

    FragmentTransaction transaction;

    private NetworkStateReceiver networkStateReceiver;

    // Mediaplayer
    Intent mediaPlayerServiceIntent;
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mediaPlayerServiceIntent = new Intent(MainActivity.this, MediaPlayerService.class);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        navButton = (ImageView) findViewById(R.id.nav_Button);

        flFragments = (FrameLayout) findViewById(R.id.activityMain_flFragments);


        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

        playerFragment = new PlayerFragment();
        favoritsFragment = new FavoritsFragment();
        commentsFragment = new CommentsFragment();


        // Transaction to swap fragments
        transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.activityMain_flFragments, playerFragment);

        // Commit the transaction
        transaction.commit();

        setupDrawerContent(nvDrawer);


        //
        // Call State
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyMgr.listen(new TeleListener(),
                PhoneStateListener.LISTEN_CALL_STATE);

        //
        // Network state
        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        //
        // WakeLock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG);

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
        transaction = getSupportFragmentManager().beginTransaction();

        // Create a new fragment and specify the planet to show based on
        // position
        switch (menuItem.getItemId()) {
            case R.id.nav_Radio_Player:
                transaction.replace(R.id.activityMain_flFragments, playerFragment);

                // Commit the transaction
                transaction.commit();

                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_Favorites:
                transaction.replace(R.id.activityMain_flFragments, favoritsFragment);

                // Commit the transaction
                transaction.commit();

                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_Comments:
                mDrawer.closeDrawer(GravityCompat.START);

                transaction.replace(R.id.activityMain_flFragments, commentsFragment);

                // Commit the transaction
                transaction.commit();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Makes sure the music keeps playing after the screen is off.
        try{
            mWakeLock.acquire();
        }catch (Exception ex){

        }
    }

    //
    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // If screen is backon while music is playing, release the lock
        if (MyApp.isPlaying)
        try {
            mWakeLock.release();
        } catch (Exception ex) {

        }
    }

    @Override
    public void Pause() {
        if (MyApp.isPlaying) {
            playerFragment.StopVinyl();
            mediaPlayerServiceIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
            startService(mediaPlayerServiceIntent);
            MyApp.isPlaying = false;
        }
    }

    @Override
    public void TuneIn() {
        if (!MyApp.tunedIn) {
            // Start to observe the playlist repository
            _playlistRepository = new PlaylistRepository(getString(R.string.nula_playlist));
            _playlistRepository.addObserver(this);

        }

        if (!MyApp.isPlaying) {
            mediaPlayerServiceIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
            startService(mediaPlayerServiceIntent);
            playerFragment.StartVinyl();
        }
        MyApp.isPlaying = true;
    }


    @Override
    public void UpdatePlaylist() {
        _playlistRepository.triggerObserver();
    }

    @Override
    public void update(Observable observable, Object data) {
        // updates playlist
        try {
            playerFragment.UpdatePlaylist(_playlistRepository.getPlaylist());
            playerFragment.SetVinylImage(_playlistRepository.getPlaylist().get(0).getImage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Back button listener.
     * Will close the application if the back button pressed twice.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    //
    // When the internet connection is reestablished
    @Override
    public void onNetworkAvailable() {
        try {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            findViewById(R.id.activityMain_toolbar).setLayoutParams(layoutParams);
            //findViewById(R.id.activityMain_toolbar).setVisibility(View.VISIBLE);
            transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.activityMain_flFragments, playerFragment);
            transaction.commit();

            if (MyApp.reconnect) {
                // Starts music player once agian.
                TuneIn();
                MyApp.reconnect = false;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }


    //
    // If there is no internet connection
    @Override
    public void onNetworkUnavailable() {
        try {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            findViewById(R.id.activityMain_toolbar).setLayoutParams(layoutParams);

            transaction = getSupportFragmentManager().beginTransaction();
            NoConnectionFragment fragment = new NoConnectionFragment();
            transaction.replace(R.id.activityMain_flFragments, fragment);
            transaction.commit();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }


    //
    // Detects the call state of the phone. Will pause music if phone rings.
    class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // TODO: Needs to restart music after a phone call. (If it was playing).
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:

                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // Pauses music
                    Pause();
                    break;
                default:
                    break;
            }
        }
    }

}
