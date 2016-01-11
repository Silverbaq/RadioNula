package com.radionula.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.radionula.radionula.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritsFragment extends Fragment {


    public FavoritsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorits, container, false);
    }

}
