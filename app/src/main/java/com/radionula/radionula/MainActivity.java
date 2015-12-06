package com.radionula.radionula;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.radionula.interfaces.IControls;

public class MainActivity extends AppCompatActivity implements IControls {

    private DrawerLayout mDrawer;
    ImageView navButton;

    TopFragment topFragment;
    ControlsFragment controlFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (ImageView)findViewById(R.id.nav_Button);

        topFragment = (TopFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_fragmentTop);
        controlFragment = (ControlsFragment) getSupportFragmentManager().findFragmentById(R.id.activityMain_fragmentControls);



        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(GravityCompat.START);
            }
        });

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
    }

    @Override
    public void Pause() {
        topFragment.StopVinyl();
    }
}
