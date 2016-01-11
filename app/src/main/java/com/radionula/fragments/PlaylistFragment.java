package com.radionula.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.radionula.model.NulaTrack;
import com.radionula.radionula.PlaylistAdapter;
import com.radionula.radionula.R;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends Fragment {

    LinearLayout llPlaylist ;
    PlaylistAdapter adapter;

    public PlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        llPlaylist = (LinearLayout)view.findViewById(R.id.fragment_playlist_llPlaylist);

        return view;
    }

    public void SetPlaylist(List<NulaTrack> tracks){
        adapter = new PlaylistAdapter(getActivity(), tracks);

        llPlaylist.removeAllViews();

        for (int i = 0; i < adapter.getCount(); i++){
            LinearLayout layout = new LinearLayout(getContext());
            View item = adapter.getView(i, null, null);
            llPlaylist.addView(item);
        }
    }

    public void UpdatePlaylist(List<NulaTrack> tracks){
        adapter.updateList(tracks);
        SetPlaylist(tracks);
        //adapter.notifyDataSetChanged();
    }
}
