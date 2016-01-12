package com.radionula.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.radionula.model.NulaTrack;
import com.radionula.radionula.MyApp;
import com.radionula.radionula.PlaylistAdapter;
import com.radionula.radionula.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritsFragment extends Fragment {

    ListView lvFavorites;
    PlaylistAdapter adapter;

    public FavoritsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_favorits, container, false);

        lvFavorites = (ListView) view.findViewById(R.id.fragment_favorites_lvFavorites);

        setFavoritesList((List<NulaTrack>) MyApp.LoadUserFavorites(getActivity()));

        return view;
    }

    public void setFavoritesList(List<NulaTrack> tracks){
        adapter = new PlaylistAdapter(getActivity(), tracks, PlaylistAdapter.AdapterType.REMOVE);

        lvFavorites.setAdapter(adapter);

        /*

        llFavorites.removeAllViews();

        for (int i = 0; i < adapter.getCount(); i++){
            LinearLayout layout = new LinearLayout(getContext());
            View view = adapter.getView(i, null, null);

            llFavorites.addView(view);
        }
        */
    }

}
