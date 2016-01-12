package com.radionula.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.radionula.model.NulaTrack;
import com.radionula.radionula.MyApp;
import com.radionula.radionula.PlaylistAdapter;
import com.radionula.radionula.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritsFragment extends Fragment {

    LinearLayout llFavorites;
    PlaylistAdapter adapter;

    public FavoritsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_favorits, container, false);

        llFavorites = (LinearLayout) view.findViewById(R.id.fragment_favorites_llFavorites);

        setFavoritesList((List<NulaTrack>) MyApp.LoadUserFavorites(getActivity()));

        return view;
    }

    public void setFavoritesList(List<NulaTrack> tracks){
        adapter = new PlaylistAdapter(getActivity(), tracks);

        llFavorites.removeAllViews();

        for (int i = 0; i < adapter.getCount(); i++){
            LinearLayout layout = new LinearLayout(getContext());
            View item = adapter.getView(i, null, null);

            llFavorites.addView(item);
        }
    }

}
