package com.example.cmq.musicapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by CMQ on 01-Jan-18.
 */

public class SongAdapter extends ArrayAdapter<Song> implements Filterable {
    private ArrayList<Song> songList;
    private ArrayList<Song> filteredSongList;
    private SongFilter mFilter = new SongFilter();

    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
        songList = songs;
        filteredSongList = songs;
    }

    @Nullable
    @Override
    public Song getItem(int position) {
        return filteredSongList.get(position);
    }

    @Override
    public int getCount() {
        return filteredSongList.size();
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

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class SongFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();
            ArrayList<Song> filterResults = new ArrayList<Song>();

            int size = songList.size();
            Song filteringSong;
            for (int i = 0; i < size; i++) {
                filteringSong = songList.get(i);
                if (filteringSong.Title.toLowerCase().contains(filterString) ||
                        filteringSong.Artist.toLowerCase().contains(filterString)) {
                    filterResults.add(filteringSong);
                }
            }

            results.values = filterResults;
            results.count = filterResults.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredSongList = (ArrayList<Song>)results.values;
            notifyDataSetChanged();
        }
    }
}
