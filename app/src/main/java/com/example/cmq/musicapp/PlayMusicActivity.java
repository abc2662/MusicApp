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
import android.util.Log;
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
        public static final String PLAY_MUSIC_REQUEST = "playMusicRequest";
        public static final String SONG_LIST = "songList";
        public static final String PLAY_INDEX = "playIndex";
    }

    public static final class Options {
        public static final int DEFAULT = 0;
        public static final int STREAM = 1;
        public static final int RESUME = 2;
    }

    private ArrayList<Song> songList;
    private int indexSong = 0;

    static ArrayList<Song> arraySong = new ArrayList<Song>();
    static ArrayList<Song> shuffleArraysong = new ArrayList<>();
    static ArrayList<Song> temparraySong = new ArrayList<Song>();

    static String title;
    static MediaPlayer mediaPlayer = new MediaPlayer();
    String musicLink;
    AnimatorSet anim_disc;
    static boolean loopall = false;
    static boolean shuffle = false;
    public int activityrequest;
    Intent musiclinkIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        createPlayList();
        createMediaPlayer();
        initalizeComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();

        musiclinkIntent = getIntent();
        activityrequest = musiclinkIntent.getIntExtra(MESSAGE.PLAY_MUSIC_REQUEST, Options.DEFAULT);
        songList = (ArrayList<Song>) musiclinkIntent.getSerializableExtra(MESSAGE.SONG_LIST);
        indexSong = musiclinkIntent.getIntExtra(MESSAGE.PLAY_INDEX, 0);

        if (songList.size() <= 1) {
            btnNext.setEnabled(false);
            btnPrev.setEnabled(false);
        } else {
            btnNext.setEnabled(true);
            btnPrev.setEnabled(true);
        }

        switch (activityrequest) {
            case Options.STREAM: {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                playMusic(indexSong);
                break;
            }
            case Options.DEFAULT: {
                playMusic(indexSong);
                break;
            }
            case Options.RESUME: {
                if (mediaPlayer.isPlaying() == true) {
                    btnPlay.setImageResource(R.drawable.pause);
                    //imgDisc.startAnimation(anim_disc);
                }
                //Restore loop button
                if (mediaPlayer.isLooping() == true) {
                    btnLoop.setImageResource(R.drawable.replay_loop);
                    loopall = false;
                } else if (loopall == false && mediaPlayer.isLooping() == false) {
                    btnLoop.setImageResource(R.drawable.replay);
                } else if (loopall == true) {
                    btnLoop.setImageResource(R.drawable.replay_selected);
                }
                //Restore shuffle button
                if (shuffle == true) {
                    btnRandom.setImageResource(R.drawable.shuffle_selected);
                }
                txtTitle.setText(arraySong.get(indexSong).Title);
                setTime();
                updateTime();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
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

        txtTitle.setText(songList.get(indexSong).Title);
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

        mediaPlayer = MediaPlayer.create(PlayMusicActivity.this, Uri.parse(songList.get(indexSong).Link));

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                indexSong++;
                if ((indexSong >= arraySong.size()) && loopall == false) {
                   stopMusic();
                } else if ((indexSong >= arraySong.size()) && loopall == true) {
                    playMusic(0);
                } else {
                    createMediaPlayer();
                    mediaPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause);
                }
            }
        });
    }

    private void createPlayList() {
        arraySong = new ArrayList<Song>();
        OfflineMusicActivity offlineMusic = new OfflineMusicActivity();
        arraySong = offlineMusic.getPlayList();
        Log.w("Link", arraySong.get(0).Link);
    }

    public void AddSongs(ArrayList<Song> arraylistSong) {
        arraySong = arraylistSong;
    }

    public void playMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        mediaPlayer.reset();

        try {
            /* load the new source */
            mediaPlayer.setDataSource(songList.get(indexSong).Link);
            /* Prepare the mediaplayer */
            mediaPlayer.prepare();
            /* Start the mediaPlayer */
            mediaPlayer.start();

            UpdateUI();
            btnPlay.setImageResource(R.drawable.pause);
            anim_disc.end();
            anim_disc.start();
        } catch (IOException e) {
            Toast.makeText(this, "Unexpected error: File path not found.", Toast.LENGTH_LONG);
        }
    }

    public void playMusic(int index) {
        if (index >= 0 && index < songList.size()) {
            indexSong = index;
            playMusic();
        }
    }

    public void stopMusic() {
        mediaPlayer.stop();
        btnPlay.setImageResource(R.drawable.play);
        anim_disc.end();
    }

    TextView txtTitle, txtTimeProcess, txtTimeTotal;
    SeekBar sbProcess;
    ImageButton btnPrev, btnPlay, btnNext, btnStop, btnRandom, btnLoop;
    ImageView imgDisc;

    private void initalizeComponents() {
        txtTitle = (TextView) findViewById(R.id.txt_TitleSong);
        txtTimeProcess = (TextView) findViewById(R.id.txt_TimeProcess);
        txtTimeTotal = (TextView) findViewById(R.id.txt_TimeTotal);
        sbProcess = (SeekBar) findViewById(R.id.sb_Process);
        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnRandom = (ImageButton) findViewById(R.id.btn_random);
        btnLoop = (ImageButton) findViewById(R.id.btn_loop);
        imgDisc = (ImageView) findViewById(R.id.img_Disc);

        anim_disc = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.disc_rotation);
        anim_disc.setTarget(imgDisc);

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
//                if(mediaPlayer.isLooping()==true)
//                {
//                 mediaPlayer.seekTo(0);
//                 return;
//                }
                indexSong++;
                if (indexSong > arraySong.size() - 1) {
                    indexSong = 0;
                }

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                createMediaPlayer();
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.pause);
                anim_disc.end();
                anim_disc.start();
            }
        });


        //--------------------------------------------------//
        //Previous Button Event
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(mediaPlayer.isLooping()==true)
//                {
//                    mediaPlayer.seekTo(0);
//                    return;
//                }
                indexSong--;
                if (indexSong < 0) {
                    indexSong = arraySong.size() - 1;
                }

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                createMediaPlayer();
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.pause);
                anim_disc.end();
                anim_disc.start();
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
                if (mediaPlayer.isLooping() == false && loopall == false) {
                    btnLoop.setImageResource(R.drawable.replay_selected);
                    loopall = true;
                } else if (loopall == true) {
                    btnLoop.setImageResource(R.drawable.replay_loop);
                    loopall = false;
                    mediaPlayer.setLooping(true);
                } else if (loopall == false && mediaPlayer.isLooping() == true) {
                    btnLoop.setImageResource(R.drawable.replay);
                    mediaPlayer.setLooping(false);
                    loopall = false;
                }
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffle == false) {
                    btnRandom.setImageResource(R.drawable.shuffle_selected);
                    shuffle = true;
                    shuffleArraysong = arraySong;
                    Collections.shuffle(shuffleArraysong);
                    temparraySong = arraySong;
                    arraySong = shuffleArraysong;
                } else {
                    btnRandom.setImageResource(R.drawable.shuffle);
                    arraySong = temparraySong;
                    shuffle = false;
                }
            }
        });
    }
}
