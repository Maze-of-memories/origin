package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class SingleGameActivity extends Activity implements AdapterView.OnItemClickListener, Animation.AnimationListener {

    private int move_cnt = 0;
    private int fail_cnt = 0;
    private int perfect_cnt = 0;
    private int chess_startPosition = 20;
    private int pre_chess_Position = chess_startPosition;
    private int stage_num;

    private ArrayList<String> map_info = new ArrayList<String>();

    private String[] map = {
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0",
            "0", "0", "0", "0", "0", "0", "0", "0", "0"
    };

    private static final String dbName = "single.db";
    private static final String tableName = "SINGLE_MAP_5";
    public static final int dbVersion = 1;
    private SQLiteOpenHelper opener;
    private SQLiteDatabase db;
    private int position;
    boolean game_clear = false;

    // size of map
    final static int MAP_SIZE = 5;
    final static int REAL_MAP_SIZE = MAP_SIZE * 2 - 1;

    // 말의 위치
    final int LEFT_MARKER = 1;

    // 말
    int myMarker;

    // 화면 상(그리드뷰) 각 플레이어의 시작 위치와 종료 위치
    private static final int LEFT_START_POSITION = 20;
    private static final int LEFT_GOAL_POSITION = 4;

    // 화면 상 플레이어의 위치
    private int myMarkerStartPosition;
    private int myMarkerGoalPosition;
    private int myMarkerPosition = LEFT_START_POSITION;
    private int myPreviousPosition;

    // 실제 맵에서 각 플레어의 시작위치
    private static final int MAP_LEFT_START_POSITION = 72;

    // 실제 맵에서 플레이어의 현재 위치
    private int myRealPosition;
    private int myRealStartPosition;

    // 맵을 표현하는 그리드뷰
    GridView mapView;
    MapViewAdpater mapViewAdapter;

    private Integer[] gridItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_game);

        init();

        // 말의 위치를 나타내는 배열 초기화
        gridItems = new Integer[MAP_SIZE * MAP_SIZE];
        for (int i = 0; i < MAP_SIZE * MAP_SIZE; i++)
            gridItems[i] = new Integer(0);

        // 미로 그리드뷰 초기화
        mapView = (GridView) findViewById(R.id.game_move);
        mapViewAdapter = new MapViewAdpater(this);
        mapView.setAdapter(mapViewAdapter);
        mapView.setOnItemClickListener(this);

        if (!isDBExists()) {
            copyDB();
        }

        opener = new MySQLiteOpenHelper(SingleGameActivity.this, dbName, null, dbVersion);
        db = opener.getReadableDatabase();
        select();
        db.close();

        game_init();

    }

    public void init() {

        Intent intent = getIntent();
        stage_num = intent.getExtras().getInt("stage_num");
        position = intent.getExtras().getInt("position");

        TextView textView = (TextView) findViewById(R.id.stage_num);
        textView.setText("Stage" + stage_num);

        myMarker = LEFT_MARKER;                             // 내가 조작하게될 말
        myMarkerStartPosition = LEFT_START_POSITION;        // 내 말의 시작 위치
        myMarkerGoalPosition = LEFT_GOAL_POSITION;          // 내 말의 도착 위치
        myMarkerPosition = LEFT_START_POSITION;             // 내 말의 현재 위치
        myRealStartPosition = MAP_LEFT_START_POSITION;      // 나의 실제 시작 위치
        myRealPosition = MAP_LEFT_START_POSITION;           // 나의 실제 현재 위치

        // 각자의 현재 위치와 이전위치를 동일하게 설정해준다.
        myPreviousPosition = myMarkerPosition;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //게임조작
    public void game_init() {

        for (int i = 0; i < map.length; i++) {
            map[i] = map_info.get(0).substring(i, i + 1);
        }
    }

    public void move(int position) {

        move_cnt++;

        /////////////////////////////////////////////////////////
        myPreviousPosition = myMarkerPosition;
        myMarkerPosition = position;

        mapViewAdapter.notifyDataSetChanged();

        /////////////////////////////////////////////////////////////
        pre_chess_Position = position;

    }

    public void setText() {
        TextView textView = (TextView) findViewById(R.id.move_cnt);
        textView.setText("Move : " + move_cnt);

        textView = (TextView) findViewById(R.id.fail_cnt);
        textView.setText("Fail : " + fail_cnt + " / 8");
        /*
        textView = (TextView) findViewById(R.id.perfect_cnt);
        textView.setText("Perfect : " + perfect_cnt);
        */
    }

    //추가
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!game_clear) {
            // 위치 검증(상하좌우로만 이동가능)
            if ((position == myMarkerPosition + MAP_SIZE)
                    || (position == myMarkerPosition - MAP_SIZE)
                    || (position == myMarkerPosition - 1 && myMarkerPosition % MAP_SIZE != 0)
                    || (position == myMarkerPosition + 1 && myMarkerPosition % MAP_SIZE != MAP_SIZE - 1))
                move(position);

            TextView textView = (TextView) findViewById(R.id.move_cnt);
            textView.setText("Move : " + move_cnt);

            if (pre_chess_Position == 4) {
                game_clear();
            }
        }
    }

    // 이동하기 전과 후의 위치를 비교하여 벽으로 막혔는지 검사하는 메소드
    public boolean isBlockedByWall(int prePos, int curPos) {

        // 그리드뷰 상의 위치를 실제 미로상의 위치로 변환한다.
        prePos = mapViewAdapter.convertPosition(prePos);
        curPos = mapViewAdapter.convertPosition(curPos);

        // 이동한 방향으로 벽이 없으면 true를 리턴한다.
        if ((curPos == prePos + (REAL_MAP_SIZE * 2) && map[prePos + REAL_MAP_SIZE].equals("o"))
                || (curPos == prePos - (REAL_MAP_SIZE * 2) && map[prePos - REAL_MAP_SIZE].equals("o"))
                || (curPos == prePos + 2 && map[prePos + 1].equals("o"))
                || (curPos == prePos - 2 && map[prePos - 1].equals("o")))
            return false;
        else
            return true;

    }

    ///////////////////////////////////////////////////////////////
    @Override
    public void onAnimationStart(Animation animation) {
        mapView.setEnabled(false);
    }

    @Override
    public void onAnimationEnd(Animation animation) {

        if (isBlockedByWall(myPreviousPosition, myMarkerPosition)) {
            // 벽으로 막혔으면 시작위치로 재설정한다.
            myPreviousPosition = myMarkerPosition = myMarkerStartPosition;
            move_cnt = 0;
            fail_cnt++;
            Toast.makeText(this, "틀렸습니다. 시작 위치로 돌아갑니다.", Toast.LENGTH_SHORT).show();

            setText();

        } else {
            // 이동이 완료된 후 이전 위치를 현재 위치와 같게 해준다.(애니메이션이 동작하지 않도록)
            myPreviousPosition = myMarkerPosition;
        }

        mapViewAdapter.notifyDataSetChanged();

        mapView.setEnabled(true);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //////////////////////////////////////////////////////////////

    // 그리드뷰에 이미지를 출력해주는 어댑터 클래스
    public class MapViewAdpater extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private final int MAP_SIZE = 5;
        private final int REAL_SIZE = MAP_SIZE * 2 - 1;
        private boolean wallVisibility = false;

        private Animation disappearToRignt, disappearToLeft, disappearToUp, disappearToDown;
        private Animation appearFromRight, appearFromLeft, appearFromUp, appearFromDown;
        private Animation blink;

        public MapViewAdpater(Context c) {
            mContext = c;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 애니메이션 생성
            disappearToRignt = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_right);
            disappearToLeft = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_left);
            disappearToUp = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_up);
            disappearToDown = AnimationUtils.loadAnimation(mContext, R.anim.disappear_to_down);

            disappearToRignt.setAnimationListener(SingleGameActivity.this);
            disappearToLeft.setAnimationListener(SingleGameActivity.this);
            disappearToUp.setAnimationListener(SingleGameActivity.this);
            disappearToDown.setAnimationListener(SingleGameActivity.this);

            appearFromRight = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_right);
            appearFromLeft = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_left);
            appearFromUp = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_up);
            appearFromDown = AnimationUtils.loadAnimation(mContext, R.anim.appear_from_down);

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
            ImageView directionMarker = (ImageView) v.findViewById(R.id.direction_marker);

            if (position == myMarkerPosition - 5) {
                // 위쪽 방향 표시
                directionMarker.setImageResource(R.drawable.arrow_up);
                directionMarker.setVisibility(View.VISIBLE);
                directionMarker.startAnimation(blink);
            } else if (position == myMarkerPosition + 5) {
                // 아래쪽 방향 표시
                directionMarker.setImageResource(R.drawable.arrow_down);
                directionMarker.setVisibility(View.VISIBLE);
                directionMarker.startAnimation(blink);
            } else if (position == myMarkerPosition - 1 && myMarkerPosition % 5 != 0) {
                // 왼쪽 방향 표시
                directionMarker.setImageResource(R.drawable.arrow_left);
                directionMarker.setVisibility(View.VISIBLE);
                directionMarker.startAnimation(blink);
            } else if (position == myMarkerPosition + 1 && myMarkerPosition % 5 != 4) {
                // 오른쪽 방향 표시
                directionMarker.setImageResource(R.drawable.arrow_right);
                directionMarker.setVisibility(View.VISIBLE);
                directionMarker.startAnimation(blink);
            } else {
                directionMarker.setVisibility(View.INVISIBLE);
                directionMarker.clearAnimation();
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout v = (RelativeLayout) convertView;

            if (v == null) {
                v = (RelativeLayout) mInflater.inflate(R.layout.activity_multi_gridview_item, null);
            }

            v.findViewById(R.id.top_wall).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.bottom_wall).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.left_wall).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.right_wall).setVisibility(View.INVISIBLE);

            ImageView marker = (ImageView) v.findViewById(R.id.marker);
            ImageView leftMarker = (ImageView) v.findViewById(R.id.left_marker);
            ImageView rightMarker = (ImageView) v.findViewById(R.id.right_marker);

            // 이동 가능 방향 이미지
            ImageView directionMarker = (ImageView) v.findViewById(R.id.direction_marker);
            directionMarker.setVisibility(View.INVISIBLE);
            directionMarker.clearAnimation();

            // 위치에 따른 배경색 처리
            if (position % 2 == 0)
                v.setBackgroundColor(getResources().getColor(R.color.map_tile_dark));
            else
                v.setBackgroundColor(getResources().getColor(R.color.map_tile_light));

            // 종료 위치, 시작 위치 표시
            TextView startGoal = (TextView) v.findViewById(R.id.start_goal);
            if (position == LEFT_GOAL_POSITION) {
                v.setBackgroundColor(getResources().getColor(R.color.blue));
                startGoal.setText("GOAL");
                startGoal.setVisibility(View.VISIBLE);
            } else if (position == LEFT_START_POSITION) {
                v.setBackgroundColor(getResources().getColor(R.color.blue));
                startGoal.setText("START");
                startGoal.setVisibility(View.VISIBLE);
            } else
                startGoal.setVisibility(View.INVISIBLE);

            /* 말 표시 */
            ImageView myMarker, peerMarker;
            if (myMarkerStartPosition == LEFT_START_POSITION) {
                myMarker = leftMarker;
                peerMarker = rightMarker;
            } else {
                myMarker = rightMarker;
                peerMarker = leftMarker;
            }

            // 기본적으로 모든 말이 안보이는 상태에서 시작한다.
            myMarker.setVisibility(View.INVISIBLE);
            peerMarker.setVisibility(View.INVISIBLE);
            marker.setVisibility(View.INVISIBLE);

            // 내 말이 사라지는 포지션일때
            if (position == myPreviousPosition && position != myMarkerPosition) {

                // 사라지는 애니메이션 적용
                if (myMarkerPosition == myPreviousPosition + 1)
                    myMarker.startAnimation(disappearToRignt);
                else if (myMarkerPosition == myPreviousPosition - 1)
                    myMarker.startAnimation(disappearToLeft);
                else if (myMarkerPosition == myPreviousPosition + MAP_SIZE)
                    myMarker.startAnimation(disappearToDown);
                else
                    myMarker.startAnimation(disappearToUp);

                myMarker.setVisibility(View.INVISIBLE);
            }

            // 내 말이 나타나는 포지션일때
            if (position == myMarkerPosition && position != myPreviousPosition) {

                // 나타나는 애니메이션 적용
                if (myMarkerPosition == myPreviousPosition + 1)
                    myMarker.startAnimation(appearFromLeft);
                else if (myMarkerPosition == myPreviousPosition - 1)
                    myMarker.startAnimation(appearFromRight);
                else if (myMarkerPosition == myPreviousPosition + MAP_SIZE)
                    myMarker.startAnimation(appearFromUp);
                else
                    myMarker.startAnimation(appearFromDown);
            }

            // 내 말이 움직이지 않고 고정되어 있을 때
            if (position == myMarkerPosition && position == myPreviousPosition)
                myMarker.setVisibility(View.VISIBLE);

            /* 이동 가능한 방향 표시 */
            if (myPreviousPosition != myMarkerPosition) {
                // 애니메이션이 작동할 때는 방향 표시를 하지 않는다.
                v.findViewById(R.id.direction_marker).setVisibility(View.INVISIBLE);
                v.findViewById(R.id.direction_marker).clearAnimation();
            } else
                displayDirectionMarker(v, position);

            return v;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //게임 클리어
    public void game_clear() {
        game_clear = true;

        Toast.makeText(SingleGameActivity.this, "Game Clear!!!", Toast.LENGTH_SHORT).show();

        findViewById(R.id.R_single_clear).setVisibility(View.VISIBLE);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.stage_clear_star);
        linearLayout.removeAllViews();

        if (fail_cnt < 3) {
            for (int i = 0; i < 3; i++) {
                ImageView view = new ImageView(this);
                view.setImageResource(android.R.drawable.btn_star_big_on);
                linearLayout.addView(view);
            }
        } else if (fail_cnt < 6) {
            ImageView view_off = new ImageView(this);
            view_off.setImageResource(android.R.drawable.btn_star_big_off);
            linearLayout.addView(view_off);
            for (int i = 0; i < 2; i++) {
                ImageView view = new ImageView(this);
                view.setImageResource(android.R.drawable.btn_star_big_on);
                linearLayout.addView(view);
            }
        } else if (fail_cnt < 9) {
            for (int i = 0; i < 2; i++) {
                ImageView view_off = new ImageView(this);
                view_off.setImageResource(android.R.drawable.btn_star_big_off);
                linearLayout.addView(view_off);
            }
            ImageView view = new ImageView(this);
            view.setImageResource(android.R.drawable.btn_star_big_on);
            linearLayout.addView(view);
        } else {
            for (int i = 0; i < 3; i++) {
                ImageView view_off = new ImageView(this);
                view_off.setImageResource(android.R.drawable.btn_star_big_off);
                linearLayout.addView(view_off);
            }
        }

        Button btn;

        btn = (Button) findViewById(R.id.exit);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SingleGameActivity.this, SingleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        btn = (Button) findViewById(R.id.retry);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                game_clear = false;

                findViewById(R.id.R_single_clear).setVisibility(View.GONE);
                fail_cnt = 0;
                move_cnt = 0;
                setText();
                game_init();
                //imageAdapter.notifyDataSetChanged();

            }
        });
        btn = (Button) findViewById(R.id.next);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SingleGameActivity.this, SingleGameActivity.class);
                intent.putExtra("stage_num", stage_num + 1);
                intent.putExtra("position", position + 1);
                finish();
                startActivity(intent);
            }
        });

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //DB

    public void select() {

        Cursor cursr = db.query(tableName, null, null, null, null, null, null);

        cursr.moveToPosition(position);

        map_info.add(cursr.getString(cursr.getColumnIndex("MAP_INFO")));
        perfect_cnt = cursr.getInt(cursr.getColumnIndex("DISTANCE"));

        Log.i("map_info", map_info.get(0).toString());
        Log.i("perfect_cnt", "" + perfect_cnt);

    }

    public void copyDB() {
        AssetManager assetMgr = getApplication().getAssets();
        File directory = new File("/data/data/com.sy.mazeofmemory/databases");
        File addressDB = new File("/data/data/com.sy.mazeofmemory/databases/" + dbName);

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            InputStream is = assetMgr.open(dbName);
            BufferedInputStream bis = new BufferedInputStream(is);

            // make the directory first
            if (!directory.exists())
                directory.mkdir();

            // if file exists, remove and regenerate
            if (addressDB.exists()) {
                addressDB.delete();
                addressDB.createNewFile();
            }

            fos = new FileOutputStream(addressDB);
            bos = new BufferedOutputStream(fos);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = bis.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();

            bos.close();
            fos.close();
            bis.close();
            is.close();

        } catch (IOException e) {
            Log.e("SingleDBManager : ", e.getMessage());
        }
    }

    public boolean isDBExists() {
        File addressDB = new File("/data/data/com.sy.mazeofmemory/databases/" + dbName);

        if (addressDB.exists()) {
            Log.i("SingleDBManager : ", "DB exists.");
            return true;
        } else {
            Log.i("SingleDBManager : ", "DB not exists.");
            return false;
        }
    }

}
