package com.example.cmq.musicapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    public String Title;
    public String Link;
    public String Artist;

    public Song(String title, String link) {
        Title = title;
        Link = link;
    }

    public Song(Parcel source) {
        Title = source.readString();
        Link = source.readString();
        Artist = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Title);
        dest.writeString(Link);
        dest.writeString(Artist);
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }

        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }
    };

}
