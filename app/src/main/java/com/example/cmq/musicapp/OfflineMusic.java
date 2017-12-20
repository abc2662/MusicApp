package com.example.cmq.musicapp;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.Duration;

/**
 * Created by Admin on 12/3/2017.
 */

public class OfflineMusic extends AppCompatActivity {
    public ListView lv;
    int READ_EXTERNAL_STORAGE_PERMISSION_CODE =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pick);
        lv = (ListView) findViewById(R.id.fileListView);
        ArrayAppend();

//        final ArrayList songsListData = new ArrayList();
////        final ListAdapter adapter = new SimpleAdapter(this, songsListData,
////                R.layout.content_pick, new String[] { "songTitle" }, new int[] {});

    }
    public void ArrayAppend()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        }
        getPlayList();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,songsList);
        lv.setAdapter(arrayAdapter);
    }

    public ArrayList songsList = new ArrayList();
    public ArrayList getPlayList(){
        File home = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
        int a = home.listFiles().length;
        if(home.listFiles().length>0)
        {
            for (File file : home.listFiles()) {
                HashMap song = new HashMap();
                song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
                song.put("songPath", file.getPath());

                // Adding each song to SongList
                songsList.add(song);
            }
        }
        // return songs list array
        return songsList;
    }
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}

