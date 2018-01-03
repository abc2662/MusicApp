package com.example.cmq.musicapp;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    private SongAdapter songAdapter;
    private ImageButton btn_resume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_music);

        ListView listView = (ListView) findViewById(R.id.listViewResults);
        songAdapter = new SongAdapter(this, songList);
        listView.setAdapter(songAdapter);

        searchView = (SearchView) findViewById(R.id.svSearch);
        ImageButton btn_signIn = (ImageButton) findViewById(R.id.btn_drive);
        btn_resume = (ImageButton) findViewById(R.id.btn_resume);

        AnimatorSet anim_disc = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.disc_rotation);
        anim_disc.setTarget(btn_resume);
        anim_disc.start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PlayMusicActivity.songList == null)
                    return;

                Intent resumeMusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
                resumeMusicIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.RESUME);
                startActivity(resumeMusicIntent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent playIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
                playIntent.putParcelableArrayListExtra(PlayMusicActivity.MESSAGE.SONG_LIST, songList);
                playIntent.putExtra(PlayMusicActivity.MESSAGE.PLAY_INDEX, position);
                playIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.DEFAULT);
                startActivity(playIntent);
            }
        });
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent driveIntent = new Intent(getApplicationContext(), DriveActivity.class);
                startActivity(driveIntent);
            }
        });

        ViewCompat.setNestedScrollingEnabled(listView, true);
        listView.setTextFilterEnabled(true);
        setupSearchView();
        updateSongList();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(PlayMusicActivity.songList==null)
        {
            btn_resume.setVisibility(View.GONE);
        }
        else
        {
            btn_resume.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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

    public ArrayList<Song> songList = new ArrayList<>();
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

                Song song = new Song(link);
                song.setTitle(title);
                song.setArtist(artist);

                songList.add(song);
            }

            songAdapter.notifyDataSetChanged();
        }
    }

    private void setupSearchView() {
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
                songAdapter.getFilter().filter(newText);
                return true;
            }
        });
        //searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search Here");
    }

    class FileExtensionFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            String mimeType = URLConnection.guessContentTypeFromName(name);
            return mimeType != null && mimeType.startsWith("audio");
        }
    }

}