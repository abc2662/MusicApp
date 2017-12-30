package com.example.cmq.musicapp;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.Duration;



public class OfflineMusicActivity extends AppCompatActivity {
    public ListView lv;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 3;
    public String[] from={"songTitle","image","artist"};
    public int[] to={R.id.tvtitleOnlist,R.id.imgvOnlist,R.id.tvtartistOnlist};
    public ArrayList songsList = new ArrayList();
    public ArrayList<String> songPaths = new ArrayList<String>();
    public ArrayList<String> songTitles = new ArrayList<String>();
    public String[] Paths;
    public String[] Titles;
    public SearchView searchView;

    static MediaPlayer mediaPlayer = new MediaPlayer() ;
    ArrayList<Song> arraySong = new ArrayList<Song>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_music);
        lv = (ListView) findViewById(R.id.listViewResults);
        searchView = (SearchView)findViewById(R.id.svSearch);

        AskPermission();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = Paths[position].toString();
                Log.w("Current Path", path);
                playMusic(path);
            }
        });

    }
    public void playMusic(String path)
    {
        Intent playmusicIntent = new Intent(getApplicationContext(),PlayMusicActivity.class );
        playmusicIntent.putExtra(getString(R.string.musiclinkdata),path);
        playmusicIntent.putExtra(getString(R.string.playMusicrequest),0);
        this.startActivity(playmusicIntent);
    }
    public void AskPermission()
    {
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
        }
        else{
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
                    Log.e("Permisson","Denied");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void ArrayAppend()
    {
        getPlayList();
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,songsList,R.layout.list_view_item,from,to);
        //lv.setTextFilterEnabled(true);
        lv.setAdapter(simpleAdapter);
    }
    public void setupSearchView()
    {
        //searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)){
                    //simpleAdapter.getFilter().filter("");
                    lv.clearTextFilter();
                }else {
                    //simpleAdapter.getFilter().filter(newText.toString());
                    int a;
                }
                return true;
            };
                                          });
        //searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search Here");
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
                //String title = file.getName().substring(0, (file.getName().length() - 4));
                //Deal with metadata ở đây
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(link);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                song.put("songTitle", title );
                song.put("image", R.drawable.icon_music);
                song.put("artist",artist);
                //song.put("songPath", file.getPath());
                //Adding each song link to SongPaths to startMediaplayer
                songPaths.add(link);
                // Adding each song to SongList to show on ListView
                songsList.add(song);
                //Adding each song to arraySong for mediaplayer;
                arraySong.add(new Song(title,link));
                //Adding eachsong title to songTitle for mediaPlayer
                songTitles.add(title);
            }
            Titles = songTitles.toArray(new String[songTitles.size()]);
            Paths = songPaths.toArray(new String[songPaths.size()]);
        }
        // return songs list array
        return arraySong;
    }

    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }

}

