package com.example.cmq.musicapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.api.services.drive.Drive;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
public class PlayMusicActivity extends AppCompatActivity {
    public static final class MESSAGE {
        public static final String ACTIVITY_REQUEST = "playMusicRequest";
        public static final String SONG_LIST = "songList";
        public static final String PLAY_INDEX = "playIndex";
    }

    public static final class Options {
        public static final int DEFAULT = 0;
        public static final int STREAM = 1;
        public static final int RESUME = 2;
    }

    static MediaPlayer mediaPlayer = new MediaPlayer();

    public static ArrayList<Song> songList;
    private ArrayList<Integer> shuffleIndices;
    public static int songIndex = 0;
    public static boolean loopAll = false;
    public static boolean shuffle = false;
    public int activityRequest;
    public Intent musiclinkIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        musiclinkIntent = getIntent();
        activityRequest = musiclinkIntent.getIntExtra(MESSAGE.ACTIVITY_REQUEST, Options.DEFAULT);
        createMediaPlayer();
        initializeComponents();
    }
    private void getPlayList()
    {
        songList = musiclinkIntent.getParcelableArrayListExtra(MESSAGE.SONG_LIST);
        songIndex = musiclinkIntent.getIntExtra(MESSAGE.PLAY_INDEX, 0);
        if (songList.size() <= 1) {
            btnNext.setEnabled(false);
            btnPrev.setEnabled(false);
        } else {
            btnNext.setEnabled(true);
            btnPrev.setEnabled(true);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        switch (activityRequest) {
            case Options.STREAM: {
                getPlayList();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                playMusic(songIndex);
                break;
            }
            case Options.DEFAULT: {
                getPlayList();
                playMusic(songIndex);
                break;
            }
            case Options.RESUME: {
                txtTitle.setText(songList.get(songIndex).Title);
                if (mediaPlayer.isPlaying() == true) {
                    btnPlay.setImageResource(R.drawable.pause);
                    anim_disc.start();
                }
                //Restore loop button
                if (mediaPlayer.isLooping() == true) {
                    btnLoop.setImageResource(R.drawable.replay_loop);
                    loopAll = false;
                } else if (loopAll == false && mediaPlayer.isLooping() == false) {
                    btnLoop.setImageResource(R.drawable.replay);
                } else if (loopAll == true) {
                    btnLoop.setImageResource(R.drawable.replay_selected);
                }
                //Restore shuffle button
                if (shuffle == true) {
                    btnShuffle.setImageResource(R.drawable.shuffle_selected);
                }
                setTime();
                updateTime();
                break;
            }
        }
    }


    public void UpdateUI() {
//        try {
//            musicLink = musiclinkIntent.getStringExtra(getString(R.string.musiclinkdata));
//        } catch (Exception e) {
//            Log.v("OnResume", "Music");
//        }
//        try {
//            title = musiclinkIntent.getStringExtra(getString(R.string.songtitle));
//        } catch (Exception e) {
//            Log.w("OnDrive", "Playlist");
//        }

        txtTitle.setText(songList.get(songIndex).Title);
        setTime();
        updateTime();
    }

    private void updateTime() {
        //Update Time Process and SeekBar Process
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
                int currentPosition = mediaPlayer.getCurrentPosition();
                txtTimeProcess.setText(timeFormat.format(currentPosition));
                sbProcess.setProgress(currentPosition);
                handler.postDelayed(this, 500);
            }
        }, 200);
    }

    private void setTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
        txtTimeTotal.setText(timeFormat.format(mediaPlayer.getDuration()));
        sbProcess.setMax(mediaPlayer.getDuration());
    }

    private void createMediaPlayer() {
        if (mediaPlayer != null) {
            return;
        }

        mediaPlayer = MediaPlayer.create(PlayMusicActivity.this, Uri.parse(songList.get(songIndex).Link));

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnNext.callOnClick();
            }
        });
    }

    public void playMusic(int index) {
        if (index >= 0 && index < songList.size()) {
            if (mediaPlayer.isPlaying()) {
                stopMusic();
            }

            try {
                /* load the new source */
                mediaPlayer.setDataSource(songList.get(index).Link);

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer player) {
                        player.start();
                        UpdateUI();
                        btnPlay.setImageResource(R.drawable.pause);
                        anim_disc.start();
                    }
                });

                /* Prepare the mediaPlayer */
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                Toast.makeText(this, "Unexpected error: File path not found.", Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Unexpected error.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void stopMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else {
            return;
        }

        mediaPlayer.reset();
        btnPlay.setImageResource(R.drawable.play);
        anim_disc.end();
    }

    public void shuffleSongList() {
        shuffleIndices.clear();
        for (int i = 0; i < songList.size(); i++)
            shuffleIndices.add(i);
        Collections.shuffle(shuffleIndices);
    }

    private int findSongIndex() {
        if (!shuffle)
            return songIndex;
        else
            return shuffleIndices.get(songIndex);
    }

    TextView txtTitle, txtTimeProcess, txtTimeTotal;
    SeekBar sbProcess;
    ImageButton btnPrev, btnPlay, btnNext, btnShuffle, btnLoop;
    ImageView imgDisc;
    AnimatorSet anim_disc;

    private void initializeComponents() {
        txtTitle = (TextView) findViewById(R.id.txt_TitleSong);
        txtTimeProcess = (TextView) findViewById(R.id.txt_TimeProcess);
        txtTimeTotal = (TextView) findViewById(R.id.txt_TimeTotal);
        sbProcess = (SeekBar) findViewById(R.id.sb_Process);
        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnShuffle = (ImageButton) findViewById(R.id.btn_shuffle);
        btnLoop = (ImageButton) findViewById(R.id.btn_loop);
        imgDisc = (ImageView) findViewById(R.id.img_Disc);

        anim_disc = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.disc_rotation);
        anim_disc.setTarget(imgDisc);
        anim_disc.start();

        //--------------------------------------------------//
        //Play Button Event
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    anim_disc.pause();
                    btnPlay.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause);
                    if (anim_disc.isPaused())
                        anim_disc.resume();
                    else
                        anim_disc.start();
                }
            }
        });

        //--------------------------------------------------//
        //Next Button Event
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (++songIndex >= songList.size()) {
                    songIndex = 0;
                }

                playMusic(findSongIndex());
            }
        });


        //--------------------------------------------------//
        //Previous Button Event
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (--songIndex < 0) {
                    songIndex = songList.size() - 1;
                }

                playMusic(findSongIndex());
            }
        });


        //--------------------------------------------------//
        //SeekBar Process Event
        sbProcess.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(sbProcess.getProgress());
            }
        });


        //--------------------------------------------------//
        //Loop Button Event

        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mediaPlayer.isLooping() && !loopAll) {
                    btnLoop.setImageResource(R.drawable.replay_selected);
                    mediaPlayer.setLooping(false);
                    loopAll = true;
                } else if (loopAll) {
                    btnLoop.setImageResource(R.drawable.replay_loop);
                    loopAll = false;
                    mediaPlayer.setLooping(true);
                } else if (!loopAll && mediaPlayer.isLooping()) {
                    btnLoop.setImageResource(R.drawable.replay);
                    mediaPlayer.setLooping(false);
                    loopAll = false;
                }
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shuffle) {
                    btnShuffle.setImageResource(R.drawable.shuffle_selected);
                    shuffle = true;
                    shuffleSongList();
                } else {
                    btnShuffle.setImageResource(R.drawable.shuffle);
                    shuffle = false;
                }
            }
        });
    }
}
