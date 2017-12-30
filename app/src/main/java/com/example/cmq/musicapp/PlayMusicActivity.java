package com.example.cmq.musicapp;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

//import com.google.api.services.drive.Drive;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
public class PlayMusicActivity extends AppCompatActivity {

    TextView txtTitle, txtTimeProcess, txtTimeTotal;
    SeekBar sbProcess;
    ImageButton btnPrev, btnPlay, btnNext, btnStop, btnRandom, btnLoop;
    ImageView imgDics;
    ArrayList<Song> arraySong = new ArrayList<Song>();
    ArrayList<Song> shuffleArraysong = new ArrayList<>();
    ArrayList<Song> temparraySong = new ArrayList<Song>();
    int indexSong=0;
    static MediaPlayer mediaPlayer = new MediaPlayer() ;
    String musicLink;
    String title;
    Animation anim_dics;
    boolean loopall =  false;
    boolean shuffle = false;
    public int activityrequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        if(mediaPlayer.isPlaying()== true)
        {
            mediaPlayer.stop();
        }
        String TAG =  "onCreate: ";
        Log.i(TAG, "Current onCreate");
        Intent musiclinkIntent = getIntent();
        createPlayList();
        InitComp();
        activityrequest = musiclinkIntent.getIntExtra(getString(R.string.streamMusicrequest),0);


