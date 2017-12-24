package com.example.cmq.musicapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.drive.Drive;

//import com.google.api.services.drive.Drive;

import com.google.android.gms.common.AccountPicker;


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
    MediaPlayer mediaPlayer = new MediaPlayer() ;
    String musicLink;
    Animation anim_dics;
    boolean loopall =  false;
    boolean shuffle = false;
    public int activityrequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG =  "onCreate: ";
        Log.i(TAG, "Current onCreate");
        setContentView(R.layout.activity_play_music);
        Intent musiclinkIntent = getIntent();
        createPlayList();
        InitComp();
        activityrequest = musiclinkIntent.getIntExtra(getString(R.string.streamMusicrequest),0);
        musicLink = musiclinkIntent.getStringExtra(getString(R.string.musiclinkdata));
        //createMediaPlayer();


        //--------------------------------------------------//
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play_48);
                    imgDics.clearAnimation();
                } else {
                    mediaPlayer.start();
                    btnPlay.setImageResource(R.drawable.pause_48);
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
                    btnPlay.setImageResource(R.drawable.play_48);
                    imgDics.clearAnimation();
                    createMediaPlayer();
                }
                else if(activityrequest == 1)
                {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                    btnPlay.setImageResource(R.drawable.play_48);
                    imgDics.clearAnimation();
                }

            }
        });



        //--------------------------------------------------//
        //Next Button Event
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                btnPlay.setImageResource(R.drawable.pause_48);
                imgDics.startAnimation(anim_dics);
            }
        });


        //--------------------------------------------------//
        //Previous Button Event
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                btnPlay.setImageResource(R.drawable.pause_48);
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
                    btnLoop.setImageResource(R.drawable.loop_all);
                    loopall = true;
                }
                else if(loopall == true)
                {
                    btnLoop.setImageResource(R.drawable.loop_one);
                    loopall = false;
                    mediaPlayer.setLooping(true);
                }
                else if(loopall==false&&mediaPlayer.isLooping()==true)
                {
                    btnLoop.setImageResource(R.drawable.loop_48);
                    mediaPlayer.setLooping(false);
                    loopall = false;
                    mediaPlayer.setLooping(false);
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
                    btnRandom.setImageResource(R.drawable.random_selected);
                    shuffle = true;
                    Collections.shuffle(shuffleArraysong);
                    temparraySong = arraySong;
                    arraySong = shuffleArraysong;
                }
                else
                {
                    btnRandom.setImageResource(R.drawable.random_48);
                    arraySong = temparraySong;
                    shuffle = false;
                }


            }
        });
    }

    protected void onStart()
    {
        super.onStart();
        if(activityrequest == 1)
        {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicLink);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        btnPlay.setImageResource(R.drawable.pause_48);
                        imgDics.startAnimation(anim_dics);
                        btnPlay.setImageResource(R.drawable.pause_48);
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
                btnPlay.setImageResource(R.drawable.pause_48);
            }
        }

        anim_dics = AnimationUtils.loadAnimation(this, R.anim.dics_rotate);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
        }
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
                            btnPlay.setImageResource(R.drawable.play_48);
                            //mediaPlayer.release();
                        }
                        else if((indexSong>arraySong.size()-1)&&loopall==true)
                        {
                            indexSong = 0;
                            createMediaPlayer();
                            mediaPlayer.start();
                            btnPlay.setImageResource(R.drawable.pause_48);
                        }
                        else
                        {
                            createMediaPlayer();
                            mediaPlayer.start();
                            btnPlay.setImageResource(R.drawable.pause_48);
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
        mediaPlayer = MediaPlayer.create(PlayMusicActivity.this,Uri.parse(arraySong.get(indexSong).getLink()) );
        txtTitle.setText(arraySong.get(indexSong).getTitle());
        setTime();
        updateTime();
    }
    private void createPlayList()
    {
        arraySong = new ArrayList<Song>();
        OfflineMusic offlineMusic = new OfflineMusic();
        arraySong = offlineMusic.getPlayList();
        Log.w("Link",arraySong.get(0).getLink());
        shuffleArraysong = arraySong;
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
