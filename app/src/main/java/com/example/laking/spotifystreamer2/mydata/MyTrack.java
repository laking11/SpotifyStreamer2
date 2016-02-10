package com.example.laking.spotifystreamer2.mydata;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *  Class to store off artist track info
 */
public class MyTrack implements Parcelable {

    private String _name;
    private String _album;
    private String _thumbUri;
    private String _streamUri;
    private String _coverArtUri;
    private String _artist;
    private String _extUrl;

    public MyTrack(String name, String album, String thumb, String strm,
                   String cover, String artist, String ext) {
        _name = name;
        _album = album;
        _thumbUri = thumb;
        _streamUri = strm;
        _coverArtUri = cover;
        _artist = artist;
        _extUrl = ext;
    }

    public String getAlbum () { return _album; }
    public String getName () {return _name; }
    public String getThumb() {return _thumbUri; }
    public String getStream() {return _streamUri; }
    public String getCoverArt() {return _coverArtUri; }
    public String getArtist() {return _artist; }
    public String getExternal() {return _extUrl; }

    private MyTrack(Parcel in) {
        _name = in.readString();
        _album = in.readString();
        _thumbUri = in.readString();
        _streamUri = in.readString();
        _coverArtUri = in.readString();
        _artist = in.readString();
        _extUrl = in.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_name);
        dest.writeString(_album);
        dest.writeString(_thumbUri);
        dest.writeString(_streamUri);
        dest.writeString(_coverArtUri);
        dest.writeString(_artist);
        dest.writeString(_extUrl);
    }

    public static final Parcelable.Creator<MyTrack> CREATOR
            = new Parcelable.Creator<MyTrack>() {

        @Override
        public MyTrack createFromParcel(Parcel parcel) {
            return new MyTrack(parcel);
        }

        @Override
        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };
}
