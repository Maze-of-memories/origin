package com.sy.mazeofmemory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MultiActivity extends Activity
        implements View.OnClickListener, RoomUpdateListener, RealTimeMessageReceivedListener,
        RoomStatusUpdateListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemClickListener {

    private static final String TAG = "MultiActivity";

    // startActivityForResult 에서 사용되는 Request 코드
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // 말의 위치
    final int LEFT_MARKER = 1;
    final int RIGHT_MARKER = 2;

    // Google APIs와 상호작용하기 위해 사용되는 클라이언트
    private GoogleApiClient mGoogleApiClient;

    // 프로그래스 다이얼로그
    ProgressDialog mPDialog;

    // 메인화면의 프로필 사진과 닉네임
    ImageView mainProfilePicture;
    TextView mainNickname;

    // 나와 상대방의 프로필 사진 주소
    String myPictureURL;
    String peerPictureURL;

    // 나와 상대방의 닉네임
    String strMyNick;
    String strPeerNick;

    // 대기 화면의 프로필 사진 이미지뷰
    ImageView myPicture;
    ImageView peerPicture;

    // 대기 화면의 닉네임
    TextView myNickname;
    TextView peerNickname;

    // 대기방의 상태 메시지
    TextView waitingRoomStatus;

    // 클릭 가능한 모든 뷰의 리스트
    final static int[] CLICKABLES = {
            R.id.button_play, R.id.button_pass_turn
    };

    // 화면 리스트
    final static int[] SCREENS = {
            R.id.screen_main, R.id.screen_game,
            R.id.screen_waiting_room
    };
    int mCurScreen = -1;    /* 현재 화면을 저장하는 변수 */

    // 현재 게임이 진행중인 방의 ID. 게임중이 아니면 null이다.
    private String mRoomId = null;

    // 현재 게임이 참가한 다른 플레이어
    ArrayList<Participant> mParticipants = null;
    String mPeerId = null;

    // 현재 게임에 참가한 나의 ID
    private String mMyId = null;

    // 턴 관련 변수
    private final static int TURNCNT = 3;
    private boolean isMyTurn = false;   /* 턴 여부 */
    private int remainingTurn = TURNCNT;      /* 남은 턴 수 */

    // 말
    int myMarker;
    int peerMarker;

    // 각 플레이어의 시작 위치와 종료 위치
    private static final int LEFT_START_POSITION = 20;
    private static final int RIGHT_START_POSITION = 24;
    private static final int LEFT_GOAL_POSITION = 4;
    private static final int RIGHT_GOAL_POSITION = 0;

    // 맵 정보
    String map_info;

    // 맵을 표현하는 그리드뷰
    GridView gridMap;
    ImageAdapter iAdapter;

    private Integer[] gridItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 레이아웃 설정
        setContentView(R.layout.activity_multi);

        mPDialog = new ProgressDialog(this);
        mPDialog.setMessage(getString(R.string.please_wait));
        mPDialog.setCanceledOnTouchOutside(false);
        mPDialog.setCancelable(false);
        mPDialog.show();

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
        myPictureURL = intent.getExtras().getString("url");

        // 메인화면 프로필 사진 및 닉네임
        mainProfilePicture = (ImageView) findViewById(R.id.personphoto);
        mainNickname = (TextView)findViewById(R.id.main_nickname);

        // 대기화면 프로필 사진
        myPicture = (ImageView)findViewById(R.id.my_picture);
        peerPicture = (ImageView)findViewById(R.id.peer_picture);

        // 대기화면 닉네임 : 일단은 구글 이름 출력
        myNickname = (TextView)findViewById(R.id.my_nick);
        peerNickname = (TextView)findViewById(R.id.peer_nick);

        // 대기화면 상태 메시지
        waitingRoomStatus = (TextView)findViewById(R.id.waiting_room_status);

        // 말의 위치를 나타내는 배열 초기화
        gridItems = new Integer[25];
        for(int i = 0; i < 25; i++)
            gridItems[i] = new Integer(0);

        // 미로 그리드뷰 초기화
        gridMap = (GridView)findViewById(R.id.gridView);
        iAdapter = new ImageAdapter(this);
        gridMap.setAdapter(iAdapter);
        gridMap.setOnItemClickListener(this);
        gridMap.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });

        //imageUrl DB 저장 고려
        if (myPictureURL != null) {
            myPictureURL = myPictureURL.substring(0, myPictureURL.length() - 6);

            // 멀티플레이 메인 화면의 프로필 사진
            setProfilePicture(mainProfilePicture, myPictureURL);

            // 대기 화면의 프로필 사진
            setProfilePicture(myPicture, myPictureURL);
        }

        // 클릭 이벤트 처리가 필요한 모든 뷰에 이벤트 리스너를 설정해준다.
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
    }

    // 버튼 클릭 이벤트 처리
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            // play 버튼 클릭
            case R.id.button_play:
                Log.i(TAG, "button_play clicked");
                mPDialog.show();
                startMultiPlay();
                break;

            // 턴 넘김(턴 종료) 버튼 클릭
            case R.id.button_pass_turn  :
                if(!isMyTurn) return;

                Log.d(TAG,"pass turn button clicked");
                passTurn();
                break;
        }
    }

    // 그리드뷰 아이템 클릭 이벤트 처리
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 내 턴이 아니면 말을 움직이지 않고 바로 빠져나온다.
        if(!isMyTurn) {
            Toast.makeText(this, "It is not your turn", Toast.LENGTH_SHORT).show();
            return;
        }

        // 벽 검사 루틴 들어가야함.

        // 벽이 없으면 자신의 말을 이동시킨다.
        moveMyMarkerPosition(position);
    }

    void moveMyMarkerPosition(int pos) {
        // 자신의 말을 이동시킨다.
        for(int i = 0; i < gridItems.length; i++) {
            if(gridItems[i] == myMarker)
                gridItems[i] = 0;
        }
        gridItems[pos] = myMarker;

        // 변경사항을 어댑터에게 알린다.
        iAdapter.notifyDataSetChanged();

        // 이동한 위치 정보를 상대방에게 보낸다.
        String msg = "PEERMOVE:" + pos;
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, mPeerId);


        // 턴 수를 하나 감소시키고 턴이 끝났으면 상대방에게 턴을 넘긴다.
        if( --remainingTurn <= 0 )
            passTurn();
    }

    void movePeerMarkerPosition(int pos) {
        // 상대방의 말을 이동시킨다.
        for(int i = 0; i < gridItems.length; i++) {
            if(gridItems[i] == peerMarker )
                gridItems[i] = 0;
        }
        gridItems[pos] = peerMarker;

        // 변경사항을 어댑터에게 알린다.
        iAdapter.notifyDataSetChanged();
    }

    // 상대방에게 턴을 넘기는 메소드
    void passTurn() {
        // 전송할 메시지를 설정한다.
        String msg = "PASSTURN";

        // 실제 메시지를 보내는 static method
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, mPeerId);

        isMyTurn = false;
    }

    // 실시간 메시지를 받았을 때 호출되는 메소드
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        byte[] buf = rtm.getMessageData();
        String bufString = new String(buf);

        String sender = rtm.getSenderParticipantId();

        Log.d(TAG, "---onRealTimeMessageReceived() called / " + bufString);

        // 맵 정보를 받았을 때(Handshake 1)
        if(bufString.startsWith("HS1:")) {
            map_info = bufString.substring("HS1:".length());
            Log.d(TAG, "Map info received: " + map_info);

            // 상대방이 왼쪽 말을 가졌으므로 오른쪽 말을 갖는다.
            myMarker = RIGHT_MARKER;
            peerMarker = LEFT_MARKER;

            Toast.makeText(this, "Player connected", Toast.LENGTH_SHORT).show();

            // 맵 정보를 받으면 프로필 사진의 URL을 전송해준다.
            String msg = "HS2:" + myPictureURL;
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);
        }
        // 상대방의 프로필 사진 URL을 받았을 때(Handshake 2)
        else if(bufString.startsWith("HS2:")) {
            peerPictureURL = bufString.substring("HS2:".length());
            Log.d(TAG, "peer picture url received : " + peerPictureURL);

            // 상대방의 프로필 사진 설정
            setProfilePicture(peerPicture, peerPictureURL);

            // 프로필 사진의 URL을 전송해준다.
            String msg = "HS3:" + myPictureURL;
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);
        }
        // 상대방의 프로필 사진 URL을 받았을 때(Handshake 3)
        else if(bufString.startsWith("HS3:")) {
            peerPictureURL = bufString.substring("HS3:".length());
            Log.d(TAG, "peer picture url received : " + peerPictureURL);

            // 상대방의 프로필 사진 설정
            setProfilePicture(peerPicture, peerPictureURL);

            // 나의 닉네임 전송
            String msg = "HS4:" + strMyNick;
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);
        }
        // 상대방의 닉네임을 받았을 때(Handshake 4)
        else if(bufString.startsWith("HS4:")) {
            strPeerNick = bufString.substring("HS4:".length());
            peerNickname.setText(strPeerNick);

            waitingRoomStatus.setText("Player is connected");

            // 게임을 시작한다.
            startGame();

            // 나의 닉네임 전송
            String msg = "HS5:" + strMyNick;
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);
        }
        // 상대방의 닉네임을 받았을 때(Handshake 5)
        else if(bufString.startsWith("HS5:")) {
            strPeerNick = bufString.substring("HS5:".length());
            peerNickname.setText(strPeerNick);

            waitingRoomStatus.setText("Player is connected");

            // 게임을 시작한다.
            startGame();
        }
        // 나에게 턴이 넘어올 때
        else if(bufString.startsWith("PASSTURN")) {
            // 턴을 설정하고 횟수를 초기화한다.
            isMyTurn = true;
            remainingTurn = TURNCNT;
            Toast.makeText(this,"It is My turn", Toast.LENGTH_SHORT).show();
        }
        // 상대방의 말이 이동했을 때
        else if(bufString.startsWith("PEERMOVE:")) {
            int peerPosition = Integer.parseInt(bufString.substring("PEERMOVE:".length()));
            movePeerMarkerPosition(peerPosition);
            Log.i(TAG, "peer moved to position " + peerPosition);
        }
        else {
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
        keepScreenOn();

        // 게임시 필요한 변수들을 초기화한다.
        resetGameVars();

        // 리얼타임 멀티플레이 방을 생성한다.
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());
    }

    // 새로운 게임을 하기위해 관련 변수들을 리셋한다.
    void resetGameVars() {
       /* mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();*/
        mRoomId = null;
        mMyId = null;
        peerPictureURL = null;
        map_info = null;
        isMyTurn = false;
        remainingTurn = TURNCNT;

        // 말의 위치 초기화
        initMarkersPosition();

        // 상대 플레어의 사진을 기본 사진으로 초기화
        peerPicture.setImageResource(R.drawable.photo);

        // 상대 플레이어의 닉네임을 초기화
        peerNickname.setText("searching...");

        // 대기방의 상태 메시지 초기화
        waitingRoomStatus.setText("waiting for player...");
    }

    // 말들의 위치를 초기화한다.
    void initMarkersPosition() {
        for(int i = 0; i < gridItems.length; i++)
            gridItems[i] = 0;

        gridItems[LEFT_START_POSITION] = LEFT_MARKER;
        gridItems[RIGHT_START_POSITION] = RIGHT_MARKER;
        iAdapter.notifyDataSetChanged();
    }

    // 방이 만들어졌을 때(create() 호출시) 호출된다.
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated called (" + room.getCreatorId() + "/" + room.getRoomId() + ")");

        mPDialog.dismiss();

        // 방이 생성되면 나의 Id와 방 ID를 바로 초기화 한다.
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
        mRoomId = room.getRoomId();

        // 에러 발생시 처리
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
            showGameError();
            return;
        }

        // 기본 대기방 화면을 보여준다.
        //showWaitingRoom(room);

        // 커스텀 대기방 화면을 보여준다.
        switchToScreen(R.id.screen_waiting_room);
    }

    // 방이 완전히 연결되었을 때(플레이어간 매칭이 성사되었을 때) 호출된다.
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
            showGameError();
            return;
        }
        updateRoom(room);

        // 방 ID, 참가자들의 ID, 나의 ID를 가져온다.
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
        Log.d(TAG, mMyId);

        // 상대방의 ID 구하기
        for(Participant p : mParticipants) {
            if(p.getParticipantId().equals(mMyId))
                continue;

            mPeerId = p.getParticipantId();
        }

        // 참가자 리스트를 ID 기준으로 정렬한다(플레이어간 동일한 리스트 유지)
        Collections.sort(mParticipants, new Comparator<Participant>() {

            @Override
            public int compare(Participant lhs, Participant rhs) {
                return lhs.getParticipantId().compareTo(rhs.getParticipantId());
            }
        });


        // 리스트의 첫번 째 참가자가 선이 되고 서버에서 맵을 다운받아 상대방에게 전달한다.(맵 동기화)
        // 선이 된 참가자는 왼쪽 말을 자신의 말로 갖는다.
        if (mParticipants.get(0).getParticipantId().equals(mMyId)) {
            isMyTurn = true;    // 턴을 가져온다.
            myMarker = LEFT_MARKER;
            peerMarker = RIGHT_MARKER;
            sendMapInfo();
        }

        // 나와 상대방이 매칭된 화면을 출력한다.
        switchToScreen(R.id.screen_waiting_room);
    }

    private void startGame() {
        switchToScreen(R.id.screen_game);
    }

    // join()메소드에 의해 호출되는 콜백
    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ") : 방에 참가함");
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onJoinedRoom, status " + statusCode);
            showGameError();
        }
    }

    // 정상적으로 방을 나갔을 때 호출되는 메소드(leaveRoom() 호출을 통한)
    // 만약 연결이 끊겨서 나가지는 경우에는 onDisconnectedFromRoom() 메소드가 호출된다.
    @Override
    public void onLeftRoom(int statusCode, String s) {
        // 메인 화면으로 돌아간다.
        Log.d(TAG, "onLeftRoom, code " + statusCode);
        switchToMainScreen();
    }

    // 맵을 다운받고 상대방에게 전송한다.
    void sendMapInfo() {
        // 맵 다운로드
        map_info = downloadMultiMap();
        Log.i(TAG, "downloaded map info : " + map_info);

        String msg = "HS1:" + map_info;
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, mapSentCallback, msg.getBytes(), mRoomId, mPeerId);
    }

    // 상대방에게 맵 정보를 전송한다.
    RealTimeMultiplayer.ReliableMessageSentCallback mapSentCallback = new RealTimeMultiplayer.ReliableMessageSentCallback() {
        @Override
        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
            Log.i(TAG, "onRealTimeMessageSent() : " + statusCode);
            if(statusCode != GamesStatusCodes.STATUS_OK) {
                // Toast.makeText(getApplicationContext(), "map info sent fail", Toast.LENGTH_SHORT).show();
                // Games.RealTimeMultiplayer.leave(mGoogleApiClient, MultiActivity.this, mRoomId);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMapInfo();
            }
            else
                Log.d(TAG, "map info is sent");
        }
    };

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

    // 상대방이 방에 입장했을 때 호출되되는 메소드
    // 이 상태에서는 서로 메시지를 주고받을 수 없다.
    @Override
    public void onPeerJoined(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeerJoined() called : " + participantIds.toString());
        updateRoom(room);
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

    // 상대방이 방에 들어온 후 연결(connected)되었을 때 호출
    // 이 때 부터 메시지를 보내는게 가능하다.
    @Override
    public void onPeersConnected(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeersConnected called / participant id : " + participantIds.get(0));
        updateRoom(room);
        mRoomId = room.getRoomId();
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {updateRoom(room);}

    @Override
    public void onP2PConnected(String s) {}

    @Override
    public void onP2PDisconnected(String s) {}

    // 방의 상태를 업데이트 한다.
    void updateRoom(Room room) {
        if (room != null) {
            mRoomId = room.getRoomId();
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));
        }
        if (mParticipants != null) {
            // updatePeerScoresDisplay();
        }
    }

    // 구글 계정 로그인 성공시 호출
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");

        Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        strMyNick = currentPerson.getDisplayName();


        // 메인 화면 닉네임 출력
        mainNickname.setText(strMyNick);

        // 대기 화면의 닉네임 출력
        myNickname.setText(strMyNick);

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
            //switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
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
    String downloadMultiMap() {
        String server = getString(R.string.server) + getString(R.string.multi_map_load);
        String map = null;
        try {
            map = new HttpAsyncTask(server, null) {

                @Override
                protected void onPostExecute(String result) {

                }
            }.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return map;
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
                mPDialog.dismiss();
            }
        }.execute();
    }

    // 그리드뷰에 이미지를 출력해주는 어댑터
    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public int getCount() {
            return gridItems.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout v = (RelativeLayout)convertView;

            if(v == null) {
                v = (RelativeLayout)mInflater.inflate(R.layout.activity_multi_gridview_item, null);
            }

            // 위치에 따른 배경색 처리
            if(position % 2 == 0)
                v.setBackgroundColor(Color.DKGRAY);
            else
                v.setBackgroundColor(Color.WHITE);


            // 시작 위치와 종료 위치 표시
            TextView startGoal = (TextView)v.findViewById(R.id.start_goal);
            if(position == LEFT_START_POSITION || position == RIGHT_START_POSITION) {
                startGoal.setText("START");
                startGoal.setVisibility(View.VISIBLE);
            } else if(position == LEFT_GOAL_POSITION || position == RIGHT_GOAL_POSITION) {
                startGoal.setText("GOAL");
                startGoal.setVisibility(View.VISIBLE);
            } else
                startGoal.setVisibility(View.INVISIBLE);

            // 말 표시
            ImageView marker = (ImageView)v.findViewById(R.id.marker);
            if(gridItems[position] == LEFT_MARKER) {
                marker.setImageDrawable(new ColorDrawable(Color.BLUE));
                marker.setVisibility(View.VISIBLE);
            }
            else if(gridItems[position] == RIGHT_MARKER) {
                marker.setImageDrawable(new ColorDrawable(Color.RED));
                marker.setVisibility(View.VISIBLE);
            }
            else
                marker.setVisibility(View.INVISIBLE);

            return v;
        }
    }
}