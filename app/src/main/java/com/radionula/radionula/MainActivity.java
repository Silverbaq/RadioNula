package com.radionula.radionula;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.radionula.fragments.CommentsFragment;
import com.radionula.fragments.FavoritsFragment;
import com.radionula.fragments.PlayerFragment;
import com.radionula.interfaces.IControls;
import com.radionula.model.NulaTrack;
import com.radionula.model.PlaylistRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements IControls, Observer {

    DrawerLayout mDrawer;
    NavigationView nvDrawer;
    ImageView navButton;
    FrameLayout flFragments;

    // Backend
    private static PlaylistRepository _playlistRepository;
    private static int _radioChannel = 1;

    // Fragments
    PlayerFragment playerFragment;
    FavoritsFragment favoritsFragment;
    CommentsFragment commentsFragment;

    FragmentTransaction transaction;

    // Mediaplayer
    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



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


    //
    // Make sure this is the method with just `Bundle` as the signature
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void Skip() {

        if (mp.isPlaying()) {
            //
            // Loads in own thread to ease the gui thread - doing the changes are rather heavy
            new ChangeChannelTask().execute();


        } else {
            playerFragment.StartVinyl();
            mp.start();
        }

    }

    @Override
    public void Pause() {
        playerFragment.StopVinyl();
        mp.pause();
    }

    @Override
    public void TuneIn() {
        // Play TuneIn noice sound
        MediaPlayer mpNoize = MediaPlayer.create(this, R.raw.radionoise);
        mpNoize.start();

        // Start to observe the playlist repository
        _playlistRepository = new PlaylistRepository(getString(R.string.classic_rrs));
        _playlistRepository.addObserver(this);

        // Mediaplayer
        mp = new MediaPlayer();
        //mp = MediaPlayer.create(this, Uri.parse(getString(R.string.classic_radiostream_path)));
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        makeMediaPlayerReady(getString(R.string.classic_radiostream_path));
        mpNoize.stop();

        Skip();
        MyApp.tunedIn = true;

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

    @Override
    public void onBackPressed() {
        if (commentsFragment.webViewGoBack()) {

        } else {
            super.onBackPressed();
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

    //
    // Detects the call state of the phone. Will pause music if phone rings.
    class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    // CALL_STATE_IDLE;
                    //Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE", Toast.LENGTH_LONG).show();

                    // TODO: Needs to restart music after a phone call. (If it was playing).
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // CALL_STATE_OFFHOOK;
                    //Toast.makeText(getApplicationContext(), "CALL_STATE_OFFHOOK", Toast.LENGTH_LONG).show();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    // CALL_STATE_RINGING
                    //Toast.makeText(getApplicationContext(), incomingNumber, Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), "CALL_STATE_RINGING", Toast.LENGTH_LONG).show();

                    // Pauses music
                    Pause();
                    break;
                default:
                    break;
            }
        }
    }

    private class ChangeChannelTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog;
        String logo = "";
        int skipImage = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this, "Loading", "Please wait...", true);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            playerFragment.UpdateChannelLogo(logo,skipImage);
            dialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ChangeChannel();

            return null;
        }

        //
        // Chances the channel logo, RSS feed and sets the audio stream
        private void ChangeChannel() {
            _radioChannel++;
            switch (_radioChannel) {
                case 1:
                    // Classic Nula
                    _playlistRepository.updateFeed(getString(R.string.classic_rrs));
                    logo = "drawable://" + R.drawable.nula_logo_ch1;
                    skipImage = R.drawable.play_button_1;

                    MediaPlayer mpTmp = MediaPlayer.create(MainActivity.this, Uri.parse(getString(R.string.classic_radiostream_path)));
                    if (mp.isPlaying())
                        mp.stop();
                    mpTmp.start();

                    mp = mpTmp;
                    break;
                case 2:
                    // Soul / Funk Nula
                    _playlistRepository.updateFeed(getString(R.string.channel2_rrs));
                    logo = "drawable://" + R.drawable.nula_logo_ch2;
                    skipImage = R.drawable.play_button_2;


                    MediaPlayer mpTmp2 = MediaPlayer.create(MainActivity.this, Uri.parse(getString(R.string.channel2_radiostream)));
                    if (mp.isPlaying())
                        mp.stop();
                    mpTmp2.start();

                    mp = mpTmp2;
                    break;
                case 3:
                    // Hip-Hop Nula
                    _playlistRepository.updateFeed(getString(R.string.channel3_rrs));
                    logo = "drawable://" + R.drawable.nula_logo_ch3;
                    skipImage = R.drawable.play_button_3;

                    MediaPlayer mpTmp3 = MediaPlayer.create(MainActivity.this, Uri.parse(getString(R.string.channel3_radiostream)));
                    if (mp.isPlaying())
                        mp.stop();
                    mpTmp3.start();

                    mp = mpTmp3;
                    break;
                default:
                    _radioChannel = 0;
                    ChangeChannel();
                    break;
            }

        }

    }
}
