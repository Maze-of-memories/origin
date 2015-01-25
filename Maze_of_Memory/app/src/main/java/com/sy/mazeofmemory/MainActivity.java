package com.sy.mazeofmemory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.model.people.Person;

import com.google.android.gms.plus.model.people.Person.Image;

public class MainActivity extends Activity  implements View.OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener{

    private static final int RC_SIGN_IN = 0;

    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ProgressDialog mConnectionProgressDialog;

    private ConnectionResult mConnectionResult;

    private Games.GamesOptions apiOptions;

    private BackPressCloseHandler backPressCloseHandler;

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiOptions = Games.GamesOptions.builder().setShowConnectingPopup(true).build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API, apiOptions)
                .build();

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        btn = (Button)findViewById(R.id.single);
        btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SingleActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn = (Button)findViewById(R.id.multi);
        btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, MultiActivity.class);
                startActivity(intent);

            }
        });

        // 도움말 버튼 초기화 및 이벤트리스너 설정
        Button btnHelp = (Button)findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(this);

        backPressCloseHandler = new BackPressCloseHandler(this);

    }

    protected void onStart() {
        super.onStart();
        if(!mGoogleApiClient.isConnected())
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
        } else if(view.getId() == R.id.btnHelp) {
            // 도움말 버튼 클릭시 도움말 화면을 출력한다.
            startActivity(new Intent(this, HelpActivity.class));
        }

        if (view.getId() == R.id.sign_out_button) {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                /*권한까지 지워버림
                Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<Status>() {

                            @Override
                            public void onResult(Status status) {
                                // mGoogleApiClient is now disconnected and access has been revoked.
                                // Trigger app logic to comply with the developer policies
                            }
                        });
                */
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
        }

    }

    private void resolveSignInError() {

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
        //Toast.makeText(this, "환영합니다", Toast.LENGTH_LONG).show();

        //google 사용자 정보가져 오기
        String personName = null;
        String personGooglePlusProfile = null;
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            Image personPhoto = currentPerson.getImage();
            personGooglePlusProfile = currentPerson.getUrl();
        }
        Log.i("personName", personName);

        Log.i("personGooglePlusProfile", personGooglePlusProfile);
        
    }


    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                    backPressCloseHandler.onBackPressed();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

