package com.example.cmq.musicapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
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

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.blurry.Blurry;

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

    public static final class RepeatOptions {
        public static final int NO_REPEAT = 0;
        public static final int REPEAT_ALL = 1;
        public static final int REPEAT_ONE = 2;

        public static final int Count = 3;
    }

    public MediaPlayer mediaPlayer;
    boolean mBound;
    MusicService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mService = binder.getService();
            mediaPlayer = mService.mediaPlayer;

            ArrayList<Song> songList = intent.getParcelableArrayListExtra(MESSAGE.SONG_LIST);
            mService.setSongList(songList);

            mService.preparedListener = new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    UpdateUI();
                    btnPlay.setImageResource(R.drawable.pause);
                    anim_disc.start();
                    player.start();
                }
            };
            mService.completionListener = new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switch (repeatOption) {
                        case RepeatOptions.NO_REPEAT: {
                            if (mService.isOnLastSong()) {
                                anim_disc.pause();
                            } else
                                btnNext.callOnClick();
                            break;
                        }
                        case RepeatOptions.REPEAT_ALL: {
                            btnNext.callOnClick();
                            break;
                        }
                        case RepeatOptions.REPEAT_ONE: {
                            break;
                        }
                    }
                }
            };

            switch (activityRequest) {
                case Options.STREAM: {
                    btnNext.setEnabled(false);
                    btnPrev.setEnabled(false);
                    mService.playMusic();
                    break;
                }
                case Options.DEFAULT: {
                    mService.playMusic();
                    break;
                }
                case Options.RESUME: {
                    txtTitle.setText(mService.getCurrentSong().getTitle());
                    txtArtist.setText(mService.getCurrentSong().getArtist());
                    if (mediaPlayer.isPlaying()) {
                        btnPlay.setImageResource(R.drawable.pause);
                        anim_disc.start();
                    }
                    UpdateUI();
                    break;
                }
            }

            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mediaPlayer = null;
            mBound = false;
        }
    };

    public ArrayList<Integer> shuffleIndices = new ArrayList<>();
    public static boolean shuffle = false;
    public static int repeatOption = 0;
    public int activityRequest;
    public Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);
        initializeComponents();

        intent = getIntent();
        activityRequest = intent.getIntExtra(MESSAGE.ACTIVITY_REQUEST, Options.DEFAULT);

        // Bind to LocalService
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        RefreshUI();
    }

    public void RefreshUI() {
        //Restore loop button
        switch (repeatOption) {
            case RepeatOptions.REPEAT_ONE:
                btnLoop.setImageResource(R.drawable.replay_loop);
                break;
            case RepeatOptions.NO_REPEAT:
                btnLoop.setImageResource(R.drawable.replay);
                break;
            case RepeatOptions.REPEAT_ALL:
                btnLoop.setImageResource(R.drawable.replay_selected);
                break;
        }

        //Restore shuffle button
        if (shuffle) {
            btnShuffle.setImageResource(R.drawable.shuffle_selected);
        }
    }

    public void UpdateUI() {
        Song currentSong = mService.getCurrentSong();

        if (currentSong == null)
            return;

        imgDisc.setImageResource(R.drawable.cd_512);
        imgBlur.setImageResource(R.color.colorBack);
        if(activityRequest != Options.STREAM) {
            Bitmap bitmap = currentSong.getImage();

            if (bitmap != null) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                imgDisc.setImageDrawable(drawable);
                Blurry.with(getApplicationContext())
                        .radius(80)
                        .async()
                        .from(bitmap)
                        .into(imgBlur);
            }
        }

        txtTitle.setText(currentSong.getTitle());
        txtArtist.setText(currentSong.getArtist());
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

    public void shuffleSongList() {
        if (shuffleIndices != null)
            shuffleIndices.clear();
        int size = mService.getSongList().size();
        for (int i = 0; i < size; i++)
            shuffleIndices.add(i);
        Collections.shuffle(shuffleIndices);
    }

    private int findSongIndex() {
        if (!shuffle)
            return mService.getSongIndex();
        else
            return shuffleIndices.get(mService.getSongIndex());
    }

    public void changeRepeatOption(int index) {
        switch (index) {
            case RepeatOptions.NO_REPEAT: {
                btnLoop.setImageResource(R.drawable.replay);
                mediaPlayer.setLooping(false);
                break;
            }
            case RepeatOptions.REPEAT_ALL: {
                btnLoop.setImageResource(R.drawable.replay_selected);
                mediaPlayer.setLooping(false);
                break;
            }
            case RepeatOptions.REPEAT_ONE: {
                btnLoop.setImageResource(R.drawable.replay_loop);
                mediaPlayer.setLooping(true);
                break;
            }
        }
    }

    private TextView txtTitle, txtTimeProcess, txtTimeTotal, txtArtist;
    private SeekBar sbProcess;
    private ImageButton btnPrev, btnPlay, btnNext, btnShuffle, btnLoop, btnList;
    private ImageView imgDisc;
    private ImageView imgBlur;
    private AnimatorSet anim_disc;

    private void initializeComponents() {
        txtTitle = (TextView) findViewById(R.id.tv_title);
        txtArtist = (TextView) findViewById(R.id.tv_artist);
        txtTimeProcess = (TextView) findViewById(R.id.tv_time_progress);
        txtTimeTotal = (TextView) findViewById(R.id.tv_time_total);
        sbProcess = (SeekBar) findViewById(R.id.sb_progress);
        btnPrev = (ImageButton) findViewById(R.id.btn_prev);
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnNext = (ImageButton) findViewById(R.id.btn_next);
        btnList = (ImageButton) findViewById(R.id.btn_list);
        btnShuffle = (ImageButton) findViewById(R.id.btn_shuffle);
        btnLoop = (ImageButton) findViewById(R.id.btn_loop);
        imgDisc = (CircleImageView) findViewById(R.id.img_disc);
        imgBlur = (ImageView) findViewById(R.id.img_blur);
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
                    btnPlay.setImageResource(R.drawable.pause);
                    if (anim_disc.isPaused())
                        anim_disc.resume();
                    else
                        anim_disc.start();
                    mediaPlayer.start();
                }
            }
        });

        //--------------------------------------------------//
        //Next Button Event
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.playNext();
            }
        });


        //--------------------------------------------------//
        //Previous Button Event
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mService.playPrevious();
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
                repeatOption = (repeatOption + 1) % RepeatOptions.Count;
                changeRepeatOption(repeatOption);
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
        //--------------------------------------------------//
        //List Button Event
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
