package com.radionula.radionula.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.radionula.radionula.R;

public class NoConnectionFragment extends Fragment {


    public NoConnectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_no_connection, container, false);

        TextView textview1 = (TextView) view.findViewById(R.id.fragment_no_connection_textview1);
        TextView textview2 = (TextView) view.findViewById(R.id.fragment_no_connection_textview2);

        Typeface artistFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");

        textview1.setTypeface(artistFont);
        textview2.setTypeface(artistFont);


        return view;
    }

}
