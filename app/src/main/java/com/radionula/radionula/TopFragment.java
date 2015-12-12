package com.radionula.radionula;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
import com.androidquery.callback.ImageOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {


    public TopFragment() {
        // Required empty public constructor
    }

    ImageView ivRecord;
    ImageView ivRecordImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top, container, false);

        ivRecord = (ImageView) view.findViewById(R.id.fragment_top_ivRecord);
        ivRecordImage = (ImageView) view.findViewById(R.id.fragment_top_ivRecordImage);

        return view;
    }

    public void StopVinyl(){
        try {
            ivRecord.getAnimation().cancel();
        } catch (Exception e){

        }
    }

    public void StartVinyl(){
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(4000);

        ivRecord.startAnimation(anim);
    }

    public void SetVinylImage(String imageUrl){
        // TODO: Implement round image

    }





}
