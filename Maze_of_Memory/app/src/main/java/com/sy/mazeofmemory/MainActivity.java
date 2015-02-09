package com.sy.mazeofmemory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity implements View.OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean mSignInClicked;
    private ConnectionResult mConnectionResult;
    private Games.GamesOptions apiOptions;
    private BackPressCloseHandler backPressCloseHandler;

    private String personName;
    private String personPhotoUrl = null;
    private String personGooglePlusProfile;
    private String personEmail;

    Button btn;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

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

        btn = (Button) findViewById(R.id.single);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SingleActivity.class);
                startActivity(intent);
            }
        });
        btn = (Button) findViewById(R.id.multi);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MultiActivity.class);
                intent.putExtra("url", personPhotoUrl);
                startActivity(intent);
            }
        });

        // 도움말 버튼 초기화 및 이벤트리스너 설정
        Button btnHelp = (Button) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(this);

        backPressCloseHandler = new BackPressCloseHandler(this);

    }

    protected void onStart() {
        super.onStart();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnectionFailed(ConnectionResult result) {

        dialog.dismiss();
        findViewById(R.id.R_login).setVisibility(View.VISIBLE);
        findViewById(R.id.R_main).setVisibility(View.GONE);

        if (!mIntentInProgress) {
            mConnectionResult = result;

            if (mSignInClicked) {
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
        } else if (view.getId() == R.id.btnHelp) {
            // 도움말 버튼 클릭시 도움말 화면을 출력한다.
            startActivity(new Intent(this, HelpActivity.class));
        }

        if (view.getId() == R.id.sign_out_button) {
            if (mGoogleApiClient.isConnected()) {
                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                /* 토큰 권한 삭제
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

                findViewById(R.id.R_main).setVisibility(View.GONE);
                findViewById(R.id.R_login).setVisibility(View.VISIBLE);
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
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mSignInClicked = false;

        //google 사용자 정보가져 오기
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            personPhotoUrl = currentPerson.getImage().getUrl();
            personGooglePlusProfile = currentPerson.getUrl();
            personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);

        }
        Log.i("personName", personName);
        Log.i("personPhotoUrl", personPhotoUrl);
        Log.i("personGooglePlusProfile", personGooglePlusProfile);
        Log.i("personEmail", personEmail);

        personName = personName.replaceAll("\\p{Space}","_");

        Log.i("personName", personName);

        accountCreate();

    }

    public void accountCreate() {
        new AsyncTask<Void, Void, Void>() {

            boolean exist;

            @Override
            protected Void doInBackground(Void... params) {
                StringBuffer sb = new StringBuffer();

                try {

                    //공백 예외
                    URL url = new URL("http://53.vs.woobi.co.kr/MOM/AccountCreate.php?email=" + personEmail + "&nickname=" + personName);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream()));

                    String str = null;
                    while ((str = reader.readLine()) != null) {
                        sb.append(str);
                    }

                    if (sb.toString().equals("success")) {
                        exist = true;
                    } else if (sb.toString().equals("fail")) {
                        exist = false;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //작업 후 보여질 화면
                dialog.dismiss();

                    findViewById(R.id.R_login).setVisibility(View.GONE);
                    findViewById(R.id.R_main).setVisibility(View.VISIBLE);

            }
        }.execute();

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

