package com.example.mazeofmemory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.plus.Plus;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.PlusClient;

public class MainActivity extends Activity  implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;

    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private PlusClient mPlusClient;

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    private ProgressDialog mConnectionProgressDialog;

    private ConnectionResult mConnectionResult;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);


        btn = (Button)findViewById(R.id.single);
        btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SingleActivity.class);
                startActivity(intent);
            }
        });
        btn = (Button)findViewById(R.id.multi);
        btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, MultiActivity.class);
                startActivity(intent);

            }
        });

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    public void onClick(View view) {

        if (view.getId() == R.id.sign_in_button
                && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }

    }
    private void resolveSignInError() {

        System.out.println(mConnectionResult.hasResolution());
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        mSignInClicked = false;
        //Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        Toast.makeText(this,"환영합니다", Toast.LENGTH_LONG).show();
        // 연결 오류를 해결했습니다.
    }


    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }
}

