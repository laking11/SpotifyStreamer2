package com.example.laking.spotifystreamer2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.laking.spotifystreamer2.adapters.TracksAdapter;
import com.example.laking.spotifystreamer2.mydata.MyTrack;
import com.example.laking.spotifystreamer2.services.SearchService;

import java.util.ArrayList;

public class TracksFragment extends Fragment {
    private final String LOG_TAG = TracksFragment.class.getSimpleName();
    private static final String STATE_TRACKS = "state_tracks";

    private TracksAdapter mTrackAdapter;
    private ArrayList<MyTrack> mTrackList;
    private SearchReceiver mSearchRcv = new SearchReceiver();
    private String artistId;

    public interface OnFragmentListener {
        void onTrackSelected(ArrayList<MyTrack> trks, int position);
    }

    public TracksFragment() {
        // Required empty public constructor
    }

    public static TracksFragment newInstance(String artist) {
        TracksFragment tf = new TracksFragment();

        Bundle args = new Bundle();
        args.putString("artist", artist);
        tf.setArguments(args);

        return tf;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) artistId = arguments.getString("artist");

        // Check if have instanceState
        if (savedInstanceState != null) {
            mTrackList = savedInstanceState.getParcelableArrayList(STATE_TRACKS);
        } else {
            mTrackList = new ArrayList<MyTrack>();
        }
         if (artistId != null && !artistId.isEmpty()) {
            startSearch(artistId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSearchRcv);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter artistRcvFilter = new IntentFilter(SearchService.BROADCAST_TRACK);
        artistRcvFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mSearchRcv, artistRcvFilter);

   }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_TRACKS, mTrackList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_tracks, container, false);

        mTrackAdapter = new TracksAdapter(getActivity(), mTrackList);

        ListView lv = (ListView) root.findViewById(R.id.list_artist_results);
        lv.setAdapter(mTrackAdapter);

        // Add item click listener that will launch top tracks activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((OnFragmentListener) getActivity()).onTrackSelected(mTrackList, position);
            }
        });

        return root;
    }

    private void startSearch(String name) {
        // Launch search service with intent with search name
        Intent i = new Intent(getActivity(), SearchService.class);
        i.putExtra(SearchService.SEARCH_STRING, name);
        i.putExtra(SearchService.SEARCH_TYPE, SearchService.SEARCH_TRACK);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cc = pref.getString(getString(R.string.pref_country_key),
                getString(R.string.pref_country_default));
        i.putExtra(SearchService.COUNTRY_CODE,cc);
        getActivity().startService(i);
    }

    private class SearchReceiver extends BroadcastReceiver {
        private SearchReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<MyTrack> tList =
                    intent.getParcelableArrayListExtra(SearchService.TRACK_RESULT);

            mTrackAdapter.clear();
            if (tList.size() > 0) {
                for (MyTrack t : tList) {
                    mTrackList.add(t);
                }
                mTrackAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), R.string.tracks_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

