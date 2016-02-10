package com.example.laking.spotifystreamer2;

import android.content.Intent;
import android.os.Bundle;

import com.example.laking.spotifystreamer2.mydata.MyBaseActivity;
import com.example.laking.spotifystreamer2.mydata.MyTrack;

import java.util.ArrayList;

public class TracksActivity extends MyBaseActivity implements
        TracksFragment.OnFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String artistId = intent.getStringExtra(Intent.EXTRA_TEXT);
                TracksFragment tf = TracksFragment.newInstance(artistId);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.tracks_fragment_container, tf)
                        .commit();
            }
        }
    }

    @Override
    public void onTrackSelected(ArrayList<MyTrack> trks, int position) {
        Intent plyrIntent = new Intent(this, PlayerActivity.class)
                .putExtra("TRACK_LIST", trks);
        plyrIntent.putExtra("LIST_POSITION", position);
        startActivity(plyrIntent);
    }
}
