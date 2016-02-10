package com.example.laking.spotifystreamer2.mydata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import com.example.laking.spotifystreamer2.PlayerActivity;
import com.example.laking.spotifystreamer2.R;
import com.example.laking.spotifystreamer2.SettingsActivity;
import com.example.laking.spotifystreamer2.services.PlayerService;

import java.util.ArrayList;

/**
 * Created by laking on 1/29/16.
 */
public class MyBaseActivity extends AppCompatActivity  {
    private MenuItem menuShare;
    private TrackReceiver mTrackRcv = new TrackReceiver();

    public MyBaseActivity () {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        menuShare = menu.findItem(R.id.action_share);
        menuShare.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else
        if (id == R.id.action_nowplaying) {
            startActivity(new Intent(this, PlayerActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mTrackRcv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent i = new Intent(this, PlayerService.class);
        i.setAction(PlayerService.REQUEST_TRACK);
        startService(i);
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        IntentFilter trackRcvFilter = new IntentFilter(PlayerService.BROADCAST_TRACK);
        trackRcvFilter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mTrackRcv, trackRcvFilter);
    }

    private Intent createShareIntent(String text) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        return share;
    }

    private void updateShareIntent (String text) {
        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (menuShare != null ) {
            menuShare.setVisible(true);

            ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuShare);

            //menuShare.getActionProvider();

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareIntent(text));
            }
        }
    }
    private class TrackReceiver extends BroadcastReceiver {

        private TrackReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<MyTrack> tList = intent.getParcelableArrayListExtra(PlayerService.DATA_TRACK);
            MyTrack t = tList.get(0);
            if (t != null ) {
                String toSend = "Check out " + t.getArtist() + "'s song " + t.getName() + "\n";
                toSend = toSend + "Go here to listen\n\n";
                toSend = toSend + t.getStream();
                updateShareIntent(toSend);
            }
        }
    }

}
