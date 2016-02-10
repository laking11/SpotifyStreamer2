package com.example.laking.spotifystreamer2.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.laking.spotifystreamer2.mydata.MyArtist;
import com.example.laking.spotifystreamer2.mydata.MyTrack;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Created by laking on 1/9/16.
 */
public class SearchService extends IntentService {
    public static final String SEARCH_STRING = "SearchService.D_SEARCH_STRING";
    public static final String SEARCH_TYPE = "SearchService.D_SEARCH_TYPE";
    public static final String COUNTRY_CODE = "SearchService.D_COUNTRY";
    public static final int SEARCH_ARTIST = 0;
    public static final int SEARCH_TRACK = 1;

    public static final String BROADCAST_ARTIST = "SearchService.B_ARTIST";
    public static final String BROADCAST_TRACK = "SearchService.B_TRACK";
    public static final String TRACK_RESULT = "SearchService.R_TRACK";
    public static final String ARTIST_RESULT = "SearchService.R_ARTIST";

    public static final String LOG_TAG = SearchService.class.getSimpleName();

    private SpotifyApi api = new SpotifyApi();
    private SpotifyService spotify = api.getService();

    public SearchService () { super("SpotifySearch"); }

    @Override
    protected void onHandleIntent(Intent intent) {

        int searchType = intent.getIntExtra(SEARCH_TYPE, -1);
        String searchString = intent.getStringExtra(SEARCH_STRING);
        String cc = "";
        if (intent.hasExtra(COUNTRY_CODE))
            cc = intent.getStringExtra(COUNTRY_CODE);

        // Intent to store the search results
        Intent resultsIntent = new Intent();

        if (searchType == SEARCH_ARTIST) {
            // execute api search
            ArrayList<MyArtist> a = searchArtist(searchString);

            // Modify intent with results
            resultsIntent.setAction(BROADCAST_ARTIST);
            resultsIntent.putParcelableArrayListExtra(ARTIST_RESULT, a);

        } else if (searchType == SEARCH_TRACK) {
            // execute api search
            ArrayList<MyTrack> t = searchTracks(searchString, cc);

            // Modify intent with results
            resultsIntent.setAction(BROADCAST_TRACK);
            resultsIntent.putParcelableArrayListExtra(TRACK_RESULT, t);
        } else {
            return;
        }

        // Use local broadcast manager to send intent with search results
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultsIntent);
    }

    private ArrayList<MyArtist> searchArtist(String artist) {
        ArtistsPager ap;
        ArrayList<MyArtist> artistList = new ArrayList<>();

        try {
            ap = spotify.searchArtists(artist);
        } catch (RetrofitError e) {
            Log.e(LOG_TAG, "Error: ", e);
            ap = null;
        }

        if (ap != null && ap.artists.items.size() > 0) {
            for (Artist a : ap.artists.items) {
                String imgUrl = "";
                if (a.images.size() > 0) {
                    int imgPos = 0;
                    if (a.images.size() > 2) {
                        imgPos = a.images.size() - 2;
                    }
                    imgUrl = a.images.get(imgPos).url;
                }
                artistList.add(new MyArtist(a.name, a.id, imgUrl));
            }
        }
        return artistList;
    }

    private ArrayList<MyTrack> searchTracks (String artistId, String cc) {
        Tracks tracks;
        ArrayList<MyTrack> trackList = new ArrayList<>();

        try {
            tracks = spotify.getArtistTopTrack(artistId,cc);
        } catch (RetrofitError e) {
            Log.e(LOG_TAG, "Error: ", e);
            tracks = null;
        }

        if (tracks != null && tracks.tracks.size() > 0) {
            for (Track t : tracks.tracks) {
                String imgUrl = "";
                String coverUrl = "";
                if (t.album.images.size() > 0) {
                    int imgPos = 0;
                    if (t.album.images.size() > 2) {
                        imgPos = t.album.images.size() - 2;
                    }
                    imgUrl = t.album.images.get(imgPos).url;
                    coverUrl = t.album.images.get(0).url;
                }
                trackList.add(new MyTrack(t.name, t.album.name, imgUrl, t.preview_url,
                        coverUrl,t.artists.get(0).name, t.external_urls.get("spotify")));

            }
        }
        return trackList;
    }
}

