package com.example.cmq.musicapp;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
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
    DriveResourceClient mDriveResourceClient;
    DriveClient mDriveClient;
    Button btnMyDrive;
    ImageButton btnBack;
    String TAG = "OnActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        btnMyDrive = (Button)findViewById(R.id.btnMyDrive);
        btnBack = (ImageButton)findViewById(R.id.btnBack);
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
            //signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            mDriveClient = Drive.getDriveClient(getApplicationContext(), account);
            mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
            tvUserName.setText(account.getDisplayName());
            Uri uri = account.getPhotoUrl();
            Log.w("Uri", uri.toString());
            Picasso.with(getApplicationContext()).load(uri.toString()).into(imgUserImg);
        } else {
            signedIn = false;
        }
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
    TextView tvUserName;
    ImageView imgUserImg;
    //SignInButton signInButton;
    Button signOutButton;
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
            Toast.makeText(getApplicationContext(), "Sign In needed", Toast.LENGTH_SHORT);
        }
    }
    private void initializeComponents() {
        //findViewByID
        tvUserName = (TextView) findViewById(R.id.tvName);
        imgUserImg = (CircleImageView) findViewById(R.id.imgUser);
        //imgUserImg.setImageResource(R.drawable.ic_launcher_background);
        //signInButton = (SignInButton) findViewById(R.id.btnSignIn);
        signOutButton = (Button) findViewById(R.id.btnSign_Out);


        initializeGoogleDriveSignIn();
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
                        finish();
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
    GoogleSignInClient mGoogleSignInClient;
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
                Toast.makeText(getApplicationContext(), "Signed Out", Toast.LENGTH_LONG).show();
                Log.w("On", "SignOut Success!");
                signedIn = false;
                signOutButton.setVisibility(View.GONE);
                imgUserImg.setImageResource(R.drawable.default_ava);
                tvUserName.setText("");
            }
        });
    }


    public void btnMyDrive_OnClick(View view) {

        //else
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
                Toast.makeText(getApplicationContext(), "Sign In needed", Toast.LENGTH_SHORT);
            }
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
                                Log.w("MimeType", metadata.getMimeType().toString());

                                Song song = new Song();
                                ArrayList<Song> songList = new ArrayList<Song>();
                                songList.add(song);

                                Intent playmusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
                                playmusicIntent.putExtra(PlayMusicActivity.MESSAGE.SONG_LIST, songList);
                                playmusicIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.STREAM);

                                startActivity(playmusicIntent);
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
        Intent resumeMusicIntent = new Intent(getApplicationContext(), PlayMusicActivity.class);
        resumeMusicIntent.putExtra(PlayMusicActivity.MESSAGE.ACTIVITY_REQUEST, PlayMusicActivity.Options.RESUME);
        startActivity(resumeMusicIntent);
    }
}
