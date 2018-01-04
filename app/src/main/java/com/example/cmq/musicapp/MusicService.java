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
import java.util.Collections;

public class MusicService extends Service {
    MediaPlayer mediaPlayer;
    MediaPlayer.OnPreparedListener preparedListener;
    MediaPlayer.OnCompletionListener completionListener;
    MediaPlayer.OnErrorListener errorListener;

    private ArrayList<Song> songList;
    private int songIndex;
    private ArrayList<MusicServiceListener> listeners;
    private boolean shuffle;
    private ArrayList<Integer> shuffleIndices;
    private int repeatOption = 0;
    
    int getSongIndex() { return songIndex; }
    ArrayList<Song> getSongList() { return songList; }
    boolean isShuffle() { return shuffle; }
    int getRepeatOption() { return repeatOption; }
    void setSongIndex(int index) {
        if (index >= songList.size() || index < 0)
            return;

        songIndex = index;
    }
    void setSongList(ArrayList<Song> list) {
        songList = list;
        setSongIndex(0);
    }
    void setShuffle(boolean isShuffle) {
        shuffle = isShuffle;
        if (shuffle) {
            shuffleSongList();
        }
    }
    void setRepeatOption(int index) {
        if (index > RepeatOptions.Count)
            index = 0;
        else if (index < 0)
            index = RepeatOptions.Count - 1;

        switch (index) {
            case RepeatOptions.NO_REPEAT: {
                mediaPlayer.setLooping(false);
                break;
            }
            case RepeatOptions.REPEAT_ALL: {
                mediaPlayer.setLooping(false);
                break;
            }
            case RepeatOptions.REPEAT_ONE: {
                mediaPlayer.setLooping(true);
                break;
            }
        }
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
        shuffle = false;
        shuffleIndices = new ArrayList<>();
        listeners = new ArrayList<>();

        completionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp.getDuration() == 0 || mp.getCurrentPosition() != mp.getDuration()) {
                    return;
                }
                switch (repeatOption) {
                    case RepeatOptions.NO_REPEAT: {
                        if (!isOnLastSong()) {
                            playNext();
                        } else {
                            stopMusic();
                        }
                        break;
                    }
                    case RepeatOptions.REPEAT_ALL: {
                        playNext();
                        break;
                    }
                    case RepeatOptions.REPEAT_ONE: {
                        break;
                    }
                }
            }
        };
        preparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        };
        errorListener = new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        };
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

        Song song = songList.get(findSongIndex());

        if (listeners.size() > 0) {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).onPlay(song);
            }
        }

        try {
            /* load the new source */
            mediaPlayer.setDataSource(song.getLink());

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
        if (listeners.size() > 0) {
            for (int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).onStop();
            }
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(completionListener);
    }

    public Song getCurrentSong() {
        if (songList.size() > 0) {
            return songList.get(findSongIndex());
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

    public void addMusicServiceListener(MusicServiceListener listener) {
        listeners.add(listener);
    }

    public void shuffleSongList() {
        if (shuffleIndices != null)
            shuffleIndices.clear();
        int size = songList.size();
        for (int i = 0; i < size; i++)
            shuffleIndices.add(i);
        Collections.shuffle(shuffleIndices);
    }
    
    private int findSongIndex() {
        if (!shuffle)
            return getSongIndex();
        else
            return shuffleIndices.get(getSongIndex());
    }
    
    public interface MusicServiceListener {
        void onPlay(Song song);
        void onStop();
    }


    public static final class RepeatOptions {
        public static final int NO_REPEAT = 0;
        public static final int REPEAT_ALL = 1;
        public static final int REPEAT_ONE = 2;

        public static final int Count = 3;
    }
}
