package com.example.cmq.musicapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriveActivity extends AppCompatActivity {
    public boolean signedIn;
    public DriveResourceClient mDriveResourceClient;
    public DriveClient mDriveClient;
    private LinearLayout btnMyDrive;
    private ImageButton btnBack;
    private ImageButton imgResume;
    private static final String TAG = "OnActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        initializeComponents();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GoogleSignInAccount account = null;
        try {
            account = GoogleSignIn.getLastSignedInAccount(this);
        } catch (Exception e) {
            Log.e("Fail to", "get last account info");
        }

        if (account != null) {
            signedIn = true;
            signOutButton.setVisibility(View.VISIBLE);
            mDriveClient = Drive.getDriveClient(getApplicationContext(), account);
            mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
            tvUserName.setText(account.getDisplayName());
            Uri uri = account.getPhotoUrl();
            if (uri != null) {
                Log.w("Uri", uri.toString());
                Picasso.with(getApplicationContext()).load(uri.toString()).into(imgUserImg);
            }

        } else {
            signedIn = false;
            signOutButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PlayMusicActivity.songList == null) {
            imgResume.setVisibility(View.GONE);
        } else {
            imgResume.setVisibility(View.VISIBLE);
        }
    }

    private TextView tvUserName;
    private ImageView imgUserImg;
    //SignInButton signInButton;
    private Button signOutButton;
    public void openFile()
    {
        try {
            pickFile()
                    .addOnSuccessListener(this,
                            new OnSuccessListener<DriveId>() {
                                @Override
                                public void onSuccess(DriveId driveId) {
                                    getMetadata(driveId.asDriveFile());
                                    //openFiles(driveId.asDriveFile());
                                }
                            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Drive List:", "No file selected", e);
                            //showMessage(getString(R.string.file_not_selected));
                            finish();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Sign in needed", Toast.LENGTH_SHORT).show();
        }
    }
    private void initializeComponents() {
        //findViewByID
        tvUserName = (TextView) findViewById(R.id.tv_name);
        imgUserImg = (CircleImageView) findViewById(R.id.img_user);
        signOutButton = (Button) findViewById(R.id.btn_sign_out);
        btnMyDrive = (LinearLayout) findViewById(R.id.btn_my_drive);
        btnBack = (ImageButton)findViewById(R.id.btn_back);
        imgResume = (ImageButton) findViewById(R.id.btn_resume);

        AnimatorSet anim_disc = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.disc_rotation);
        anim_disc.setTarget(imgResume);
        anim_disc.start();

        initializeGoogleDriveSignIn();

        btnMyDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!signedIn)
                {
                    signIn();
                }
                else
                {
                    openFile();
                }
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private static final int REQUEST_CODE_SIGN_IN = 100;
    private void signIn() {
        if (!signedIn) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN: {
                Log.i(TAG, "Result");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                task.addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount account) {
                        Log.w(TAG, "sigInResult: Success");
                        signedIn = true;
                        tvUserName.setText(account.getDisplayName());
                        signOutButton.setVisibility(View.VISIBLE);
                        Log.w("URI", account.getPhotoUrl().toString());
                        Picasso.with(getApplicationContext()).load(account.getPhotoUrl().toString()).into(imgUserImg);
                        //imgUserImg.setImageBitmap(loadBitmap(account.getPhotoUrl().toString()));
                        //imgUserImg.setImageURI(account.getPhotoUrl());
                        mDriveClient = Drive.getDriveClient(getApplicationContext(), account);
                        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "signInResult:failed ");
                        Toast.makeText(getApplicationContext(), getString(R.string.signInFailed), Toast.LENGTH_LONG).show();
                    }
                });
                //handleSignInResult(task);
                break;
            }
            case REQUEST_CODE_OPEN_ITEM: {
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mOpenItemTaskSource.setResult(driveId);
                }
                break;
            }
        }
    }

    public GoogleSignInClient mGoogleSignInClient;
    private void initializeGoogleDriveSignIn() {
        GoogleSignInOptions googleSignInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .requestEmail()
                        .requestProfile()
                        .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), googleSignInOptions);
    }

    public void btnSignOut_OnClick(View view) {
        if (!signedIn)
            return;

        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), getString(R.string.signOutSuccess), Toast.LENGTH_LONG).show();
                Log.w("On", "SignOut Success!");
                signedIn = false;
                signOutButton.setVisibility(View.GONE);
                imgUserImg.setImageResource(R.drawable.account_selected);
                tvUserName.setText("");
            }
        });
    }

    public void btnMyDrive_OnClick(View view) {
        try {
            pickFile()
                    .addOnSuccessListener(this,
                            new OnSuccessListener<DriveId>() {
                                @Override
                                public void onSuccess(DriveId driveId) {
                                    getMetadata(driveId.asDriveFile());
                                    //openFiles(driveId.asDriveFile());
                                }
                            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Drive List:", "No file selected", e);
                            //showMessage(getString(R.string.file_not_selected));
                            finish();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Sign In needed", Toast.LENGTH_SHORT).show();
        }
    }

    private static final int REQUEST_CODE_OPEN_ITEM = 300;
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        mDriveClient
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith(new Continuation<IntentSender, Void>() {
                    @Override
                    public Void then(@NonNull Task<IntentSender> task) throws Exception {
                        startIntentSenderForResult(
                                task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0);
                        return null;
                    }
                });
        return mOpenItemTaskSource.getTask();
    }

    public Task<DriveId> pickFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.or(Filters.eq(SearchableField.MIME_TYPE, "audio/mp3"),Filters.eq(SearchableField.MIME_TYPE, "audio/mpeg")))
                        .setActivityTitle("Select File")
                        .build();
        return pickItem(openOptions);
    }

    public void getMetadata(final DriveFile file) {
        Task<Metadata> getMetadataTask = mDriveResourceClient.getMetadata(file);

        getMetadataTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<Metadata>() {
                            @Override
                            public void onSuccess(Metadata metadata) {
                                String link = metadata.getWebContentLink();
                                String title = metadata.getTitle();
                                String mimeType = metadata.getMimeType();

                                Log.w("Link", link);
                                Log.w("MimeType", mimeType);
                                Log.w("MimeType", metadata.getMimeType());

                                Song song = new Song(link);
                                song.setTitle(title.substring(0,title.length()-4));

                                ArrayList<Song> songList = new ArrayList<>();
                                songList.add(song);

                                Intent playIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
                                playIntent.putExtra(PlayMusicActivity.MESSAGE.SONG_LIST, songList);
                                playIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.STREAM);

                                startActivity(playIntent);
                                //finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("OnDriveList", "Unable to retrieve metadata", e);
                        //showMessage(getString(R.string.read_failed));
                        //finish();
                    }
                });
    }

    public void btnResume_Click(View view) {
        if(PlayMusicActivity.songList ==null)
            return;
        Intent resumeMusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
        resumeMusicIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.RESUME);
        startActivity(resumeMusicIntent);
    }
}
