package com.example.cmq.musicapp;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaActionSound;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MusicService extends Service {
    MediaPlayer mediaPlayer;
    MediaPlayer.OnPreparedListener preparedListener;
    MediaPlayer.OnCompletionListener completionListener;

    private ArrayList<Song> songList;
    private int songIndex;

    int getSongIndex() { return songIndex; }
    ArrayList<Song> getSongList() { return songList; }
    void setSongIndex(int index) {
        if (index >= songList.size() || index < 0)
            return;

        songIndex = index;
    }
    void setSongList(ArrayList<Song> list) {
        songList = list;
        setSongIndex(0);
    }

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public MusicService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.mute);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.reset();
        songList = new ArrayList<>();
        songIndex = 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public void playMusic() {
        stopMusic();

        try {
            /* load the new source */
            mediaPlayer.setDataSource(songList.get(songIndex).getLink());

            /* Prepare the mediaPlayer */
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_IOException), Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(this, "Index out of bounds", Toast.LENGTH_LONG).show();
        }
    }

    public void stopMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(completionListener);
    }

    public Song getCurrentSong() {
        if (songList.size() > 0) {
            return songList.get(songIndex);
        }

        return null;
    }

    public void playNext() {
        if (songList.size() == 0)
            return;

        if (++songIndex >= songList.size()) {
            songIndex = 0;
        }

        playMusic();
    }

    public void playPrevious() {
        if (songList.size() == 0)
            return;

        if (--songIndex < 0) {
            songIndex = songList.size() - 1;
        }

        playMusic();
    }

    public boolean isOnLastSong() {
        return songIndex == songList.size() - 1;
    }
}
