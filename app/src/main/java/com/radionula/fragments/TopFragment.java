package com.radionula.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.radionula.radionula.MyApp;
import com.radionula.radionula.R;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {

    RotateAnimation anim;
    RotateAnimation anim2;

    boolean playing = false;


    public TopFragment() {
        // Required empty public constructor
    }

    ImageView ivRecord;
    ImageView ivLogo;
    CircularImageView ivRecordImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top, container, false);

        ivRecord = (ImageView) view.findViewById(R.id.fragment_top_ivRecord);
        ivRecordImage = (CircularImageView) view.findViewById(R.id.fragment_top_ivRecordImage);
        ivLogo = (ImageView)view.findViewById(R.id.fragment_top_ivLogo);

        // Image spin animation
        anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(4000);

        anim2 = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim2.setInterpolator(new LinearInterpolator());
        anim2.setRepeatCount(Animation.INFINITE);
        anim2.setDuration(4000);

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
        playing = true;

        ivRecord.startAnimation(anim);
        ivRecordImage.startAnimation(anim2);

        ivLogo.bringToFront();
    }

    public void SetVinylImage(String imageUrl){
        MyApp.getImageLoader().displayImage(imageUrl, ivRecordImage);

        // Load image, decode it to Bitmap and return Bitmap to callback
        /*MyApp.getImageLoader().loadImage(imageUrl, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                ivRecordImage.setImageBitmap(loadedImage);
            }
        });
        */
        ivLogo.bringToFront();
    }


}
