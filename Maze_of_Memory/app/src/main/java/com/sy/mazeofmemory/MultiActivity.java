package com.sy.mazeofmemory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
        AdapterView.OnItemClickListener, Animation.AnimationListener {

    private static final String TAG = "MultiActivity";

    // startActivityForResult 에서 사용되는 Request 코드
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // size of map
    final static int MAP_SIZE = 5;
    final static int REAL_MAP_SIZE = MAP_SIZE * 2 - 1;

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
            R.id.button_play, R.id.button_pass_turn, R.id.button_give_up,
            R.id.button_exit, R.id.button_rematch, R.id.button_newmatch
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

    ImageView mPlayingMyPicture;
    ImageView mPlayingPeerPicture;

    // 턴 관련 변수
    private final static int TURNCNT = 3;
    private boolean isMyTurn = false;       /* 턴 여부 */
    private int remainingMoveCnt = 0;    /* 남은 턴 수 */
    private TextView mTvRemainingMoveCnt;           /* 남은 턴 수를 출력할 텍스트뷰 */

    // 타이머
    CountDownTimer timer;
    ProgressBar mMyProgressBar;
    ProgressBar mPeerProgressBar;

    // 말
    int myMarker;
    int peerMarker;

    // 화면 상(그리드뷰) 각 플레이어의 시작 위치와 종료 위치
    private static final int LEFT_START_POSITION = 20;
    private static final int RIGHT_START_POSITION = 24;
    private static final int LEFT_GOAL_POSITION = 4;
    private static final int RIGHT_GOAL_POSITION = 0;
    
    // 화면 상 플레이어의 위치
    private int myMarkerStartPosition;
    private int myMarkerGoalPosition;
    private int myMarkerPosition;
    private int myPreviousPosition;

    private int peerMarkerPosition;
    private int peerPreviousPosition;
    private int peerMarkerStartPosition;

    // 실제 맵에서 각 플레어의 시작위치
    private static final int MAP_LEFT_START_POSITION = 72;
    private static final int MAP_RIGHT_START_POSITION = 80;
    
    // 실제 맵에서 플레이어의 현재 위치
    private int myRealPosition;
    private int myRealStartPosition;

    // 맵 정보
    String map;

    // 맵을 표현하는 그리드뷰
    GridView mapView;
    MapViewAdpater mapViewAdapter;

    private Integer[] gridItems;

    // 게임 상태
    private boolean mIsGamePlaying;

    // 게임 종료 다이얼로그
    AlertDialog mGameEndDialog;
    RelativeLayout mGameEndDialogLayout;

    // 나와 상대방의 리매치 대기 정보
    boolean mIWantRematch = false;
    boolean mPeerWantRematch = false;

    // new match 버튼 클릭 정보
    boolean isNewMatch = false;

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

        // 메인화면 전적 출력
        setMainScreenRecord();

        // 대기화면 프로필 사진
        myPicture = (ImageView)findViewById(R.id.my_picture);
        peerPicture = (ImageView)findViewById(R.id.peer_picture);

        // 대기화면 닉네임
        myNickname = (TextView)findViewById(R.id.my_nick);
        peerNickname = (TextView)findViewById(R.id.peer_nick);

        // 대기화면 상태 메시지
        waitingRoomStatus = (TextView)findViewById(R.id.waiting_room_status);

        // 말의 위치를 나타내는 배열 초기화
        gridItems = new Integer[MAP_SIZE * MAP_SIZE];
        for(int i = 0; i < MAP_SIZE * MAP_SIZE; i++)
            gridItems[i] = new Integer(0);

        // 미로 그리드뷰 초기화
        mapView = (GridView)findViewById(R.id.gridView);
        mapViewAdapter = new MapViewAdpater(this);
        mapView.setAdapter(mapViewAdapter);
        mapView.setOnItemClickListener(this);
        mapView.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });

        // 카운트다운 타이머 초기화
        timer = new CountDownTimer(15000, 10) {

            @Override
            public void onTick(long millisUntilFinished) {
                if(isMyTurn)
                    mMyProgressBar.setProgress((int)millisUntilFinished / 10);
                else
                    mPeerProgressBar.setProgress((int)millisUntilFinished / 10);
            }

            @Override
            public void onFinish() {

                if(isMyTurn)
                    mMyProgressBar.setProgress(0);
                else
                    mPeerProgressBar.setProgress(0);

                // 카운터가 종료되었을 때 자신의 턴이라면 턴을 상대방에게 넘겨준다.
                //  -- 자신의 턴이 아닌경우, 상대쪽에서 턴을 보내오기 때문에
                //  -- 특별한 동작이 필요하지 않다.
                if(isMyTurn)
                    passTurn();
            }
        };

        // 남은 이동횟수를 출력할 텍스트뷰
        mTvRemainingMoveCnt = (TextView)findViewById(R.id.txt_turn_left);

        // 타이머 프로그레스 바
        mMyProgressBar = (ProgressBar)findViewById(R.id.my_progressBar);
        mPeerProgressBar = (ProgressBar)findViewById(R.id.peer_progressBar);

        // 게임 종료 다이얼로그 생성
        makeGameEndDialog();

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
                createMultiPlayRoom();
                break;

            // 턴 넘김(턴 종료) 버튼 클릭
            case R.id.button_pass_turn  :
                if(!isMyTurn) return;

                Log.d(TAG,"pass turn button clicked");
                passTurn();
                break;

            // 게임 포기 버튼 클릭
            case R.id.button_give_up:
                showGiveUpDialog();
                break;

            // exit 버튼 클릭
            case R.id.button_exit:
                leaveRoom();
                break;

            // rematch 버튼 클릭(게임 화면)
            case R.id.button_rematch:
                // 상대방에게 rematch 요청을 보낸다.
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, "REMATCH".getBytes(), mRoomId, mPeerId);

                if (mPeerWantRematch) {
                    // 상대방이 이미 rematch를 요청한 상태면 바로 매치를 시작한다.
                    rematch();
                } else {
                    // 상대방이 아직 rematch 요청을 보내오지 않았다면 요청 전송 후 메시지를 출력한다.
                    mIWantRematch = true;
                    TextView state = (TextView)findViewById(R.id.txt_rematch_state);
                    state.setText(getString(R.string.REMATCH_REQ_SENT));
                    state.setVisibility(View.VISIBLE);
                    v.setEnabled(false);
                }
                break;
            // new match 버튼 클릭(게임 화면)
            case R.id.button_newmatch:
                newMatch();
                break;
        }
    }

    // 그리드뷰 아이템 클릭 이벤트 처리
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 내 턴이 아니면 말을 움직이지 않고 바로 빠져나온다.
        if(!isMyTurn) {
            // Toast.makeText(this, "It is not your turn", Toast.LENGTH_SHORT).show();
            return;
        }

        // 위치 검증(상하좌우로만 이동가능)
        if((position == myMarkerPosition + MAP_SIZE)
                || (position == myMarkerPosition - MAP_SIZE)
                || (position == myMarkerPosition - 1 && myMarkerPosition % MAP_SIZE != 0)
                || (position == myMarkerPosition + 1 && myMarkerPosition % MAP_SIZE != MAP_SIZE - 1))
            moveMyMarkerPosition(position);

        // checkAndMove(position);

    }

    // 이동하기 전과 후의 위치를 비교하여 벽으로 막혔는지 검사하는 메소드
    public boolean isBlockedByWall(int prePos, int curPos) {

        // 그리드뷰 상의 위치를 실제 미로상의 위치로 변환한다.
        prePos = mapViewAdapter.convertPosition(prePos);
        curPos = mapViewAdapter.convertPosition(curPos);

        // 이동한 방향으로 벽이 없으면 true를 리턴한다.
        if((curPos == prePos + (REAL_MAP_SIZE * 2) && map.charAt(prePos + REAL_MAP_SIZE) == 'o')
                || (curPos == prePos - (REAL_MAP_SIZE * 2) && map.charAt(prePos - REAL_MAP_SIZE) == 'o')
                || (curPos == prePos + 2 && map.charAt(prePos + 1) == 'o')
                || (curPos == prePos - 2 && map.charAt(prePos - 1) == 'o'))
            return false;
        else
            return true;

    }

    // 클릭한 방향의 벽을 검사한뒤 말을 이동시킨다.
    //  -- 클릭한 방향으로 벽이 없으면 해당 position으로 말을 이동시킨다.
    //  -- 클릭한 방향으로 벽이 있으면 시작위치로 말을 이동시킨다.
    public void checkAndMove(int position) {

        // 아래 타일로 이동
        if (position == myMarkerPosition + 5 && map.charAt(myRealPosition + 9) == 'o') {
            moveMyMarkerPosition(position);     // 화면의 말의 위치 이동
            myRealPosition += 18;               // 실제 맵 이동
        }
        // 위 타일로 이동
        else if (position == myMarkerPosition - 5 && map.charAt(myRealPosition - 9) == 'o') {
            moveMyMarkerPosition(position);     // 화면의 말의 위치 이동
            myRealPosition -= 18;               // 실제 맵 이동
        }
        // 왼쪽 타일로 이동
        else if (position == myMarkerPosition - 1 && map.charAt(myRealPosition - 1) == 'o'
                && myMarkerPosition % 5 != 0 ) {
            moveMyMarkerPosition(position);     // 화면의 말의 위치 이동
            myRealPosition -= 2;                // 실제 맵 이동
        }
        // 오른쪽 타일로 이동
        else if (position == myMarkerPosition + 1 && map.charAt(myRealPosition + 1) == 'o'
                && myMarkerPosition % 5 != 4) {
            moveMyMarkerPosition(position);     // 화면의 말의 위치 이동
            myRealPosition += 2;                // 실제 맵 이동
        // 벽으로 막혔을 경우
        } else if ((position == myMarkerPosition + 5 && map.charAt(myRealPosition + 9) == 'x')
                    || (position == myMarkerPosition - 5 && map.charAt(myRealPosition - 9) == 'x')
                    || (position == myMarkerPosition - 1 && map.charAt(myRealPosition - 1) == 'x'
                    && myMarkerPosition % 5 != 0)
                    || (position == myMarkerPosition + 1 && map.charAt(myRealPosition + 1) == 'x'
                    && myMarkerPosition % 5 != 4)) {

                // 말을 시작위치로 이동시킨다.
                moveMyMarkerPosition(myMarkerStartPosition);
                myRealPosition = myRealStartPosition;

                // 상대방에게 턴을 넘긴다.
                passTurn();
        }
    }

    void moveMyMarkerPosition(int pos) {
        // 자신의 말을 이동시킨다.

        myPreviousPosition = myMarkerPosition;
        myMarkerPosition = pos;

        mapViewAdapter.notifyDataSetChanged();

        // 이동한 위치 정보를 상대방에게 보낸다.
        String msg = "PEERMOVE_START:" + pos;
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, mPeerId);

        // 턴 수를 하나 감소시킨다.
        mTvRemainingMoveCnt.setText(--remainingMoveCnt + "");

        /*// 목적지에 도착했을 때
        if(pos == myMarkerGoalPosition) {
            // 내가 이겼으므로 전적에 1승을 추가한다.
            updateMyRecord("WIN");

            // 승리 다이얼로그 출력
            showGameEndDialog("You win!");

            // 내가 이겼음을 상대방에게 알린다.
            msg = "PEERWIN";
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, mPeerId);

            // 게임을 종료시킨다.
            endGame();
        } else {
            // 턴 수를 하나 감소시키고 턴이 끝났으면 상대방에게 턴을 넘긴다.
            mTvRemainingMoveCnt.setText(--remainingMoveCnt + "");

            if( remainingMoveCnt <= 0 )
                passTurn();
        }*/
    }

    // 상대방의 말을 이동시킨다.
    void movePeerMarkerPosition(int pos) {

        peerPreviousPosition = peerMarkerPosition;
        peerMarkerPosition = pos;

        // 변경사항을 어댑터에게 알린다.
        mapViewAdapter.notifyDataSetChanged();
    }

    // 상대방에게 턴을 넘기는 메소드
    void passTurn() {
        // 턴 종료
        isMyTurn = false;

        remainingMoveCnt = 0;
        mTvRemainingMoveCnt.setText(remainingMoveCnt + "");

        // 타이머를 취소시킨다.
        timer.cancel();
        mMyProgressBar.setProgress(0);
        mPlayingMyPicture.setEnabled(false);
        findViewById(R.id.button_pass_turn).setEnabled(false);

        // 턴이 넘어갔으므로 블링크 애니메이션을 중지하기 위해 그리드뷰 어댑터를 갱신해준다.
        mapViewAdapter.notifyDataSetChanged();

        // 전송할 메시지를 설정한다.
        String msg = "PASSTURN";
        // 실제 메시지를 보내는 static method
        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, mPeerId);

        // 타이머를 동작시킨다.
        timer.start();
        mPlayingPeerPicture.setEnabled(true);
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
            map = bufString.substring("HS1:".length());
            Log.d(TAG, "Map info received: " + map);

            // Toast.makeText(this, "Player connected", Toast.LENGTH_SHORT).show();

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

            // 나의 닉네임 전송
            String msg = "HS5:" + strMyNick;
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);
        }
        // 상대방의 닉네임을 받았을 때(Handshake 5)
        else if(bufString.startsWith("HS5:")) {
            strPeerNick = bufString.substring("HS5:".length());
            peerNickname.setText(strPeerNick);

            // 나의 전적 전송
            SharedPreferences sp = getSharedPreferences("MULTI_RECORD", MODE_PRIVATE);
            String msg = "HS6:" + sp.getString("WIN", "0") + "_"
                    + sp.getString("LOSE", "0") + "_"
                    + sp.getString("WIN_RATE", "00.0");
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);
        }
        // 상대방의 전적 정보를 받앟을 때(Handshake 6)
        else if(bufString.startsWith("HS6:")) {
            String peerRecord = bufString.substring("HS6:".length());
            String[] values = peerRecord.split("_");

            // 상대방의 전적을 화면에 출력한다.
            TextView peerWinCnt = (TextView)findViewById(R.id.peer_win_cnt);
            peerWinCnt.setText(values[0]);

            TextView peerLoseCnt = (TextView)findViewById(R.id.peer_lose_cnt);
            peerLoseCnt.setText(values[1]);

            TextView peerWinRate = (TextView)findViewById(R.id.peer_win_rate);
            peerWinRate.setText(values[2] + "%");

            // 나의 전적 전송
            SharedPreferences sp = getSharedPreferences("MULTI_RECORD", MODE_PRIVATE);
            String msg = "HS7:" + sp.getString("WIN", "0") + "_"
                    + sp.getString("LOSE", "0") + "_"
                    + sp.getString("WIN_RATE", "00.0");
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, sender);

            // 게임을 시작한다.
            startGame();
        }
        // 상대방의 전적 정보를 받았을 때(Handshake 7)
        else if(bufString.startsWith("HS7:")) {
            String peerRecord = bufString.substring("HS6:".length());
            String[] values = peerRecord.split("_");

            // 상대방의 전적을 화면에 출력한다.
            TextView peerWinCnt = (TextView)findViewById(R.id.peer_win_cnt);
            peerWinCnt.setText(values[0]);

            TextView peerLoseCnt = (TextView)findViewById(R.id.peer_lose_cnt);
            peerLoseCnt.setText(values[1]);

            TextView peerWinRate = (TextView)findViewById(R.id.peer_win_rate);
            peerWinRate.setText(values[2] + "%");

            waitingRoomStatus.setText("Player is connected");

            // 게임을 시작한다.
            startGame();

            // 상대방에게 턴을 넘긴다.
            passTurn();
        }
        // 나에게 턴이 넘어올 때
        else if(bufString.startsWith("PASSTURN")) {
            // 턴을 설정하고 횟수를 초기화한다.
            isMyTurn = true;
            remainingMoveCnt = TURNCNT;
            mTvRemainingMoveCnt.setText(TURNCNT + "");

            mPeerProgressBar.setProgress(0);
            mPlayingPeerPicture.setEnabled(false);
            findViewById(R.id.button_pass_turn).setEnabled(true);

            // 타이머를 동작시킨다.
            timer.start();
            mPlayingMyPicture.setEnabled(true);

            // 상대방 말의 애니메이션이 작동하지 않도록 한다.
            mapViewAdapter.notifyDataSetChanged();

            // Toast.makeText(this,"It is My turn", Toast.LENGTH_SHORT).show();
        }
        // 상대방의 말이 이동했을 때
        else if(bufString.startsWith("PEERMOVE_START:")) {
            int peerPosition = Integer.parseInt(bufString.substring("PEERMOVE_START:".length()));
            movePeerMarkerPosition(peerPosition);
            Log.i(TAG, "peer moved to position " + peerPosition);
        }
        // 상대방의 말이 이동에 실패했을 때
        else if(bufString.startsWith("PEERMOVE_FAIL")) {

            peerPreviousPosition = peerMarkerPosition = peerMarkerStartPosition;
            mapViewAdapter.notifyDataSetChanged();
        }
        // 상대방의 말이 이동에 성공했을 때
        else if(bufString.startsWith("PEERMOVE_SUCCESS")) {
            peerPreviousPosition = peerMarkerPosition;
            mapViewAdapter.notifyDataSetChanged();
        }
        // 상대방이 이겼을 때
        else if(bufString.startsWith("PEERWIN")) {
            // 상대방이 이겼으므로 전적에 1패를 추가한다.
            updateMyRecord("LOSE");

            // 패배 다이얼로그 출력
            showGameEndDialog("You lose!");

            // 게임 종료
            endGame();
        }
        // 리매치 요청을 받았을 때
        else if(bufString.startsWith("REMATCH")) {
            mPeerWantRematch = true;

            if(mIWantRematch) {
                // 내가 이미 rematch를 요청한 상태라면 바로 매치를 시작한다.
                mGameEndDialog.dismiss();
                rematch();
                // Toast.makeText(this, "REMATCH", Toast.LENGTH_SHORT).show();
            } else {
                // 내가 rematch를 요청한 상태가 아니면 상대에게 요청이 들어왔음을 텍스트로 출력한다.
                TextView state = (TextView)mGameEndDialogLayout.findViewById(R.id.txt_rematch_state);
                state.setText(strPeerNick + " " + getString(R.string.REMATCH_REQ_RECV));
                state.setVisibility(View.VISIBLE);

                // 게임 화면에서 상태 메시지 출력
                state = (TextView)findViewById(R.id.txt_rematch_state);
                state.setText(strPeerNick + " " + getString(R.string.REMATCH_REQ_RECV));
            }
        }
        //
        else if(bufString.startsWith("MAP:")) {
            map = bufString.substring("MAP:".length());
            Log.d(TAG, "Map info received: " + map);
            passTurn();
        }
        else {
            Toast.makeText(this,"Message received: " + (char) buf[0] + "/" + (int) buf[1], Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);
        }

    }

    // 자동 매칭 멀티 게임을 시작한다.
    void createMultiPlayRoom() {
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
        map = null;
        isMyTurn = false;
        remainingMoveCnt = 0;
        timer.cancel();     /* 타이머 중지 */
        mIWantRematch = false;
        mPeerWantRematch = false;
    }

    // 상대를 찾는 대기화면을 초기화한다.
    void resetWaitingScreen() {
        // 대기화면 프로필 사진 초기화
        peerPicture.setImageResource(R.drawable.photo);

        // 대기화면 닉네임 초기화
        peerNickname.setText("Finding...");

        // 대기화면 상태 메시지
        waitingRoomStatus.setText("Finding the player...");
    }

    // 게임 플레이 화면을 구성하는 요소들을 초기화한다.
    void resetGameScreen() {

        // 벽 숨기기
        mapViewAdapter.setWallVisibility(false);

        // 말의 위치 초기화
        initMarkersPosition();

        /* rematch 관련 변수 초기화 */
        // 다이얼로그의 rematch 버튼 및 텍스트 초기화
        mGameEndDialogLayout.findViewById(R.id.rematch).setEnabled(true);
        mGameEndDialogLayout.findViewById(R.id.txt_rematch_state).setVisibility(View.INVISIBLE);

        // 게임화면의 rematch 버튼 및 텍스트 초기화
        findViewById(R.id.button_rematch).setEnabled(true);
        TextView state = (TextView)findViewById(R.id.txt_rematch_state);
        state.setText("");
        state.setVisibility(View.INVISIBLE);
    }

    // 말들의 위치를 초기화한다.
    void initMarkersPosition() {
        /*for(int i = 0; i < gridItems.length; i++)
            gridItems[i] = 0;

        gridItems[LEFT_START_POSITION] = LEFT_MARKER;
        gridItems[RIGHT_START_POSITION] = RIGHT_MARKER;*/

        myMarkerPosition = LEFT_START_POSITION;
        peerMarkerPosition = RIGHT_START_POSITION;
        myPreviousPosition = myMarkerPosition;
        peerPreviousPosition = peerMarkerPosition;

        mapViewAdapter.notifyDataSetChanged();
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

        // 대기화면을 초기화한다.
        resetWaitingScreen();

        // 커스텀 대기방 화면을 보여준다.
        switchToScreen(R.id.screen_waiting_room);

        // 게임 플레이 화면을 초기화한다.
        resetGameScreen();
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

        // 리스트의 첫번 째 참가자가 맵을 다운받아 상대방에게 전달한다.(맵 동기화)
        // 선이 된 참가자는 왼쪽 말을 자신의 말로 갖는다.
        //  -- 각 말의 위치별로 실제 맵에서의 위치를 초기화 한다.
        if (mParticipants.get(0).getParticipantId().equals(mMyId)) {
            myMarker = LEFT_MARKER;                             // 내가 조작하게될 말
            myMarkerStartPosition = LEFT_START_POSITION;        // 내 말의 시작 위치
            myMarkerGoalPosition = LEFT_GOAL_POSITION;          // 내 말의 도착 위치
            myMarkerPosition = LEFT_START_POSITION;             // 내 말의 현재 위치
            myRealStartPosition = MAP_LEFT_START_POSITION;      // 나의 실제 시작 위치
            myRealPosition = MAP_LEFT_START_POSITION;           // 나의 실제 현재 위치
            Log.i(TAG, "my real position : " + myRealPosition);

            peerMarker = RIGHT_MARKER;
            peerMarkerStartPosition = RIGHT_START_POSITION;
            peerMarkerPosition = RIGHT_START_POSITION;

            sendmap();
        } else {
            // 상대방이 왼쪽 말을 가졌으므로 오른쪽 말을 갖는다.
            myMarker = RIGHT_MARKER;
            myMarkerStartPosition = RIGHT_START_POSITION;
            myMarkerGoalPosition = RIGHT_GOAL_POSITION;
            myMarkerPosition = RIGHT_START_POSITION;
            myRealStartPosition = MAP_RIGHT_START_POSITION;
            myRealPosition = MAP_RIGHT_START_POSITION;
            Log.i(TAG, "my real position : " + myRealPosition);

            peerMarker = LEFT_MARKER;
            peerMarkerStartPosition = LEFT_START_POSITION;
            peerMarkerPosition = LEFT_START_POSITION;
        }

        // 각자의 현재 위치와 이전위치를 동일하게 설정해준다.
        myPreviousPosition = myMarkerPosition;
        peerPreviousPosition = peerMarkerPosition;

        // 나와 상대방이 매칭된 화면을 출력한다.
        switchToScreen(R.id.screen_waiting_room);
    }

    private void startGame() {
        mIsGamePlaying = true;

        // 게임 플레이화면 닉네임 출력
        TextView playingMyNick = (TextView)findViewById(R.id.playing_mynick);
        playingMyNick.setText(strMyNick);
        TextView playingPeerNick = (TextView)findViewById(R.id.playing_peernick);
        playingPeerNick.setText(strPeerNick);

        // 게임 플레이화면의 프로필사진 출력
        mPlayingMyPicture = (ImageView)findViewById(R.id.playing_my_picture);
        mPlayingPeerPicture = (ImageView)findViewById(R.id.playing_peer_picture);

        setProfilePicture(mPlayingMyPicture, myPictureURL);
        setProfilePicture(mPlayingPeerPicture, peerPictureURL);

        // 전적을 가져와 출력한다.
        SharedPreferences sp = getSharedPreferences("MULTI_RECORD", MODE_PRIVATE);
        TextView myWinCnt = (TextView)findViewById(R.id.my_win_cnt);
        myWinCnt.setText(sp.getString("WIN", "0"));

        TextView myLoseCnt = (TextView)findViewById(R.id.my_lose_cnt);
        myLoseCnt.setText(sp.getString("LOSE", "0"));

        TextView myWinRate = (TextView)findViewById(R.id.my_win_rate);
        myWinRate.setText(sp.getString("WIN_RATE", "00.0") + "%");

        // 그리드뷰 갱신
        mapViewAdapter.notifyDataSetChanged();

        // 게임 플레이 중 필요한 버튼들을 출력한다.
        findViewById(R.id.playing_button_set).setVisibility(View.VISIBLE);
        findViewById(R.id.end_button_set).setVisibility(View.GONE);

        switchToScreen(R.id.screen_game);
    }

    // 게임 종료 메소드
    private void endGame() {

        mIsGamePlaying = false;

        // 진행중이던 타이머를 중지시킨다.
        timer.cancel();
        mPeerProgressBar.setProgress(0);
        mPlayingPeerPicture.setEnabled(false);

        mMyProgressBar.setProgress(0);
        mPlayingMyPicture.setEnabled(false);

        // 게임 조작이 불가능하도록 턴을 종료시킨다.
        isMyTurn = false;
        mapViewAdapter.notifyDataSetChanged();
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
    public void onLeftRoom(int statusCode, String roomId) {
        // 메인 화면으로 돌아간다.
        Log.d(TAG, "onLeftRoom, code " + statusCode);

        // new match 버튼을 통한 방 나가기가 아닌 경우
        if(!isNewMatch)
            switchToMainScreen();

        isNewMatch = false;
    }

    // 맵을 다운받고 상대방에게 전송한다.
    void sendmap() {
        // 맵 다운로드
        map = downloadMultiMap();
        Log.i(TAG, "downloaded map info : " + map);

        String msg = "HS1:" + map;
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
                sendmap();
            }
            else
                Log.d(TAG, "map info is sent");
        }
    };

    // 취소(cancelled)된 게임에 대한 에러 메시지를 보여준다.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        // switchToMainScreen();
    }

    // 멀티플레이 메인 화면을 보여준다.
    void switchToMainScreen() {
        setMainScreenRecord();
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
    public void onPeerLeft(Room room, List<String> strings) {
        Log.i(TAG, "onPeerLeft() called.");
        updateRoom(room);
    }

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

    // 방과의 연결이 끊길 때 호출된다.
    // 상대방이 방을 나갔을 때 호출되며 승리 화면을 보여준다.
    @Override
    public void onDisconnectedFromRoom(Room room) {
        updateRoom(room);
        Log.i(TAG, "onDisconnectedFromRoom(room) called.");
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
    public void onPeersDisconnected(Room room, List<String> strings) {
        Log.i(TAG, "onPeersDisconnected() called.");
        updateRoom(room);

        // 게임 종료 다이얼로그의 rematch 버튼 비활성화
        mGameEndDialogLayout.findViewById(R.id.rematch).setEnabled(false);
        TextView state = (TextView)mGameEndDialogLayout.findViewById(R.id.txt_rematch_state);
        state.setText(strPeerNick + " " + getString(R.string.PEER_LEFT_ROOM));
        state.setVisibility(View.VISIBLE);

        // 게임 화면의 rematch 버튼 비활성화
        findViewById(R.id.button_rematch).setEnabled(false);
        state = (TextView)findViewById(R.id.txt_rematch_state);
        state.setText(strPeerNick + " " + getString(R.string.PEER_LEFT_ROOM));
        // state.setVisibility(View.VISIBLE);

        // 게임 도중 상대방이 나갔을때만 승리처리를 해준다.
        // 게임이 종료된 후 상대방이 나간것에 대해선 승리처리를 해주지 않는다.
        if(mIsGamePlaying) {
            // 내 전적에 1승 추가
            updateMyRecord("WIN");

            // 승리 다이얼로그 출력
            showGameEndDialog("You win!");

            // 게임 종료
            endGame();
        }

        // 상대방이 나갔으므로 id를 null로 설정한다.
        mPeerId = null;
    }

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

        // 이름 가져오기
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        strMyNick = currentPerson.getName().getGivenName();

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
            // 게임 플레이 중 백키를 누르면 게임 포기 다어얼로그를 띄운다.
            if(mIsGamePlaying)
                showGiveUpDialog();
            // 게임이 종료된 후 백키를 누르면 메인화면으로 이동한다.
            else
                leaveRoom();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_waiting_room) {
            // 대기방에서 백키를 누르면 방을 나간다.
            leaveRoom();
            return true;
        } else {
            return super.onKeyDown(keyCode, e);
        }
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

            // 타이머 중지
            timer.cancel();
        } else {
            switchToMainScreen();
        }
    }

    // 방을 나가는 메소드
    void leaveRoomForNewMatch() {
        Log.d(TAG, "leaveRoom() called.");
        // mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            //switchToScreen(R.id.screen_wait);

            // 타이머 중지
            timer.cancel();
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

    // 메인화면의 전적을 출력하는 메소드
    void setMainScreenRecord() {
        // 메인화면 전적 출력
        SharedPreferences sp = getSharedPreferences("MULTI_RECORD", MODE_PRIVATE);
        TextView mainWinCnt = (TextView)findViewById(R.id.main_win_cnt);
        mainWinCnt.setText(sp.getString("WIN", "0"));

        TextView mainLoseCnt = (TextView)findViewById(R.id.main_lose_cnt);
        mainLoseCnt.setText(sp.getString("LOSE", "0"));

        TextView mainWinRate = (TextView)findViewById(R.id.main_win_rate);
        mainWinRate.setText(sp.getString("WIN_RATE", "00.0") + "%");
    }

    // 승패에 따라 전적을 업데이트 하는 메소드
    void updateMyRecord(String result) {

        // 로컬에 저장되어있는 전적을 업데이트 한다.
        SharedPreferences sp = getSharedPreferences("MULTI_RECORD", MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();

        int winCnt = Integer.parseInt(sp.getString("WIN", "0"));
        int loseCnt = Integer.parseInt(sp.getString("LOSE", "0"));
        float winRate = Float.parseFloat(sp.getString("WIN_RATE", "00.0"));

        if(result.equals("WIN"))
            winCnt++;
        else
            loseCnt++;

        winRate = ((float)winCnt / (float)(winCnt + loseCnt)) * 100;

        spEditor.putString("WIN", winCnt + "");
        spEditor.putString("LOSE", loseCnt + "");
        spEditor.putString("WIN_RATE", String.format("%.1f", winRate));
        spEditor.commit();

        // 서버에 저장된 전적을 업데이트 한다.
        String url = getString(R.string.server) + getString(R.string.update_record);
        String param = "gmail=" + Plus.AccountApi.getAccountName(mGoogleApiClient)
                 + "&game_result=" + result;

        new HttpAsyncTask(url, param) {

            @Override
            protected void onPostExecute(String result) {

            }
        }.execute();
    }

    // 게임 종료 다이얼로그를 생성하는 메소드
    private void makeGameEndDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mGameEndDialogLayout = (RelativeLayout)getLayoutInflater().inflate(R.layout.dialog_multiplay_end, null);
        builder.setView(mGameEndDialogLayout);
        /*builder.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                *//*if (keyCode == KeyEvent.KEYCODE_BACK) {
                    showAnswer();
                    dialog.dismiss();
                }*//*
                return false;
            }
        });*/

        // exit 버튼 클릭
        mGameEndDialogLayout.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveRoom();
                mGameEndDialog.dismiss();
            }
        });

        // rematch 버튼 클릭(다이얼로그)
        mGameEndDialogLayout.findViewById(R.id.rematch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 상대방에게 rematch 요청을 보낸다.
                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, "REMATCH".getBytes(), mRoomId, mPeerId);

                if (mPeerWantRematch) {
                    // 상대방이 이미 rematch를 요청한 상태면 바로 매치를 시작한다.
                    mGameEndDialog.dismiss();
                    rematch();
                } else {
                    // 상대방이 아직 rematch 요청을 보내오지 않았다면 요청 전송 후 메시지를 출력한다.
                    mIWantRematch = true;
                    TextView state = (TextView) mGameEndDialogLayout.findViewById(R.id.txt_rematch_state);
                    state.setText(getString(R.string.REMATCH_REQ_SENT));
                    state.setVisibility(View.VISIBLE);
                    v.setEnabled(false);

                    // 게임화면의 rematch 버튼도 비활성화
                    state = (TextView) findViewById(R.id.txt_rematch_state);
                    state.setText(getString(R.string.REMATCH_REQ_SENT));
                    findViewById(R.id.button_rematch).setEnabled(false);
                }
            }
        });

        // new match 버튼 클릭
        mGameEndDialogLayout.findViewById(R.id.newmatch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newMatch();

                // 다이얼로그 종료
                mGameEndDialog.dismiss();
            }
        });

        // answer버튼 클릭
        mGameEndDialogLayout.findViewById(R.id.answer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAnswer();
                mGameEndDialog.dismiss();
            }
        });

        mGameEndDialog = builder.create();
        mGameEndDialog.setCancelable(false);
        mGameEndDialog.setCanceledOnTouchOutside(false);
    }

    // 개임 종료 다이얼로그 출력
    void showGameEndDialog(String result) {
        // 결과 출력
        TextView txtGameResult = (TextView)mGameEndDialogLayout.findViewById(R.id.txt_game_result);
        txtGameResult.setText(result);

        mGameEndDialog.show();
    }

    // 게임 포기 다이얼로그를 띄우는 메소드
    private void showGiveUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.GIVEUP_DIALOG_TITLE)
                .setMessage(R.string.GIVEUP_DIALOG_MSG)
                .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 전적에 1패를 추가한다.
                        updateMyRecord("LOSE");
                        leaveRoom();
                    }
                })
                .setNegativeButton(R.string.NO, null)
                .create().show();
    }
    // 벽을 보여주는 메소드
    void showAnswer() {
        // 게임 종료 후 필요한 버튼들 및 뷰를 출력한다.
        findViewById(R.id.playing_button_set).setVisibility(View.GONE);
        findViewById(R.id.end_button_set).setVisibility(View.VISIBLE);
        findViewById(R.id.txt_rematch_state).setVisibility(View.VISIBLE);

        mapViewAdapter.setWallVisibility(true);
        mapViewAdapter.notifyDataSetChanged();
    }

    // 상대방과 다시 매치를 시작한다.
    void rematch() {
        mIsGamePlaying = true;

        // playing button set으로 버튼을 교체한다.
        findViewById(R.id.end_button_set).setVisibility(View.GONE);
        findViewById(R.id.playing_button_set).setVisibility(View.VISIBLE);

        // 말의 위치를 초기화 시킨다.
        if (mParticipants.get(0).getParticipantId().equals(mMyId)) {
            myMarker = LEFT_MARKER;                             // 내가 조작하게될 말
            myMarkerStartPosition = LEFT_START_POSITION;        // 내 말의 시작 위치
            myMarkerGoalPosition = LEFT_GOAL_POSITION;          // 내 말의 도착 위치
            myMarkerPosition = LEFT_START_POSITION;             // 내 말의 현재 위치
            myRealStartPosition = MAP_LEFT_START_POSITION;      // 나의 실제 시작 위치
            myRealPosition = MAP_LEFT_START_POSITION;           // 나의 실제 현재 위치
            Log.i(TAG, "my real position : " + myRealPosition);

            peerMarker = RIGHT_MARKER;
            peerMarkerStartPosition = RIGHT_START_POSITION;
            peerMarkerPosition = RIGHT_START_POSITION;

            map = downloadMultiMap();
            Log.i(TAG, "downloaded map info : " + map);

            String msg = "MAP:" + map;
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, msg.getBytes(), mRoomId, mPeerId);
        } else {
            // 상대방이 왼쪽 말을 가졌으므로 오른쪽 말을 갖는다.
            myMarker = RIGHT_MARKER;
            myMarkerStartPosition = RIGHT_START_POSITION;
            myMarkerGoalPosition = RIGHT_GOAL_POSITION;
            myMarkerPosition = RIGHT_START_POSITION;
            myRealStartPosition = MAP_RIGHT_START_POSITION;
            myRealPosition = MAP_RIGHT_START_POSITION;
            Log.i(TAG, "my real position : " + myRealPosition);

            peerMarker = LEFT_MARKER;
            peerMarkerStartPosition = LEFT_START_POSITION;
            peerMarkerPosition = LEFT_START_POSITION;
        }

        myPreviousPosition = myMarkerPosition;
        peerPreviousPosition = peerMarkerPosition;

        // rematch 관련 변수 초기화
        mIWantRematch = false;
        mPeerWantRematch = false;

        mGameEndDialogLayout.findViewById(R.id.rematch).setEnabled(true);
        mGameEndDialogLayout.findViewById(R.id.txt_rematch_state).setVisibility(View.INVISIBLE);

        // playing button set으로 버튼을 교체한다.
        findViewById(R.id.end_button_set).setVisibility(View.GONE);
        findViewById(R.id.playing_button_set).setVisibility(View.VISIBLE);
        findViewById(R.id.button_rematch).setEnabled(true);
        TextView state = (TextView)findViewById(R.id.txt_rematch_state);
        state.setText("");
        state.setVisibility(View.INVISIBLE);

        // 벽 숨기기
        mapViewAdapter.setWallVisibility(false);
        mapViewAdapter.notifyDataSetChanged();

        Log.i(TAG, "rematch() called");
    }

    // 새로운 상대와 새 게임을 시작한다.
    void newMatch() {
        isNewMatch = true;

        // 방을 나간다
        leaveRoomForNewMatch();

        // 새로운 멀티 플레이 게임을 시작한다.
        mPDialog.show();
        createMultiPlayRoom();
    }

    /* 애니메이션이 시작될 때 호출되는 메소드 */
    @Override
    public void onAnimationStart(Animation animation) {
        mapView.setEnabled(false);
    }

    /* 애니메이션이 끝났을 때 호출되는 메소드 */
    @Override
    public void onAnimationEnd(Animation animation) {

        if(isBlockedByWall(myPreviousPosition, myMarkerPosition)) {
            // 벽으로 막혔으면 시작위치로 재설정한다.
            myPreviousPosition = myMarkerPosition = myMarkerStartPosition;
            // 이동에 실패했음을 상대방에게 알린다.
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, "PEERMOVE_FAIL".getBytes(), mRoomId, mPeerId);

            // 이동 가능 수를 0으로 설정하여 더이상 이동할 수 없도록 한다(턴이 넘어감)
            remainingMoveCnt = 0;
        }
        else {
            // 이동이 완료된 후 이전 위치를 현재 위치와 같게 해준다.(애니메이션이 동작하지 않도록)
            myPreviousPosition = myMarkerPosition;
            // 이동에 성공했음을 상대방에게 알린다.
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, "PEERMOVE_SUCCESS".getBytes(), mRoomId, mPeerId);
        }

        mapViewAdapter.notifyDataSetChanged();

        // 목적지에 도착했을 때
        if(myMarkerPosition == myMarkerGoalPosition) {
            // 내가 이겼으므로 전적에 1승을 추가한다.
            updateMyRecord("WIN");

            // 승리 다이얼로그 출력
            showGameEndDialog("You win!");

            // 내가 이겼음을 상대방에게 알린다.
            Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, "PEERWIN".getBytes(), mRoomId, mPeerId);

            // 게임을 종료시킨다.
            endGame();
        } else {

            // 턴이 끝났으면 상대방에게 턴을 넘긴다.
            if( remainingMoveCnt <= 0 )
                passTurn();
        }

        mapView.setEnabled(true);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    // 그리드뷰에 이미지를 출력해주는 어댑터 클래스
    public class MapViewAdpater extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private final int MAP_SIZE = 5;
        private final int REAL_SIZE = MAP_SIZE * 2 - 1;
        private boolean wallVisibility = false;

        private Animation disappearToRignt, disappearToLeft, disappearToUp, disappearToDown;
        private Animation appearFromRight, appearFromLeft, appearFromUp, appearFromDown;
        private Animation disappearToRigntNoLsnr, disappearToLeftNoLsnr, disappearToUpNoLsnr, disappearToDownNoLsnr;
        private Animation blink;

        public MapViewAdpater(Context c) {
            mContext = c;
            mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 애니메이션 생성
            disappearToRignt = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_right);
            disappearToLeft = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_left);
            disappearToUp = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_up);
            disappearToDown = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_down);

            disappearToRignt.setAnimationListener(MultiActivity.this);
            disappearToLeft.setAnimationListener(MultiActivity.this);
            disappearToUp.setAnimationListener(MultiActivity.this);
            disappearToDown.setAnimationListener(MultiActivity.this);

            appearFromRight = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_right);
            appearFromLeft = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_left);
            appearFromUp = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_up);
            appearFromDown = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_down);

            // 리스너가 없는 상대방용 애니메이션
            disappearToRigntNoLsnr = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_right);
            disappearToLeftNoLsnr = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_left);
            disappearToUpNoLsnr = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_up);
            disappearToDownNoLsnr = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_down);

            // 깜빡이는 애니메이션
            blink = AnimationUtils.loadAnimation(mContext, R.anim.blink);
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

        public int convertPosition(int pos) {
            int q = pos / MAP_SIZE;
            int r = pos % MAP_SIZE;

            int realPosition = (q * REAL_SIZE * 2) + (r * 2);
            return realPosition;
        }

        public void setWallVisibility(boolean visibility) {
            wallVisibility = visibility;
        }

        private void displayDirectionMarker(View v, int position) {
            // 이동 가능한 방향 표시
            ImageView directionMarker = (ImageView)v.findViewById(R.id.direction_marker);

            if(isMyTurn) {
                if(position == myMarkerPosition - 5) {
                    // 위쪽 방향 표시
                    directionMarker.setImageResource(R.drawable.arrow_up);
                    directionMarker.setVisibility(View.VISIBLE);
                    directionMarker.startAnimation(blink);
                }
                else if(position == myMarkerPosition + 5) {
                    // 아래쪽 방향 표시
                    directionMarker.setImageResource(R.drawable.arrow_down);
                    directionMarker.setVisibility(View.VISIBLE);
                    directionMarker.startAnimation(blink);
                }
                else if(position == myMarkerPosition - 1 && myMarkerPosition % 5 != 0) {
                    // 왼쪽 방향 표시
                    directionMarker.setImageResource(R.drawable.arrow_left);
                    directionMarker.setVisibility(View.VISIBLE);
                    directionMarker.startAnimation(blink);
                }
                else if(position == myMarkerPosition + 1 && myMarkerPosition % 5 != 4) {
                    // 오른쪽 방향 표시
                    directionMarker.setImageResource(R.drawable.arrow_right);
                    directionMarker.setVisibility(View.VISIBLE);
                    directionMarker.startAnimation(blink);
                }
                else {
                    directionMarker.setVisibility(View.INVISIBLE);
                    directionMarker.clearAnimation();
                }
            } else {
                directionMarker.setVisibility(View.INVISIBLE);
                directionMarker.clearAnimation();
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout v = (RelativeLayout)convertView;

            if(v == null) {
                v = (RelativeLayout)mInflater.inflate(R.layout.activity_multi_gridview_item, null);
            }

            v.findViewById(R.id.top_wall).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.bottom_wall).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.left_wall).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.right_wall).setVisibility(View.INVISIBLE);

            ImageView marker = (ImageView)v.findViewById(R.id.marker);
            ImageView leftMarker = (ImageView)v.findViewById(R.id.left_marker);
            ImageView rightMarker = (ImageView)v.findViewById(R.id.right_marker);

            // 이동 가능 방향 이미지
            ImageView directionMarker = (ImageView)v.findViewById(R.id.direction_marker);
            directionMarker.setVisibility(View.INVISIBLE);
            directionMarker.clearAnimation();

            // 위치에 따른 배경색 처리
            if(position % 2 == 0)
                v.setBackgroundColor(getResources().getColor(R.color.map_tile_dark));
            else
                v.setBackgroundColor(getResources().getColor(R.color.map_tile_light));

            // 종료 위치, 시작 위치 표시
            TextView startGoal = (TextView)v.findViewById(R.id.start_goal);
            if(position == LEFT_GOAL_POSITION ) {
                v.setBackgroundColor(getResources().getColor(R.color.blue));
                startGoal.setText("GOAL");
                startGoal.setVisibility(View.VISIBLE);
            } else if (position == RIGHT_GOAL_POSITION) {
                v.setBackgroundColor(getResources().getColor(R.color.red));
                startGoal.setText("GOAL");
                startGoal.setVisibility(View.VISIBLE);
            } else if(position == LEFT_START_POSITION) {
                v.setBackgroundColor(getResources().getColor(R.color.blue));
                startGoal.setText("START");
                startGoal.setVisibility(View.VISIBLE);
            } else if(position == RIGHT_START_POSITION) {
                v.setBackgroundColor(getResources().getColor(R.color.red));
                startGoal.setText("START");
                startGoal.setVisibility(View.VISIBLE);
            } else
                startGoal.setVisibility(View.INVISIBLE);

            /* 말 표시 */

            ImageView myMarker, peerMarker;
            if(myMarkerStartPosition == LEFT_START_POSITION) {
                myMarker = leftMarker;
                peerMarker = rightMarker;
            }
            else {
                myMarker = rightMarker;
                peerMarker = leftMarker;
            }

            // 기본적으로 모든 말이 안보이는 상태에서 시작한다.
            myMarker.setVisibility(View.INVISIBLE);
            peerMarker.setVisibility(View.INVISIBLE);
            marker.setVisibility(View.INVISIBLE);

            // 내 말이 사라지는 포지션일때
            if(position == myPreviousPosition && position != myMarkerPosition) {

                // 상대방의 말과 같은 위치인지 확인한다.
                if(position == peerMarkerPosition)
                    peerMarker.setVisibility(View.VISIBLE);

                // 사라지는 애니메이션 적용
                if(myMarkerPosition == myPreviousPosition + 1 )
                    myMarker.startAnimation(disappearToRignt);
                else if(myMarkerPosition == myPreviousPosition - 1 )
                    myMarker.startAnimation(disappearToLeft);
                else if(myMarkerPosition == myPreviousPosition + MAP_SIZE)
                    myMarker.startAnimation(disappearToDown);
                else
                    myMarker.startAnimation(disappearToUp);

                myMarker.setVisibility(View.INVISIBLE);
            }

            // 내 말이 나타나는 포지션일때
            if(position == myMarkerPosition && position != myPreviousPosition) {
                // 상대방의 말과 같은 위치인지 확인한다.
                if(position == peerMarkerPosition)
                    peerMarker.setVisibility(View.VISIBLE);

                // 나타나는 애니메이션 적용
                if(myMarkerPosition == myPreviousPosition + 1 )
                    myMarker.startAnimation(appearFromLeft);
                else if(myMarkerPosition == myPreviousPosition - 1 )
                    myMarker.startAnimation(appearFromRight);
                else if(myMarkerPosition == myPreviousPosition + MAP_SIZE)
                    myMarker.startAnimation(appearFromUp);
                else
                    myMarker.startAnimation(appearFromDown);
            }

            // 상대방의 말이 사라지는 포지션일때
            if(position == peerPreviousPosition && position != peerMarkerPosition) {
                // 나의 말과 같은 위치인지 확인한다.
                if(position == myMarkerPosition)
                    myMarker.setVisibility(View.VISIBLE);

                // 사라지는 애니메이션 적용
                if(peerMarkerPosition == peerPreviousPosition + 1 )
                    peerMarker.startAnimation(disappearToRigntNoLsnr);
                else if(peerMarkerPosition == peerPreviousPosition - 1 )
                    peerMarker.startAnimation(disappearToLeftNoLsnr);
                else if(peerMarkerPosition == peerPreviousPosition + MAP_SIZE)
                    peerMarker.startAnimation(disappearToDownNoLsnr);
                else
                    peerMarker.startAnimation(disappearToUpNoLsnr);

                peerMarker.setVisibility(View.INVISIBLE);
            }

            // 상대방의 말이 나타나는 포지션일때
            if(position == peerMarkerPosition && position != peerPreviousPosition) {
                // 나의 말과 같은 위치인지 확인한다.
                if(position == myMarkerPosition)
                    myMarker.setVisibility(View.VISIBLE);

                // 나타나는 애니메이션 적용
                if(peerMarkerPosition == peerPreviousPosition + 1 )
                    peerMarker.startAnimation(appearFromLeft);
                else if(peerMarkerPosition == peerPreviousPosition - 1 )
                    peerMarker.startAnimation(appearFromRight);
                else if(peerMarkerPosition == peerPreviousPosition + MAP_SIZE)
                    peerMarker.startAnimation(appearFromUp);
                else
                    peerMarker.startAnimation(appearFromDown);
            }

            // 내 말이 움직이지 않고 고정되어 있을 때
            if(position == myMarkerPosition && position == myPreviousPosition && position != peerMarkerPosition)
                myMarker.setVisibility(View.VISIBLE);

            // 상대방의 말이 움직이지 않고 고정되어 있을 때
            if(position == peerMarkerPosition && position == peerPreviousPosition && position != myMarkerPosition)
                peerMarker.setVisibility(View.VISIBLE);

            // 내 말과 상대방의 말이 같은 포지션이 고정되어 있을 때
            if(position == myMarkerPosition && position == myPreviousPosition && position == peerMarkerPosition && position == peerPreviousPosition)
                marker.setVisibility(View.VISIBLE);

            /* 이동 가능한 방향 표시 */
            if(myPreviousPosition != myMarkerPosition) {
                // 애니메이션이 작동할 때는 방향 표시를 하지 않는다.
                v.findViewById(R.id.direction_marker).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.direction_marker).clearAnimation();
            }
            else
                displayDirectionMarker(v, position);

            /* 벽 출력 */
            if(wallVisibility) {
                int rPos = convertPosition(position);
                int wPos;

                // 위쪽 벽 검사 및 출력
                wPos = rPos - REAL_SIZE;
                if (wPos >= 0 && map.charAt(wPos) == 'x')
                    v.findViewById(R.id.top_wall).setVisibility(View.VISIBLE);

                // 아래쪽 벽 검사 및 출력
                wPos = rPos + REAL_SIZE;
                if (wPos < REAL_SIZE * REAL_SIZE && map.charAt(wPos) == 'x')
                    v.findViewById(R.id.bottom_wall).setVisibility(View.VISIBLE);

                // 왼쪽 벽 검사 및 출력
                wPos = rPos - 1;
                if (wPos >= 0 && rPos % REAL_SIZE != 0 && map.charAt(wPos) == 'x')
                    v.findViewById(R.id.left_wall).setVisibility(View.VISIBLE);

                // 오른쪽 벽 검사 및 출력
                wPos = rPos + 1;
                if (wPos < REAL_SIZE * REAL_SIZE && rPos % REAL_SIZE != REAL_SIZE - 1 && map.charAt(wPos) == 'x')
                    v.findViewById(R.id.right_wall).setVisibility(View.VISIBLE);
            }

            return v;
        }
    }
}