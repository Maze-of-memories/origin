package com.sy.mazeofmemory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends BaseGameActivity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener,
        DrawerLayout.DrawerListener{

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

    boolean isPageOpen = false;
    LinearLayout menuPage;
    private DrawerLayout drawerLayout;

    Sound sound = new Sound();
    boolean btn_sound;
    boolean bg_sound;

    Button background_sound;
    Button sound_btn;

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
        Button btnMenu = (Button) findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this);

        Button btnLeaderboard = (Button) findViewById(R.id.leaderboard);
        btnLeaderboard.setOnClickListener(this);

        Button achievement = (Button) findViewById(R.id.achievement);
        achievement.setOnClickListener(this);

        //배경음 클릭
        background_sound = (Button) findViewById(R.id.background_sound);
        background_sound.setOnClickListener(this);

        //버튼음 클릭
        sound_btn = (Button) findViewById(R.id.btn_sound);
        sound_btn.setOnClickListener(this);

        //back키 막기
        backPressCloseHandler = new BackPressCloseHandler(this);

        getPreferences();

        /////////////////////////////////////////////////////////////////////////////
        drawerLayout = (DrawerLayout) findViewById(R.id.R_drawer);
        drawerLayout.setDrawerListener(this);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        menuPage = (LinearLayout) findViewById(R.id.menuPage);
        menuPage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    drawerLayout.closeDrawer(menuPage);
                }
                return true;
            }
        });
    }

    protected void onStart() {
        super.onStart();
        //자동로그인 취소
        getGameHelper().setMaxAutoSignInAttempts(0);
        mGoogleApiClient.connect();

        sound.initBtnSound(this);
        if (bg_sound)
            startService(new Intent("backgroundSound"));

        if (btn_sound) {
            sound_btn.setBackgroundResource(R.drawable.sound_btn_default);
        } else {
            sound_btn.setBackgroundResource(R.drawable.sound_btn_click);
        }
        if (bg_sound) {
            background_sound.setBackgroundResource(R.drawable.sound_bg_default);
        } else {
            background_sound.setBackgroundResource(R.drawable.sound_bg_click);
        }
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (bg_sound)
            stopService(new Intent("backgroundSound"));
    }

    private void getPreferences() {
        SharedPreferences sound = getSharedPreferences("sound", MODE_PRIVATE);
        this.btn_sound = sound.getBoolean("btn_sound", true);
        this.bg_sound = sound.getBoolean("bg_sound", true);
    }

    private void saveBtnPreferences(boolean btn_sound) {
        SharedPreferences sound = getSharedPreferences("sound", MODE_PRIVATE);
        SharedPreferences.Editor editor = sound.edit();
        editor.putBoolean("btn_sound", btn_sound);
        editor.commit();
    }

    private void saveBgPreferences(boolean bg_sound) {
        SharedPreferences sound = getSharedPreferences("sound", MODE_PRIVATE);
        SharedPreferences.Editor editor = sound.edit();
        editor.putBoolean("bg_sound", bg_sound);
        editor.commit();
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

    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onSignInSucceeded() {
    }

    ///////////////////////////////////////////////////////////////
    //드라워
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View drawerView) {
    }

    @Override
    public void onDrawerClosed(View drawerView) {
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        switch (newState) {
            case DrawerLayout.STATE_SETTLING:
                if (btn_sound) {
                    sound.playBtnSound();
                }
                break;
        }
    }
    ///////////////////////////////////////////////////////////////

    public void onClick(View view) {

        if (view.getId() == R.id.sign_in_button
                && !mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        } else if (view.getId() == R.id.sign_out_button) {
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
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();

                drawerLayout.closeDrawer(menuPage);
                findViewById(R.id.menuPage).setVisibility(View.GONE);
                findViewById(R.id.R_main).setVisibility(View.GONE);
                findViewById(R.id.R_login).setVisibility(View.VISIBLE);
            }
        } else if (view.getId() == R.id.btn_sound) {
            if (btn_sound) {
                btn_sound = false;
                sound_btn.setBackgroundResource(R.drawable.sound_btn_click);
            } else {
                btn_sound = true;
                sound_btn.setBackgroundResource(R.drawable.sound_btn_default);
            }
            saveBtnPreferences(btn_sound);
        } else if (view.getId() == R.id.background_sound) {
            if (bg_sound) {
                bg_sound = false;
                stopService(new Intent("backgroundSound"));
                background_sound.setBackgroundResource(R.drawable.sound_bg_click);
            } else {
                bg_sound = true;
                startService(new Intent("backgroundSound"));
                background_sound.setBackgroundResource(R.drawable.sound_bg_default);
            }
            saveBgPreferences(bg_sound);
        } else if (view.getId() == R.id.btn_menu) {
            if (btn_sound) {
                sound.playBtnSound();
            }
            drawerLayout.openDrawer(menuPage);
        } else if (view.getId() == R.id.leaderboard) {
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), 5);
        } else if (view.getId() == R.id.achievement) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 5);
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

        personName = personName.replaceAll("\\p{Space}", "_");

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