        musicLink = musiclinkIntent.getStringExtra(getString(R.string.musiclinkdata));
        try{
            title = musiclinkIntent.getStringExtra(getString(R.string.songtitle));
        }
        catch (Exception e)
        {
            Log.w("OnDrive","Playlist");
        }
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play);
                    imgDics.clearAnimation();
                } else {
                    mediaPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause);
                    imgDics.startAnimation(anim_dics);
                }
            }
        });


        //--------------------------------------------------//
        //Stop Button Event
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(activityrequest == 0)
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    btnPlay.setImageResource(R.drawable.play);
                    imgDics.clearAnimation();
                    createMediaPlayer();
                }
                else if(activityrequest == 1)
                {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play);
                    imgDics.clearAnimation();
                }

            }
        });



        //--------------------------------------------------//
        //Next Button Event
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isLooping()==true)
                {
                 mediaPlayer.seekTo(0);
                 return;
                }
                indexSong++;
                if (indexSong > arraySong.size() - 1) {
                    indexSong = 0;
                }

                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                createMediaPlayer();
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.pause);
                imgDics.startAnimation(anim_dics);
            }
        });


        //--------------------------------------------------//
        //Previous Button Event
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isLooping()==true)
                {
                    mediaPlayer.seekTo(0);
                    return;
                }
                indexSong--;
                if (indexSong < 0) {
                    indexSong = arraySong.size() - 1;
                }

                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                createMediaPlayer();
                mediaPlayer.start();
                btnPlay.setImageResource(R.drawable.pause);
                imgDics.startAnimation(anim_dics);
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
                if(mediaPlayer.isLooping()==false&&loopall == false) {
                    btnLoop.setImageResource(R.drawable.replay_selected);
                    loopall = true;
                }
                else if(loopall == true)
                {
                    btnLoop.setImageResource(R.drawable.replay_loop);
                    loopall = false;
                    mediaPlayer.setLooping(true);
                }
                else if(loopall==false&&mediaPlayer.isLooping()==true)
                {
                    btnLoop.setImageResource(R.drawable.replay);
                    mediaPlayer.setLooping(false);
                    loopall = false;
                }
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(shuffle == false)
                {
                    btnRandom.setImageResource(R.drawable.shuffle_selected);
                    shuffle = true;
                    shuffleArraysong = arraySong;
                    Collections.shuffle(shuffleArraysong);
                    temparraySong = arraySong;
                    arraySong = shuffleArraysong;
                }
                else
                {
                    btnRandom.setImageResource(R.drawable.shuffle);
                    arraySong = temparraySong;
                    shuffle = false;
                }
            }
        });
        int a;
    }

    protected void onStart()
    {
        super.onStart();
        anim_dics = AnimationUtils.loadAnimation(this, R.anim.dics_rotate);
        if(activityrequest == 1)
        {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicLink);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                txtTitle.setText(title);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        btnPlay.setImageResource(R.drawable.pause);
                        imgDics.startAnimation(anim_dics);
                        btnPlay.setImageResource(R.drawable.pause);
                        setTime();
                        updateTime();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if(activityrequest == 0)
        {
            Log.w("getLink",arraySong.get(indexSong).getLink());
            Log.w("musicLink",musicLink);
            while(!arraySong.get(indexSong).getLink().equals(musicLink))
            {
                indexSong++;
                if(indexSong>arraySong.size())
                {
                    indexSong=0;
                }
            }
            if(arraySong.get(indexSong).getLink().equals(musicLink))
            {
                createMediaPlayer();
                mediaPlayer.start();
                imgDics.startAnimation(anim_dics);
                btnPlay.setImageResource(R.drawable.pause);
            }
        }


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(mediaPlayer.isPlaying())
//        {
//            mediaPlayer.stop();
//
//        }
    }
    @Override
    protected void onStop()
    {
        super.onStop();
    }
    private void updateTime(){
        //Update Time Process and SeekBar Process
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
                txtTimeProcess.setText(timeFormat.format(mediaPlayer.getCurrentPosition()));
                sbProcess.setProgress(mediaPlayer.getCurrentPosition());

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        indexSong++;
                        if ((indexSong > arraySong.size()-1)&&loopall == false) {
                            indexSong = 0;
                            createMediaPlayer();
                            btnPlay.setImageResource(R.drawable.play);
                            imgDics.clearAnimation();
                        }
                        else if((indexSong>arraySong.size()-1)&&loopall==true)
                        {
                            indexSong = 0;
                            createMediaPlayer();
                            mediaPlayer.start();
                            btnPlay.setImageResource(R.drawable.pause);
                        }
                        else
                        {
                            createMediaPlayer();
                            mediaPlayer.start();
                            btnPlay.setImageResource(R.drawable.pause);
                        }


                    }
                });
                handler.postDelayed(this, 500);
            }
        }, 200);
    }

    private void setTime(){
        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
        txtTimeTotal.setText(timeFormat.format(mediaPlayer.getDuration()));
        sbProcess.setMax(mediaPlayer.getDuration());
    }

    private void createMediaPlayer(){
        if(mediaPlayer != null)
        {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(PlayMusicActivity.this,Uri.parse(arraySong.get(indexSong).getLink()) );
        txtTitle.setText(arraySong.get(indexSong).getTitle());
        setTime();
        updateTime();
    }
    private void createPlayList()
    {
        arraySong = new ArrayList<Song>();
        OfflineMusicActivity offlineMusic = new OfflineMusicActivity();
        arraySong = offlineMusic.getPlayList();
        Log.w("Link",arraySong.get(0).getLink());
    }
    public void AddSongs(ArrayList<Song> arraylistSong) {
        arraySong = arraylistSong;
    }

    private void InitComp() {
        txtTitle = (TextView) findViewById(R.id.txt_TitleSong);
        txtTimeProcess = (TextView) findViewById(R.id.txt_TimeProcess);
        txtTimeTotal = (TextView) findViewById(R.id.txt_TimeTotal);

        sbProcess = (SeekBar) findViewById(R.id.sb_Process);

        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnStop = (ImageButton) findViewById(R.id.btn_stop);
        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnRandom = (ImageButton) findViewById(R.id.btn_random);
        btnLoop = (ImageButton) findViewById(R.id.btn_loop);

        imgDics = (ImageView) findViewById(R.id.img_Dics);

        //btnSignIn= (SignInButton) findViewById(R.id.sign_in_button_gg);

    }
}
