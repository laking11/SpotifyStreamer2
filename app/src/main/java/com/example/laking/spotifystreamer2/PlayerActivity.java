package com.example.laking.spotifystreamer2;

import android.content.Intent;
import android.os.Bundle;

import com.example.laking.spotifystreamer2.mydata.MyBaseActivity;
import com.example.laking.spotifystreamer2.mydata.MyTrack;

import java.util.ArrayList;

public class PlayerActivity extends MyBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                ArrayList<MyTrack> t = null;
                int lPos = -1;
                if (intent.hasExtra("TRACK_LIST")) {
                    t = intent.getParcelableArrayListExtra("TRACK_LIST");
                    lPos = intent.getIntExtra("LIST_POSITION", 0);
                }
                PlayerFragment pf = PlayerFragment.newInstance(t, lPos) ;
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.player_fragment_container, pf)
                        .commit();
            }
        }
    }
}

