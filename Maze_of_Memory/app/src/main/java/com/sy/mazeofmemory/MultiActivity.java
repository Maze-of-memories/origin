package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MultiActivity extends Activity
        implements View.OnClickListener, RoomUpdateListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MultiActivity";

    // startActivityForResult 에서 사용되는 Request 코드
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Google APIs와 상호작용하기 위해 사용되는 클라이언트
    private GoogleApiClient mGoogleApiClient;

    // 프로필 사진 이미지뷰
    ImageView imageView;

    // 대기 화면의 프로필 사진 이미지뷰
    ImageView myPicture;
    ImageView peerPicture;

    // 대기 화면의 닉네임
    TextView myNickname;
    TextView peerNickname;

    // 클릭 가능한 모든 뷰의 리스트
    final static int[] CLICKABLES = {
            R.id.button_play, R.id.button_clickme

    };

    // 화면 리스트
    final static int[] SCREENS = {
            R.id.screen_main, R.id.screen_wait, R.id.screen_game,
            R.id.screen_waiting_room
    };
    int mCurScreen = -1;    /* 현재 화면을 저장하는 변수 */

    // 현재 게임이 진행중인 방의 ID. 게임중이 아니면 null이다.
    private String mRoomId = null;

    // 현재 게임이 참가한 다른 플레이어
    ArrayList<Participant> mParticipants = null;

    // 현재 게임에 참가한 나의 ID
    private String mMyId = null;

    // 전송할 메시지를 담을 버퍼
    byte[] mMsgBuf = new byte[2];

    // 맵 정보
    String map_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 레이아웃 설정
        setContentView(R.layout.activity_multi);

        // Plus와 Game에 엑세스 하는 Google API Client 생성
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        // 클라이언트 연결
        mGoogleApiClient.connect();

        // MainActivity로부터 프로필 사진의 주소를 받아 이미지뷰에 출력한다.
        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");

        // 메인화면 프로필 사진
        imageView = (ImageView) findViewById(R.id.personphoto);

        // 대기화면 프로필 사진
        myPicture = (ImageView)findViewById(R.id.my_picture);
        peerPicture = (ImageView)findViewById(R.id.peer_picture);

        // 대기화면 닉네임 : 일단은 구글 이름 출력
        myNickname = (TextView)findViewById(R.id.my_nick);
        peerNickname = (TextView)findViewById(R.id.peer_nick);

        //imageUrl DB 저장 고려
        if (url != null) {
            url = url.substring(0, url.length() - 6);

            // 멀티플레이 메인 화면의 프로필 사진
            setProfilePicture(imageView, url);

            // 대기 화면의 프로필 사진
            setProfilePicture(myPicture, url);
        }

        // 클릭 이벤트 처리가 필요한 모든 뷰에 이벤트 리스너를 설정해준다.
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
    }


    // 이미지의 URL을 이용하여 view에 출력한다.
    private void setProfilePicture(final ImageView view, final String url) {
        new AsyncTask<Void, Void, Void>() {

            URL u = null;
            Bitmap bmp = null;

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    u = new URL(url);
                    bmp = BitmapFactory.decodeStream(u.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                view.setImageBitmap(bmp);
            }
        }.execute();
    }

    // 버튼 클릭 이벤트 처리
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            // play 버튼 클릭
            case R.id.button_play:
                Log.i(TAG, "button_play clicked");
                startMultiPlay();
                break;

            // Click Me!! 버튼 클릭(테스트용 : 메시지 전송)
            case R.id.button_clickme:
                Log.d(TAG,"clickme button clicked");
                sendMessage();
                break;
        }
    }

    /*
        메시지 보내고 받기
     */

    // 멀티플레이 참가자들에게 실시간 메시지를 보내는 메소드
    void sendMessage() {
        // 전송할 메시지를 설정한다.
        mMsgBuf[0] = (byte)'T';
        mMsgBuf[1] = (byte)1;

        // 모든 참가자에게 메시지 전송
        for (Participant p : mParticipants) {

            // 자기 자신은 제외한다.
            if(p.getParticipantId().equals(mMyId))
                continue;

            // 실제 메시지를 보내는 static method
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf, mRoomId, p.getParticipantId());
        }
    }

    // 실시간 메시지를 받았을 때 호출되는 메소드
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();

        // 맵 정보를 받았을 때
        if((char)buf[0] == 'o') {
            map_info = new String(buf);
            Log.d(TAG, "Map info received: " + map_info);
        } else {
            Toast.makeText(this,"Message received: " + (char) buf[0] + "/" + (int) buf[1], Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);
        }

    }

    // 자동 매칭 멀티 게임을 시작한다.
    void startMultiPlay() {
        // 임의로 선택된 1명의 플레이어와 게임을 시작한다.
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        // 리얼타임 멀티플레이 방을 생성한다.
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                // we got the result from the "select players" UI -- ready to create the room
                // handleSelectPlayersResult(responseCode, intent);
                break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                // handleInvitationInboxResult(responseCode, intent);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                // 대기방 화면에서 받은 결과값
                if (responseCode == Activity.RESULT_OK) {
                    // ready to start playing
                    // 게임을 시작할 준비가 되었을 경우 게임 화면을 띄우고 게임을 시작한다.
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    switchToScreen(R.id.screen_game);
                    // startGame(true);
                } else if (responseCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    // 플레이어가 방을 나가고자 하는 의사를 밝힌 경우
                    leaveRoom();
                } else if (responseCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    // 방 생성 대기 중 취소(백키)하는 경우
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                /*Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + responseCode + ", intent=" + intent);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (responseCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
                }*/
                break;
        }
    }

    // 새로운 게임을 하기위해 관련 변수들을 리셋한다.
    void resetGameVars() {
       /* mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();*/
    }

    // 방이 만들어졌을 때(create() 호출시) 호출된다.
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated called (" + room.getCreatorId() + "/" + room.getRoomId() + ")");

        // 방이 생성되면 나의 Id와 방 ID를 바로 초기화 한다.
        mMyId = room.getCreatorId();
        mRoomId = room.getRoomId();

        // 에러 발생시 처리
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // 기본 대기방 화면을 보여준다.
        //showWaitingRoom(room);

        // 상대 플레어의 사진을 기본 사진으로 초기화
        peerPicture.setImageResource(R.drawable.photo);

        // 상대 플레이어의 닉네임을 초기화
        peerNickname.setText("searching...");

        // 커스텀 대기방 화면을 보여준다.
        switchToScreen(R.id.screen_waiting_room);
    }

        // join()메소드에 의해 호출되는 콜백
        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ") : 방에 참가함");
            if (statusCode != GamesStatusCodes.STATUS_OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
            return;
        }

        // 기본 대기방 화면을 보여준다.
        //showWaitingRoom(room);
    }

    // 정상적으로 방을 나갔을 때 호출되는 메소드(leaveRoom() 호출을 통한)
    // 만약 연결이 끊겨서 나가지는 경우에는 onDisconnectedFromRoom() 메소드가 호출된다.
    @Override
    public void onLeftRoom(int statusCode, String s) {
        // 메인 화면으로 돌아간다.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // 방이 완전히 연결되었을 때(플레이어간 매칭이 성사되었을 때) 호출된다.
    @Override
    public void onRoomConnected(int statusCode, Room room) {

        // 방 ID, 참가자들의 ID, 나의 ID를 가져온다.
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        Log.d(TAG, mMyId);
        Log.d(TAG, mParticipants.get(0).getParticipantId());

        // 참가자 리스트를 ID 기준으로 정렬한다(플레이어간 동일한 리스트 유지)
        Collections.sort(mParticipants, new Comparator<Participant>() {

            @Override
            public int compare(Participant lhs, Participant rhs) {
                return lhs.getParticipantId().compareTo(rhs.getParticipantId());
            }
        });
        // 리스트의 첫번 째 참가자가 서버에서 맵을 다운받는다.
        if(mParticipants.get(0).getParticipantId().equals(mMyId)) {
            Toast.makeText(this, "You have first turn", Toast.LENGTH_SHORT).show();

            // 맵 다운로드
            downloadMultiMap();

        }

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);

        switchToScreen(R.id.screen_game);
    }

    // 다른 플레이어가 방에 들어오고 연결되는 과정을 track할 수 있도록 방 대기 화면을 보여준다.
    // onRoomConnected 와 onJoinedRoom 에서 호출됨
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    // 취소(cancelled)된 게임에 대한 에러 메시지를 보여주고 메인 화면으로 돌아간다.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

    // 멀티플레이 메인 화면을 보여준다.
    void switchToMainScreen() {
        switchToScreen(R.id.screen_main);
    }

    void switchToScreen(int screenId) {
        // 요청된 스크린만 보여주고 나머지 스크린은 감춘다.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }

        // 현재 스크린 id 저장.
        mCurScreen = screenId;

        /*// should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);*/
    }

    /*
     * RoomStatusUpdate(방 상태 업데이트)관련 콜백 메소드
     */
    @Override
    public void onRoomConnecting(Room room) {updateRoom(room);}

    @Override
    public void onRoomAutoMatching(Room room) {updateRoom(room);}

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {updateRoom(room);}

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {updateRoom(room);}

    @Override
    public void onPeerJoined(Room room, List<String> strings) {
        Log.d(TAG, "onPeerJoined() called : " + strings.toString());
        updateRoom(room);
        // 상대방의 프로필 사진과 닉네임을 출력한다.
            String url;
            for(Participant p : mParticipants) {

                if(p.getParticipantId().equals(mMyId))
                    continue;
                else {
                    Log.d(TAG, "mMyId : " + mMyId);
                    Log.d(TAG, "participant id : " + p.getParticipantId());
                    Log.d(TAG, "IconImageUrl : " + p.getIconImageUrl());

                    url = p.getIconImageUrl();
                    setProfilePicture(peerPicture, url);
                    peerNickname.setText(p.getDisplayName());
                }
        }
    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {updateRoom(room);}

    // 방에 연결되었을 때 호출된다.
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");
        Log.d(TAG, room.toString());

        // 방 ID, 참가자들의 ID, 나의 ID를 가져온다.
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        // 디버깅을 위한 ID 출력
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM >>");
    }

    // 방과의 연결이 끊길 때 호출된다. (메인 화면으로 돌아감)
    @Override
    public void onDisconnectedFromRoom(Room room) {
        Toast.makeText(this, "Disconnected from room(server)", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onDisconnectedFromRoom(room) called.");
        mRoomId = null;
        showGameError();
    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {updateRoom(room);}

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {updateRoom(room);}

    @Override
    public void onP2PConnected(String s) {}

    @Override
    public void onP2PDisconnected(String s) {}

    // 방의 상태를 업데이트 한다.
    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            // updatePeerScoresDisplay();
        }
    }

    // 구글 계정 로그인 성공시 호출
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        // 로그인한 계정의 이름 출력
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        myNickname.setText(currentPerson.getDisplayName());

        Log.d(TAG, "Sign-in succeeded.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    // 구글 계정 로그인 실패시 호출
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    // 게임중 백키(back key)를 눌렀을 때 정상적으로 게임을 끝낼 수 있도록 이벤트처리를 해준다.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            // 게임 플레이 중 백키를 누르면 방을 나간다.
            leaveRoom();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_waiting_room) {
            // 대기방에서 백키를 누르면 방을 나간다.
            leaveRoom();
            return true;
        }

        // 웨이팅 화면에서 백키 금지
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_wait) {
            return false;
        }

        return super.onKeyDown(keyCode, e);
    }

    // 방을 나가는 메소드
    void leaveRoom() {
        Log.d(TAG, "leaveRoom() called.");
        // mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    // 화면에 항상 위에 있도록 플래그를 설정한다.
    // 화면이 바뀌면 게임이 취소되므로 항상 위에 올 수 있도록 설정해야 한다.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // 서버에서 맵을 가져온다.
    void downloadMultiMap() {
        String server = getString(R.string.server) + getString(R.string.multi_map_load);

        new HttpAsyncTask(server, null) {

            @Override
            protected void onPostExecute(String result) {
                // 가져온 맵 정보를 저장한다
                map_info = result;
                Log.i(TAG + "/Map Info", map_info);

                byte[] msg = map_info.getBytes();

                // 모든 참가자에게 메시지 전송
                for (Participant p : mParticipants) {

                    // 자기 자신은 제외한다.
                    if(p.getParticipantId().equals(mMyId))
                        continue;

                    // 실제 메시지를 보내는 static method
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg, mRoomId, p.getParticipantId());
                }
            }
        }.execute();
    }
}
