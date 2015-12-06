package com.radionula.radionula;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.radionula.interfaces.IControls;


/**
 * A simple {@link Fragment} subclass.
 */
public class ControlsFragment extends Fragment {

    IControls _controls;

    ImageView ivSkip;
    ImageView ivPause;

    public ControlsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_controls, container, false);

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

}

