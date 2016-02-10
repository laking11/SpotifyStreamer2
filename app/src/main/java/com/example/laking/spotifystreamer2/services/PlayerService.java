package com.example.laking.spotifystreamer2.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.RemoteViews;

import com.example.laking.spotifystreamer2.MainActivity;
import com.example.laking.spotifystreamer2.R;
import com.example.laking.spotifystreamer2.mydata.MyTrack;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by laking on 1/17/16.
 */
public class PlayerService extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String ACTION_PLAY = "PlayerService.A_PLAY";
    public static final String ACTION_PAUSE = "PlayerService.A_PAUSE";
    public static final String ACTION_RESUME = "PlayerService.A_RESUME";
    public static final String ACTION_PLAYLIST = "PlayerService.A_PLAYLIST";
    public static final String ACTION_NEXT = "PlayerService.A_NEXT";
    public static final String ACTION_PREV = "PlayerService.A_PREV";
    public static final String ACTION_SEEK = "PlayerService.A_SEEK";
    public static final String DATA_SEEK_POS = "PlayerService.D_SEEK_POS";
    public static final String DATA_PLAYLIST = "PlayerService.D_PLAYLIST";
    public static final String DATA_POSITION = "PlayerService.D_POSITION";
    public static final String BROADCAST_TRACK = "PlayerService.B_TRACK";
    public static final String REQUEST_TRACK = "PlayerService.R_TRACK";
    public static final String DATA_TRACK = "PlayerService.D_TRACK";
    public static final String BROADCAST_PROGRESS = "PlayerService.B_PROGRESS";
    public static final String DATA_DURATION = "PlayerService.D_DURATION";
    public static final String DATA_PROGRESS = "PlayerService.D_PROGRESS";
    public static final String DATA_DURATION_INT = "PlayerService.D_DURATION_INT";
    public static final String DATA_PROGRESS_INT = "PlayerService.D_PROGRESS_INT";
    public static final String DATA_PLAYER_STATE = "PlayerService.D_PLAYER_STATE";

    final int NOTIFICATION_ID = 222;

    private MediaPlayer mPlayer = null;
    private WifiManager.WifiLock mWifiLock;
    private ArrayList<MyTrack> mPlaylist = new ArrayList<>();
    private int mPosition = 0;
    private MyTrack mCurrentTrack;
    private ProgessTask mProgTask;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mNotificationBuilder = null;
    Notification mNotification = null;

    RemoteViews rView = null;

    public PlayerService () {}

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        rView = new RemoteViews(getPackageName(), R.layout.notification_playback);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        if (action.equals(ACTION_PLAYLIST)) {
            ArrayList<MyTrack> tmpPlaylist = intent.getParcelableArrayListExtra(DATA_PLAYLIST);
            int tmpPos = intent.getIntExtra(DATA_POSITION, 0);
            setPlaylist(tmpPlaylist, tmpPos);
        }
        else if (action.equals(ACTION_SEEK))
            seekTo(intent.getIntExtra(DATA_SEEK_POS, 0));
        else if (action.equals(ACTION_PLAY))
            playSong();
        else if (action.equals(ACTION_PAUSE))
            pauseSong();
        else if (action.equals(ACTION_RESUME))
            resumeSong();
        else if (action.equals(ACTION_NEXT))
            playNext();
        else if (action.equals(ACTION_PREV))
            playPrev();
        else if (action.equals(REQUEST_TRACK))
            updateTrack();

        return START_NOT_STICKY;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        resumeSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateProgress(true);
        stopForeground(false);
    }

    private void setPlaylist(ArrayList<MyTrack> ply, int pos) {
        mPlaylist = ply;
        mPosition = pos;
    }

    private void initMediaPlayer () {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.setOnCompletionListener(this);
            setupAsForeground();
        } else {
            if (mPlayer.isPlaying()) mPlayer.stop();
            if (mProgTask != null) mProgTask.cancel(true);

            mPlayer.reset();
        }
    }

    private void playSong () {
        mCurrentTrack = mPlaylist.get(mPosition);
        try {
            initMediaPlayer();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(getApplicationContext(), Uri.parse(mCurrentTrack.getStream()));
            mPlayer.prepareAsync();
            updateTrack();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNext () {
        if (mPosition == mPlaylist.size() - 1) {
            mPosition = 0;
        } else {
            ++mPosition;
        }
        playSong();
    }

    private void playPrev () {
        if (mPosition == 0) {
            mPosition = mPlaylist.size() - 1 ;
        } else {
            --mPosition;
        }
        playSong();
    }

    private void pauseSong () {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            stopForeground(false);
            if (mProgTask != null) mProgTask.cancel(true);
            if (mWifiLock.isHeld())
                mWifiLock.release();
            updateProgress(false);
        }
    }

    private void resumeSong () {
        if (!mWifiLock.isHeld())
            mWifiLock.acquire();
        mPlayer.start();
        startForeground(NOTIFICATION_ID, mNotification);
        mProgTask = new ProgessTask();
        mProgTask.execute();
    }

    private void seekTo (int pos) {
        mPlayer.seekTo(pos);
    }

    private void updateTrack () {
        if (mCurrentTrack != null) {
            ArrayList<MyTrack> tmp = new ArrayList<>();
            tmp.add(mCurrentTrack);
            Intent iBroadcast = new Intent(PlayerService.BROADCAST_TRACK);
            iBroadcast.putExtra(PlayerService.DATA_TRACK, tmp);
            LocalBroadcastManager.getInstance(this).sendBroadcast(iBroadcast);

            if (mNotification != null)
                updateNotification();
        }
    }

    private void updateProgress (boolean finished) {
        Intent iProgress = new Intent(PlayerService.BROADCAST_PROGRESS);

        int milliDur = mPlayer.getDuration();
        int milliCurr = mPlayer.getCurrentPosition();
        // Cleanup of current pos when stop being milliseconds less than duration
        if (finished) {
            milliCurr = milliDur;
        }
        // Calculate milliseconds to minutes/seconds here on background
        // thread.  This is called through AsyncTask.
        String duration = String.format("%01d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliDur),
                TimeUnit.MILLISECONDS.toSeconds(milliDur));

        String current = String.format("%01d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliCurr),
                TimeUnit.MILLISECONDS.toSeconds(milliCurr));
        iProgress.putExtra(PlayerService.DATA_DURATION, duration);
        iProgress.putExtra(PlayerService.DATA_PROGRESS, current);
        iProgress.putExtra(PlayerService.DATA_DURATION_INT, milliDur);
        iProgress.putExtra(PlayerService.DATA_PROGRESS_INT, milliCurr);
        if (mPlayer.isPlaying()) {
            iProgress.putExtra(PlayerService.DATA_PLAYER_STATE, true);
        } else {
            iProgress.putExtra(PlayerService.DATA_PLAYER_STATE, false);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(iProgress);
    }

    void updateNotification() {

        rView.setTextViewText(R.id.note_artist, mCurrentTrack.getArtist());
        rView.setTextViewText(R.id.note_song, mCurrentTrack.getName());
        Picasso.with(this).load(mCurrentTrack.getThumb()).into(rView, R.id.note_image,
                NOTIFICATION_ID, mNotification);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // if preference set for notification and lock controls
        if (pref.getBoolean(getString(R.string.pref_notification_key), true)) {
            mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            rView.setViewVisibility(R.id.note_next, View.VISIBLE);
            rView.setViewVisibility(R.id.note_play, View.VISIBLE);
            rView.setViewVisibility(R.id.note_pause, View.VISIBLE);
            rView.setViewVisibility(R.id.note_prev, View.VISIBLE);
        } else {
            mNotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            rView.setViewVisibility(R.id.note_next, View.GONE);
            rView.setViewVisibility(R.id.note_play, View.GONE);
            rView.setViewVisibility(R.id.note_pause, View.GONE);
            rView.setViewVisibility(R.id.note_prev, View.GONE);

        }

        mNotification = mNotificationBuilder.build();
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

    }

    private Intent getPlayerIntent (String action) {
        Intent i = new Intent(this, PlayerService.class);
        i.setAction(action);
        return i;
    }

    private void setupAsForeground () {

        rView.setOnClickPendingIntent(
                R.id.note_prev,
                PendingIntent.getService(this, 0, getPlayerIntent(ACTION_PREV),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        rView.setOnClickPendingIntent(
                R.id.note_play,
                PendingIntent.getService(this, 0, getPlayerIntent(ACTION_RESUME),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        rView.setOnClickPendingIntent(
                R.id.note_pause,
                PendingIntent.getService(this, 0, getPlayerIntent(ACTION_PAUSE),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );
        rView.setOnClickPendingIntent(
                R.id.note_next,
                PendingIntent.getService(this, 0, getPlayerIntent(ACTION_NEXT),
                        PendingIntent.FLAG_UPDATE_CURRENT)

        );

        // Build the notification object.
        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContent(rView)
                .setOngoing(true);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(pi);

        mNotification = mNotificationBuilder.build();
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        updateNotification();

        startForeground(NOTIFICATION_ID, mNotification);

   }


    @Override
    public void onDestroy() {
        if (mWifiLock.isHeld())
            mWifiLock.release();
        mPlayer.release();
        stopForeground(true);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ProgessTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mPlayer.isPlaying()) return null;
                updateProgress(false);
            }
            return null;
        }
    }

}





