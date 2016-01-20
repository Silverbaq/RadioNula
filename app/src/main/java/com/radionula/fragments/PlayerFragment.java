package com.radionula.fragments;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.radionula.interfaces.IControls;
import com.radionula.model.NulaTrack;
import com.radionula.radionula.LoadingAdapter;
import com.radionula.radionula.MyApp;
import com.radionula.radionula.PlaylistAdapter;
import com.radionula.radionula.R;

import java.util.List;
import java.util.Observer;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

    //
    // Top of player
    RotateAnimation anim;
    RotateAnimation anim2;

    boolean playing = false;

    ImageView ivRecord;
    ImageView ivLogo;
    CircularImageView ivRecordImage;

    //
    // Playlist of player
    LinearLayout llPlaylist ;
    PlaylistAdapter adapter;
    ImageView ivFaded;

    //
    // Control of player
    IControls _controls;

    ImageView ivSkip;
    ImageView ivPause;


    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _controls = (IControls)context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);


        ivRecord = (ImageView) view.findViewById(R.id.fragment_top_ivRecord);
        ivRecordImage = (CircularImageView) view.findViewById(R.id.fragment_top_ivRecordImage);
        ivLogo = (ImageView)view.findViewById(R.id.fragment_top_ivLogo);
        llPlaylist = (LinearLayout)view.findViewById(R.id.fragment_playlist_llPlaylist);
        ivFaded = (ImageView)view.findViewById(R.id.fragment_playlist_ivShadow);

        // Sets load adapter
        LoadingAdapter loadingAdapter = new LoadingAdapter(getActivity());
        View item = loadingAdapter.getView(0, null, null);
        llPlaylist.addView(item);

        // Image spin animation
        anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(4000);

        anim2 = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim2.setInterpolator(new LinearInterpolator());
        anim2.setRepeatCount(Animation.INFINITE);
        anim2.setDuration(4000);

        ivSkip = (ImageView) view.findViewById(R.id.fragment_controls_ivSkip);
        ivPause = (ImageView) view.findViewById(R.id.fragment_controls_ivPause);

        ivSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _controls.Skip();
            }
        });

        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _controls.Pause();
            }
        });

        return view;
    }

    public void StopVinyl(){
        try {
            ivRecord.getAnimation().cancel();
            ivRecordImage.getAnimation().cancel();
            playing = false;
        } catch (Exception e){

        }
    }

    public void StartVinyl(){
        if (!playing) {
            playing = true;

            ivRecord.startAnimation(anim);
            ivRecordImage.startAnimation(anim2);

            ivLogo.bringToFront();
        }
    }

    public void SetVinylImage(String imageUrl){
        MyApp.getImageLoader().displayImage(imageUrl, ivRecordImage);
        ivLogo.bringToFront();
    }

    public void SetPlaylist(List<NulaTrack> tracks){
        adapter = new PlaylistAdapter(getActivity(), tracks, PlaylistAdapter.AdapterType.ADD);

        llPlaylist.removeAllViews();

        for (int i = 0; i < adapter.getCount(); i++){
            //
            // Set header
            if (i == 0){
                View view = getActivity().getLayoutInflater().inflate(R.layout.list_header, null);

                TextView tvHeader = ((TextView)view.findViewById(R.id.list_header_textview));
                tvHeader.setText("NOW PLAYING");

                Typeface artistFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
                tvHeader.setTypeface(artistFont);

                llPlaylist.addView(view);
            } else if (i == 1){
                View view = getActivity().getLayoutInflater().inflate(R.layout.list_header, null);

                TextView tvHeader = ((TextView) view.findViewById(R.id.list_header_textview));
                tvHeader.setText("PLAYLIST HISTORY");

                Typeface artistFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
                tvHeader.setTypeface(artistFont);


                llPlaylist.addView(view);
            }

            LinearLayout layout = new LinearLayout(getContext());
            View item = adapter.getView(i, null, null);

            llPlaylist.addView(item);

        }
        ivFaded.bringToFront();

    }

    public void UpdatePlaylist(List<NulaTrack> tracks){
        SetPlaylist(tracks);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (playing){
            ivRecord.startAnimation(anim);
            ivRecordImage.startAnimation(anim2);

            ivLogo.bringToFront();
        }
    }
}
