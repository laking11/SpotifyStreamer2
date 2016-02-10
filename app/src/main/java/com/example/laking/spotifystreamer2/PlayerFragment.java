package com.example.laking.spotifystreamer2;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.laking.spotifystreamer2.mydata.MyTrack;
import com.example.laking.spotifystreamer2.services.PlayerService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class PlayerFragment extends DialogFragment {
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();
    private static final String STATE_PAUSE = "state_pause";

    private View root;

    private TextView artName;
    private TextView albName;
    private TextView trkName;

    private ImageView image;
    private SeekBar mProgBar;
    private TextView mCurrPos;
    private TextView mDuration;

    private ImageButton playButton;

    private int mLastSeekPos = 0;
    private boolean mPauseProgessUpdate = false;
    private boolean paused = false;
    private boolean mAutoStart = false;
    TrackReceiver mTrackRcv = new TrackReceiver();
    ProgressReceiver mProgressRcv = new ProgressReceiver();


    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance(ArrayList<MyTrack> trks, int pos) {
        Bundle args = null;
        if (trks != null) {
            args = new Bundle();
            args.putParcelableArrayList("tracks", trks);
            args.putInt("position", pos);
        }
        PlayerFragment pf = new PlayerFragment();
        pf.setArguments(args);

        return pf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null ) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                ArrayList<MyTrack> trackArray = arguments.getParcelableArrayList("tracks");
                int position = arguments.getInt("position");

                // Start service for Player with info from intent
                Intent plyrIntent = new Intent(getActivity(), PlayerService.class);
                plyrIntent.setAction(PlayerService.ACTION_PLAYLIST);
                plyrIntent.putExtra(PlayerService.DATA_PLAYLIST, trackArray);
                plyrIntent.putExtra(PlayerService.DATA_POSITION, position);
                getActivity().startService(plyrIntent);
                mAutoStart = true;
            }
        } else {
            mAutoStart = false;
            paused = savedInstanceState.getBoolean(STATE_PAUSE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mTrackRcv);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProgressRcv);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAutoStart) {
            // Auto start playing on creation
            playButton.setImageResource(android.R.drawable.ic_media_pause);
            sendToPlayer(PlayerService.ACTION_PLAY);
        } else {
            if (paused) {
                playButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            }
            sendToPlayer(PlayerService.ACTION_RESUME);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_PAUSE, paused);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter trackRcvFilter = new IntentFilter(PlayerService.BROADCAST_TRACK);
        trackRcvFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mTrackRcv, trackRcvFilter);

        IntentFilter progressRcvFilter = new IntentFilter(PlayerService.BROADCAST_PROGRESS);
        progressRcvFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mProgressRcv, progressRcvFilter);

        sendToPlayer(PlayerService.REQUEST_TRACK);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        root  = inflater.inflate(R.layout.fragment_player, container, false);

        albName = (TextView) root.findViewById(R.id.playing_album);
        trkName = (TextView) root.findViewById(R.id.playing_track);
        artName = (TextView) root.findViewById(R.id.playing_artist);
        image = (ImageView) root.findViewById(R.id.playing_image);

        mProgBar = (SeekBar) root.findViewById(R.id.playing_progressbar);
        mCurrPos = (TextView) root.findViewById(R.id.player_progress);
        mDuration = (TextView) root.findViewById(R.id.player_duration);


        playButton = (ImageButton) root.findViewById(R.id.play_pause);
        ImageButton prevButton = (ImageButton) root.findViewById(R.id.prev_track);
        ImageButton nextButton = (ImageButton) root.findViewById(R.id.next_track);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                    paused = false;
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    sendToPlayer(PlayerService.ACTION_RESUME);
                } else {
                    sendToPlayer(PlayerService.ACTION_PAUSE);
                    paused = true;
                    playButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPlayer(PlayerService.ACTION_PREV);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToPlayer(PlayerService.ACTION_NEXT);
            }
        });

        mProgBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mLastSeekPos = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPauseProgessUpdate = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sendToPlayer(PlayerService.ACTION_SEEK, mLastSeekPos);
                mPauseProgessUpdate = false;
                mLastSeekPos = 0;
            }
        });

        return root;
    }

    private void sendToPlayer (String action) {
        sendToPlayer(action, -1);
    }

    private void sendToPlayer (String action, int xtra) {
        Intent i = new Intent(getActivity(), PlayerService.class);
        i.setAction(action);
        if (xtra > -1) {
            i.putExtra(PlayerService.DATA_SEEK_POS, xtra);
        }
        getActivity().startService(i);
    }

    private class TrackReceiver extends BroadcastReceiver {

        private TrackReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<MyTrack> tList = intent.getParcelableArrayListExtra(PlayerService.DATA_TRACK);
            MyTrack t = tList.get(0);
            albName.setText(t.getAlbum());
            trkName.setText(t.getName());
            artName.setText(t.getArtist());

            String imageUrl = t.getCoverArt();
            if (imageUrl.isEmpty()) {
                image.setImageResource(R.drawable.no_image);
            } else {
                Picasso.with(context).load(imageUrl).placeholder(R.drawable.loading_test).
                        error(R.drawable.no_image).into(image);
            }
        }
    }

    private class ProgressReceiver extends BroadcastReceiver {
        private ProgressReceiver () {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPauseProgessUpdate) return ;
            String c = intent.getStringExtra(PlayerService.DATA_PROGRESS);
            String d = intent.getStringExtra(PlayerService.DATA_DURATION);
            int milliC = intent.getIntExtra(PlayerService.DATA_PROGRESS_INT, 0);
            int milliD = intent.getIntExtra(PlayerService.DATA_DURATION_INT, 0);
            boolean state = intent.getBooleanExtra(PlayerService.DATA_PLAYER_STATE, true);
            mCurrPos.setText(c);
            mDuration.setText(d);
            mProgBar.setMax(milliD);
            mProgBar.setProgress(milliC);
            //Toast.makeText(getActivity(), "Dur:" + milliD + "Cur:" + milliC, Toast.LENGTH_SHORT).show();

            if (state) {
                playButton.setImageResource(android.R.drawable.ic_media_pause);
                paused = false;
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play);
            }
            // Reset state and image if finished
            if (milliC == milliD) {
                paused = true;
                playButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }
}
