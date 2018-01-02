package com.example.cmq.musicapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URLConnection;
import java.util.ArrayList;


public class OfflineMusicActivity extends AppCompatActivity {
    private SearchView searchView;
    private ListView listView;
    public SongAdapter songAdapter;
    private ImageButton btn_signIn;
    private ImageButton btn_resume;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_music);

        listView = (ListView) findViewById(R.id.listViewResults);
        songAdapter = new SongAdapter(this, songList);
        listView.setAdapter(songAdapter);

        searchView = (SearchView)findViewById(R.id.svSearch);
        btn_signIn = (ImageButton) findViewById(R.id.btn_drive);
        btn_resume = (ImageButton)findViewById(R.id.btn_resume);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resumeMusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
                resumeMusicIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.RESUME);
                startActivity(resumeMusicIntent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent playmusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
                playmusicIntent.putParcelableArrayListExtra(PlayMusicActivity.MESSAGE.SONG_LIST, songList);
                playmusicIntent.putExtra(PlayMusicActivity.MESSAGE.PLAY_INDEX, position);
                playmusicIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.DEFAULT);
                startActivity(playmusicIntent);
            }
        });
        btn_signIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent driveintent = new Intent(getApplicationContext(),DriveActivity.class);
                startActivity(driveintent);
            }
        });

        ViewCompat.setNestedScrollingEnabled(listView, true);
        listView.setTextFilterEnabled(true);
        setupSearchView();
        updateSongList();
    }

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 3;

    private void AskPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission required")
                        .setMessage("Music app needs permission to access read external storage.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXTERNAL_STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_CODE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    updateSongList();
                } else {
                    Log.e("Permission", "Denied");
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public ArrayList<Song> songList = new ArrayList<Song>();
    public void updateSongList() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            AskPermission();
            return;
        }

        songList.clear();
        String localMusicPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        Log.w("Files", "Path: " + localMusicPath);
        File directory = new File(localMusicPath);
        File[] files = directory.listFiles(new FileExtensionFilter());
        if (files.length > 0) {
            for (File file : files) {
                String link = file.getPath();
                //String title = file.getName().substring(0, (file.getName().length() - 4));
                //Deal with metadata ở đây
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(link);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                Song song = new Song(title, link);
                song.Artist = artist;

                songList.add(song);
            }

            songAdapter.notifyDataSetChanged();
        }
    }

    public void setupSearchView() {
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
                if (TextUtils.isEmpty(newText)) {
                    songAdapter.getFilter().filter(null);
                } else {
                    songAdapter.getFilter().filter(newText);
                    listView.deferNotifyDataSetChanged();
                }
                return true;
            }
        });
        //searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search Here");
    }

    class FileExtensionFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            String mimetype = URLConnection.guessContentTypeFromName(name);
            return mimetype != null && mimetype.startsWith("audio");
        }
    }

}