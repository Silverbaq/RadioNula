package com.radionula.radionula.fragments;


import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.radionula.radionula.model.NulaTrack;
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
    TextView tvHeader;

    public FavoritsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_favorits, container, false);

        lvFavorites = (ListView) view.findViewById(R.id.fragment_favorites_lvFavorites);
        tvHeader = (TextView)view.findViewById(R.id.fragment_favorites_tvHeader);

        List<NulaTrack> tracks = (List<NulaTrack>) MyApp.Companion.LoadUserFavorites(getActivity());
        if (tracks != null)
            setFavoritesList(tracks);

        Typeface artistFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        tvHeader.setTypeface(artistFont);

        return view;
    }

    public void setFavoritesList(List<NulaTrack> tracks){
        adapter = new PlaylistAdapter(getActivity(), tracks, PlaylistAdapter.AdapterType.REMOVE);

        lvFavorites.setAdapter(adapter);


    }

}
