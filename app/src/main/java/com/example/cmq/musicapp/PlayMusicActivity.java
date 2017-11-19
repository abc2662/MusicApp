package com.example.cmq.musicapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
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

import com.google.android.gms.auth.GoogleAuthUtil;
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
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.drive.Drive;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
//import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.android.gms.common.AccountPicker;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class PlayMusicActivity extends AppCompatActivity {

    TextView txtTitle, txtTimeProcess, txtTimeTotal;
    SeekBar sbProcess;
    ImageButton btnPrev, btnPlay, btnNext, btnStop, btnRandom, btnLoop;
    ImageView imgDics;

    com.google.android.gms.common.SignInButton btnSignIn;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount mAccountData;
    ArrayList<Song> arraySong;
    int indexSong=0;
    MediaPlayer mediaPlayer;

    Animation anim_dics;
    private static int RC_SIGN_IN = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG =  "SIGN IN: ";
        Log.i(TAG, "Sign in success");
        setContentView(R.layout.activity_play_music);

        InitComp();
        AddSongs();

        createMediaPlayer();
        anim_dics = AnimationUtils.loadAnimation(this, R.anim.dics_rotate);

        //--------------------------------------------------//
        //RIGHT HERE
        String serverClientId = getString(R.string.sever_client_id);
        //gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(Drive.SCOPE_FILE).build();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestServerAuthCode(serverClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button_gg);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        // Sign In Button Event
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
                //Intent Login = buildGoogleSignInClient().getSignInIntent();
                //startActivityForResult(Login, RC_SIGN_IN);;
            }
        });
//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.sign_in_button_gg:
//                        signIn();
//                        break;
//                }
//            }
//        });


        //--------------------------------------------------//
        //Play Pause Button Event
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
                mediaPlayer.stop();
                mediaPlayer.release();
                btnPlay.setImageResource(R.drawable.play_48);
                imgDics.clearAnimation();
                createMediaPlayer();
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

//        btnLoop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = "https://khoapham.vn/download/vietnamoi.mp3";
//                MediaPlayer mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                try {
//                    mediaPlayer.setDataSource(url);
//                    mediaPlayer.prepareAsync();
//                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//                            mp.start();
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }
//    private GoogleSignInClient buildGoogleSignInClient() {
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(Drive.SCOPE_FILE)
//                        .build();
//        return GoogleSignIn.getClient(this, signInOptions);
//    }
    private void signIn() {
        final Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
//        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(signInIntent);
//        updateViewWithGoogleSignInAccountTask(task);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            String TAG = "OnActivity ";
            Log.i(TAG, "Result");
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String authCode = account.getServerAuthCode();

                // Show signed-un UI
                //updateUI(account);
                // TODO: send code to server and exchange for access/refresh/ID tokens
            } catch (ApiException e) {
                Log.w(TAG, "Sign-in failed", e);
                //updateUI(null);
            }
            //handleSignInResult(task);
        }
    }
    //Ham bi sai nhung dung bo
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            //updateUI(account);
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
//             Build a drive client.
//           DriveClient mDriveClient = Drive.getDriveClient(getApplicationContext(), account);
//            // Build a drive resource client.
//           DriveResourceClient mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
//            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE));
//            credential.setSelectedAccountName(account.getDisplayName());
//            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//            com.google.api.services.drive.Drive mService = null;
//            mService = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(),  jsonFactory, credential).build();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }

    }
    private void updateViewWithGoogleSignInAccountTask(Task<GoogleSignInAccount> task) {
        String TAG = "UPDATEVIEW";
        Log.i(TAG, "Update view with sign in account task");
        task.addOnSuccessListener(
                new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        String TAG =  "SIGN IN: ";
                        Log.i(TAG, "Sign in success");
                        // Build a drive client.
                        DriveClient mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
                        // Build a drive resource client.
                        final DriveResourceClient mDriveResourceClient =
                                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
                        Query query = new Query.Builder()
                                .addFilter(Filters.eq(SearchableField.TITLE, "Faded.mp3"))
                                .build();
                        Task<MetadataBuffer> queryTask = mDriveResourceClient.query(query);
                        queryTask
                                .addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                                            @Override
                                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                                // Handle results...
                                                Metadata metadata = metadataBuffer.get(0);
                                                String FileID = metadata.getOriginalFilename();
                                                txtTitle.setText(FileID);
                                            }
                                        })
                                .addOnFailureListener( new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle failure...
                                    }
                                });

//                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Collections.singleton(DriveScopes.DRIVE));
//                        credential.setSelectedAccountName(credential.getSelectedAccountName());
//                        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
//                        com.google.api.services.drive.Drive mService = null;
//                        mService = new com.google.api.services.drive.Drive.Builder(AndroidHttp.newCompatibleTransport(),  jsonFactory, credential).build();
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String TAG = "SIGG IN: ";
                                Log.w(TAG, "Sign in failed", e);
                            }
                        });
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //updateUI(account);

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
        mediaPlayer = MediaPlayer.create(PlayMusicActivity.this, arraySong.get(indexSong).getFile());
        txtTitle.setText(arraySong.get(indexSong).getTitle());
        setTime();
        updateTime();
    }

    private void AddSongs() {
        arraySong = new ArrayList<>();
        arraySong.add(new Song("Em gái mưa", R.raw.em_gai_mua));
        arraySong.add(new Song("Chạm khẽ tim anh một chút thôi", R.raw.cham_khe_tim_anh_mot_chut_thoi));

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

        btnSignIn= (SignInButton) findViewById(R.id.sign_in_button_gg);

    }
}
