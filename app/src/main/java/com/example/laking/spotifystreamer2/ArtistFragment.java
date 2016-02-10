package com.example.laking.spotifystreamer2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.laking.spotifystreamer2.adapters.ArtistAdapter;
import com.example.laking.spotifystreamer2.mydata.MyArtist;
import com.example.laking.spotifystreamer2.services.SearchService;

import java.util.ArrayList;


public class ArtistFragment extends Fragment {

    private static final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private static final String STATE_ARTIST = "state_artist";

    private ArtistAdapter mArtistAdapter;
    private ArrayList<MyArtist> mArtistList;
    private SearchReceiver mSearchRcv = new SearchReceiver();

    private SearchView sv;
    private ListView lv;


    //private OnFragmentInteractionListener mListener;

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if have instanceState
        if (savedInstanceState != null) {
            mArtistList = savedInstanceState.getParcelableArrayList(STATE_ARTIST);
        } else {
            mArtistList = new ArrayList<MyArtist>();
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
        IntentFilter artistRcvFilter = new IntentFilter(SearchService.BROADCAST_ARTIST);
        artistRcvFilter.addCategory(Intent.CATEGORY_DEFAULT);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mSearchRcv, artistRcvFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root  = inflater.inflate(R.layout.fragment_artist, container, false);

        //  Setup the text enter view
        sv = (SearchView) root.findViewById(R.id.edit_search);
        sv.setQueryHint(getString(R.string.enter_artist));
        sv.setIconified(false);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String keyWord = sv.getQuery().toString();
                if (!keyWord.isEmpty()) {
                    startSearch(keyWord);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Set up adapter
        mArtistAdapter = new ArtistAdapter(getActivity(), mArtistList);
        lv = (ListView) root.findViewById(R.id.list_search_results);
        lv.setAdapter(mArtistAdapter);

        // Add item click listener that will launch top tracks activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String artistId = mArtistAdapter.getItem(position).getId();
                ((OnFragmentListener) getActivity()).onArtistSelected(artistId);
            }
        });

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_ARTIST, mArtistList);
    }

    private void startSearch(String name) {
        // Launch search service with intent with search name
        Intent i = new Intent(getActivity(), SearchService.class);
        i.putExtra(SearchService.SEARCH_STRING, name);
        i.putExtra(SearchService.SEARCH_TYPE,SearchService.SEARCH_ARTIST);
        getActivity().startService(i);
        ((OnFragmentListener) getActivity()).onSearchStarted();
    }

    private class SearchReceiver extends BroadcastReceiver {
        private SearchReceiver () {}

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<MyArtist> aList =
                    intent.getParcelableArrayListExtra(SearchService.ARTIST_RESULT);

            mArtistAdapter.clear();
            if (aList.size() > 0 ) {
                for (MyArtist a : aList) {
                    mArtistList.add(a);
                }
                mArtistAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), sv.getQuery().toString()
                                + " " + getString(R.string.artist_not_found),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentListener {
        void onArtistSelected(String artistId);
        void onSearchStarted();
    }
}
