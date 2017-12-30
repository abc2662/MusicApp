package com.example.cmq.musicapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 12/3/2017.
 */

public class OfflineMusicActivity extends AppCompatActivity {
    public static final String SONG_TITLE_TAG = "songTitle";
    public static final String SONG_IMAGE_TAG = "songImage";

    public ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_music);
        AskPermission();
        listView = (ListView) findViewById(R.id.listViewResults);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = Paths[position].toString();
                Log.w("Current Path", path);
                playMusic(path);
            }
        });

    }

    public void playMusic(String path) {
        Intent playmusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
        playmusicIntent.putExtra(getString(R.string.musiclinkdata), path);
        playmusicIntent.putExtra(getString(R.string.playMusicrequest), 0);
        startActivity(playmusicIntent);
    }

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 3;

    public void AskPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        } else {
            ArrayAppend();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_CODE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ArrayAppend();
                } else {
                    Log.e("Permisson", "Denied");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void ArrayAppend() {
        getPlayList();
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                this, songList,
                R.layout.list_view_item,
                new String[] {SONG_TITLE_TAG, SONG_IMAGE_TAG},
                new int[] {R.id.itemTitle, R.id.itemImage});
        listView.setAdapter(simpleAdapter);
    }

    public ArrayList songList = new ArrayList();
    public ArrayList<String> songPaths = new ArrayList<String>();
    public String[] Paths;
    ArrayList<Song> songArray = new ArrayList<Song>();
    public ArrayList getPlayList() {
        String localMusicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        Log.w("Files", "Path: " + localMusicPath);
        File directory = new File(localMusicPath);
        if (directory.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : directory.listFiles()) {
                HashMap song = new HashMap();
                String link = file.getPath();
                String title = file.getName().substring(0, (file.getName().length() - 4));
                song.put(SONG_TITLE_TAG, title);
                song.put(SONG_IMAGE_TAG, R.drawable.icon_music);
                //song.put("songPath", file.getPath());

                //Adding each song link to SongPaths to start Mediaplayer
                songPaths.add(link);
                // Adding each song to SongList to show on ListView
                songList.add(song);
                //Adding each song to songArray for mediaplayer;
                songArray.add(new Song(title, link));
            }
            Paths = songPaths.toArray(new String[songPaths.size()]);
        }
        // return songs list array
        return songArray;
    }

    class FileExtensionFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }

}

