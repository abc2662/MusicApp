package com.example.cmq.musicapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Song implements Parcelable {
    @NonNull private String Title;
    @NonNull private String Link;
    @NonNull private String Artist;

    public Song(@NonNull String link) {
        Link = link;
        Title = "";
        Artist = "";
        if(link.contains("http")) {
            return;
        }
        int beginIndex = link.lastIndexOf('/');
        int endIndex = link.lastIndexOf('.');
        if (!(beginIndex < 0 || endIndex < 0)) {
            Title = link.substring(beginIndex + 1, endIndex);
        }
    }

    public Song(Parcel source) {
        Title = source.readString();
        Link = source.readString();
        Artist = source.readString();
    }

    public String getTitle() { return Title; }
    public String getLink() { return Link; }
    public String getArtist() { return Artist; }

    public void setTitle(String title) {
        if (title != null)
            Title = title;
    }
    public void setLink(String link) {
        if (link != null)
            Link = link;
    }
    public void setArtist(String artist) {
        if (artist != null)
            Artist = artist;
    }

    public Bitmap getImage()
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(Link);
        byte[] artBytes = mmr.getEmbeddedPicture();
        if(artBytes!=null)
        {
            return BitmapFactory.decodeByteArray(artBytes,0,artBytes.length);
        }
        return  null;
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
