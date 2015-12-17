package com.radionula.radionula;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.radionula.model.NulaTrack;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends Fragment {

    ListView lvPlaylist;
    PlaylistAdapter adapter;

    public PlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        lvPlaylist = (ListView)view.findViewById(R.id.fragment_playlist_listview);

        return view;
    }

    public void SetPlaylist(List<NulaTrack> tracks){
        adapter = new PlaylistAdapter(getActivity(), tracks);
        lvPlaylist.setAdapter(adapter);
    }

    public void UpdatePlaylist(List<NulaTrack> tracks){
        adapter.updateList(tracks);
        //adapter.notifyDataSetChanged();
    }
}
