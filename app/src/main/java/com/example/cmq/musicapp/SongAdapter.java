package com.example.cmq.musicapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by CMQ on 01-Jan-18.
 */

public class SongAdapter extends ArrayAdapter<Song> {
    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Song song = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item, parent, false);
        }
        // Lookup view for data population
        TextView itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
        TextView itemArtist = (TextView) convertView.findViewById(R.id.itemArtist);
        ImageView itemImage = (ImageView) convertView.findViewById(R.id.itemImage);
        // Populate the data into the template view using the data object
        itemTitle.setText(song.Title);
        itemArtist.setText(song.Artist);
        // TODO: Set itemImage 's image to album cover...
        // Return the completed view to render on screen
        return convertView;
    }
}
