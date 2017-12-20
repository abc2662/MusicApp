package com.example.cmq.musicapp;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
    public String[] from={"songTitle","image"};
    public int[] to={R.id.tvOnlist,R.id.imgvOnlist};
    public ArrayList songsList = new ArrayList();
    public ArrayList<String> songPaths = new ArrayList<String>();
    public String[] Paths;
    ArrayList<Song> arraySong = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pick);
        lv = (ListView) findViewById(R.id.listViewResults);
        final android.widget.TextView tvItem = (android.widget.TextView)findViewById(R.id.tvOnlist);
        ArrayAppend();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.w("Current Path",Paths[position].toString());
                android.widget.TextView tvItem = (android.widget.TextView)findViewById(R.id.tvOnlist);
                tvItem.setTextColor(Color.BLUE);
                Intent playmusicIntent = new Intent(getApplicationContext(),PlayMusicActivity.class );
                playmusicIntent.putExtra(getString(R.string.playMusicrequest),0);
                playmusicIntent.putExtra(getString(R.string.musiclinkdata),Paths[position].toString());
                startActivity(playmusicIntent);
            }
        });
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
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,songsList,R.layout.list_view_item,from,to);
        lv.setAdapter(simpleAdapter);
    }


    public ArrayList getPlayList(){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        Log.w("Files", "Path: " + path);
        File directory = new File(path);
        if(directory.listFiles(new FileExtensionFilter()).length>0)
        {
            for (File file : directory.listFiles()) {
                HashMap song = new HashMap();
                String link = file.getPath();
                String title = file.getName().substring(0, (file.getName().length() - 4));
                song.put("songTitle", title );
                song.put("image", R.drawable.icon_music);
                //song.put("songPath", file.getPath());
                //Adding each song link to SongPaths to startMediaplayer
                songPaths.add(link);
                // Adding each song to SongList to show on ListView
                songsList.add(song);
                //Adding each song to arraySong for mediaplayer;
                arraySong.add(new Song(title,link));
            }
            Paths = songPaths.toArray(new String[songPaths.size()]);
        }
//        PlayMusicActivity playmusic = new PlayMusicActivity();
//        playmusic.AddSongs(arraySong);
        // return songs list array
        return arraySong;
    }

    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }

}

