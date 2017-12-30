package com.example.cmq.musicapp;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;
    private static final int REQUEST_CODE_OPEN_ITEM = 300;
    static MediaPlayer mediaPlayer = new MediaPlayer() ;
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;
    TextView tvUserName;
    ImageView imgUserImg;
    SignInButton signInButton;
    Button signOutButton;
    public boolean signed;
    DriveResourceClient mDriveResourceClient;
    DriveClient mDriveClient;
    Metadata mMetadata;
    MediaPlayer mp = new MediaPlayer();
    static String TAG = "OnActivity ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findViewByID
        tvUserName = (TextView)findViewById(R.id.tvUserName);
        imgUserImg = (ImageView)findViewById(R.id.imgUserImg);
        //imgUserImg.setImageResource(R.drawable.ic_launcher_background);
        //Pre-Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        signInButton = (SignInButton) findViewById(R.id.btnSignIn);
        signOutButton = (Button)findViewById(R.id.btnSignOut);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }
    protected void onStart()
    {
        super.onStart();
        try {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if(account==null)
            {
                signed = false;
                signInButton.setVisibility(View.VISIBLE);
                signOutButton.setVisibility(View.GONE);
            }
            else
            {
                signInButton.setVisibility(View.GONE);
                signOutButton.setVisibility(View.VISIBLE);
                signed = true;
            }
            mDriveClient = Drive.getDriveClient(getApplicationContext(), account);
            mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), account);
            tvUserName.setText(account.getDisplayName());
            Uri uri = account.getPhotoUrl();
            Log.w("Uri", uri.toString());
            Picasso.with(getApplicationContext()).load(uri.toString()).into(imgUserImg);
        }
        catch (Exception e)
        {
            Log.e("Fail to","get last account info");
        }

    }
    //SignIn Event
    //-------------------------------------------------------------------------
    private void signIn() {
        if(signed==false)
        {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        else
        {
            return;
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RC_SIGN_IN:
                Log.i(TAG, "Result");
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                task.addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount account) {
                        Log.w(TAG,"sigInResult: Success");
                        signed = true;
                        tvUserName.setText(account.getDisplayName());
                        Log.w("URI",account.getPhotoUrl().toString());
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
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    mOpenItemTaskSource.setResult(driveId);
                }
        }
    }

    //SignOut Event
    //-------------------------------------------------------------------------
    public void OnClickSignOut(View view) {
        SignOut();
    }
    private void SignOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"Signed Out",Toast.LENGTH_LONG).show();
                Log.w(TAG, "SignOut Success!");
                signed = false;
                imgUserImg.setImageResource(R.drawable.default_ava);
                tvUserName.setText("");
            }
        });
    }
    //Get Drive PlayList
    //--------------------------------------------------------------------------
    public void btnDriveList_OnClick(View view) {
        try{
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
                            Log.e(TAG, "No file selected", e);
                            //showMessage(getString(R.string.file_not_selected));
                            finish();
                        }
                    });
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Sign In needed",Toast.LENGTH_SHORT);
        }

    }
    //OTHER FUNCTION
    //---------------------------------------------------------------------------
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
    protected Task<DriveId> pickFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.or(Filters.eq(SearchableField.MIME_TYPE,"audio/mpeg"),Filters.eq(SearchableField.MIME_TYPE,"audio/mp3")))
                        .setActivityTitle("Select File")
                        .build();
        return pickItem(openOptions);
    }
    private void getMetadata(final DriveFile file)
    {
        Task<Metadata> getMetadataTask = mDriveResourceClient.getMetadata(file);

        getMetadataTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<Metadata>() {
                            @Override
                            public void onSuccess(Metadata metadata) {
                                mMetadata = metadata;
                                String link = metadata.getWebContentLink();
                                String title = metadata.getTitle();
                                String mimeType = metadata.getMimeType();
                                Intent playmusicIntent = new Intent(getApplicationContext(),PlayMusicActivity.class);
                                playmusicIntent.putExtra(getString(R.string.musiclinkdata),link);
                                playmusicIntent.putExtra(getString(R.string.songtitle),title);
                                playmusicIntent.putExtra(getString(R.string.streamMusicrequest),1);
                                Log.w("Link", link);
                                Log.w("MimeType", mimeType);
                                Log.w("MimeType",mMetadata.getMimeType().toString());
                                startActivity(playmusicIntent);
                                //finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to retrieve metadata", e);
                        //showMessage(getString(R.string.read_failed));
                        //finish();
                    }
                });
    }

    public void btnPlayList_OnClick(View view) {
        Intent intent = new Intent(getApplicationContext(),OfflineMusicActivity.class);
        startActivity(intent);
    }
}
