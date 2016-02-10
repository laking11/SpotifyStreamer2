package com.example.laking.spotifystreamer2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.laking.spotifystreamer2.mydata.MyBaseActivity;
import com.example.laking.spotifystreamer2.mydata.MyTrack;

import java.util.ArrayList;

public class MainActivity extends MyBaseActivity implements
        ArtistFragment.OnFragmentListener,
        TracksFragment.OnFragmentListener {

    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // check if track fragment container exists.  If so we
        // have 2 pane layout
        if (findViewById(R.id.tracks_fragment_container) != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_nowplaying) {
            if (mTwoPane) {
                PlayerFragment f = PlayerFragment.newInstance(null, -1);
                f.show(getSupportFragmentManager(), "PlayerDialog");
            } else {
                startActivity(new Intent(this, PlayerActivity.class));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTrackSelected(ArrayList<MyTrack> trks, int position) {
        PlayerFragment pf = PlayerFragment.newInstance(trks, position);
        pf.show(getSupportFragmentManager(), "PlayerDialog");
    }

    @Override
    public void onArtistSelected(String artistId) {
        if (mTwoPane) {

            TracksFragment tf = TracksFragment.newInstance(artistId);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tracks_fragment_container, tf, "TopTracks")
                    .addToBackStack("TrackBefore")
                    .commit();

        } else {
            Intent trkIntent = new Intent(this, TracksActivity.class)
                    .putExtra(Intent.EXTRA_TEXT, artistId);
                startActivity(trkIntent);
        }
    }

    @Override
    public void onSearchStarted() {
        if (mTwoPane) {
            //  Clear Top Track pane by creating new empty fragment
            //  TODO:  Better way to do this?
            TracksFragment tf = new TracksFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tracks_fragment_container, tf)
                    .commit();
        }
    }
}
