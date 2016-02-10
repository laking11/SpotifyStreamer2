package com.example.laking.spotifystreamer2.mydata;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class to store artist info from spotify so it can
 * be Parceable
 */
public class MyArtist implements Parcelable {
    private String _name;
    private String _id;
    private String _imageUri;

    public MyArtist(String name, String id, String uri) {
        _name = name;
        _id = id;
        _imageUri = uri;
    }

    public String getId () { return _id; }
    public String getName () {return _name; }
    public String getImage () {return _imageUri; }

    private MyArtist(Parcel in) {
        _name = in.readString();
        _id = in.readString();
        _imageUri = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_name);
        dest.writeString(_id);
        dest.writeString(_imageUri);
    }

    public static final Parcelable.Creator<MyArtist> CREATOR
            = new Parcelable.Creator<MyArtist>() {

        @Override
        public MyArtist createFromParcel(Parcel parcel) {
            return new MyArtist(parcel);
        }

        @Override
        public MyArtist[] newArray(int size) {
            return new MyArtist[size];
        }
    };
}
