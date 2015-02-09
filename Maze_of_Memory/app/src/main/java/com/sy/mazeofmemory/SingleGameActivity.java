package com.sy.mazeofmemory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class SingleGameActivity extends Activity {

    private Integer[] chess = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            R.drawable.left_game_profile, 0, 0, 0, 0
    };
    private ImageAdapter imageAdapter;
    private int touch_cnt = 0;

    private int chess_startPosition = 20;
    private int pre_chess_Position = chess_startPosition;

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
    private int map_info_startPosition = 72;
    private int map_info_Postion = map_info_startPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_game);

        Intent intent = getIntent();
        map_info.add(intent.getExtras().getString("map_info"));

        game_init();

        GridView gridView = (GridView) findViewById(R.id.game_move);
        gridView.setAdapter(imageAdapter = new ImageAdapter(this));
        gridView.setOnItemClickListener(gridviewOnItemClickListener);


    }

    public void game_init() {

        for (int i = 0; i < map.length; i++) {
            map[i] = map_info.get(0).substring(i, i + 1);
        }
        for (int i = 0; i < chess.length; i++) {
            chess[i] = 0;
        }
        chess[chess_startPosition] = R.drawable.left_game_profile;

        pre_chess_Position = chess_startPosition;
        map_info_Postion = map_info_startPosition;

        //Log.i("map_info",map_info.get(0));

        if(touch_cnt >= 1){
            Toast.makeText(this, "틀렸습니다. 시작 위치로 돌아갑니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void move(int position) {
        //25배열
        chess[pre_chess_Position] = 0;
        chess[position] = R.drawable.left_game_profile;
        pre_chess_Position = position;

    }

    public void game_Move(int position) {

        //아래 클릭
        if (position == pre_chess_Position + 5 && map[map_info_Postion + 9].equals("o")) {
            move(position);
            //81배열
            map_info_Postion = map_info_Postion + 18;
            touchEvent();
        }
        //위 클릭
        else if (position == pre_chess_Position - 5 && map[map_info_Postion - 9].equals("o")) {
            move(position);
            //81배열
            map_info_Postion = map_info_Postion - 18;
            touchEvent();
        }
        //왼쪽 클릭
        else if (position == pre_chess_Position - 1 && map[map_info_Postion - 1].equals("o")) {
            move(position);
            //81배열
            map_info_Postion = map_info_Postion - 2;
            touchEvent();
        }
        //오른쪽 클릭
        else if (position == pre_chess_Position + 1 && map[map_info_Postion + 1].equals("o")) {
            move(position);
            //81배열
            map_info_Postion = map_info_Postion + 2;
            touchEvent();
        } else if ((position == pre_chess_Position + 5 && map[map_info_Postion + 9].equals("x"))
                || (position == pre_chess_Position - 5 && map[map_info_Postion - 9].equals("x"))
                || (position == pre_chess_Position - 1 && map[map_info_Postion - 1].equals("x"))
                || (position == pre_chess_Position + 1 && map[map_info_Postion + 1].equals("x"))) {

            touchEvent();
            game_init();
        }
    }

    public GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            game_Move(position);
        }
    };

    public void touchEvent() {

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;

        int metaState = 0;

        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                x,
                y,
                metaState
        );
        onTouchEvent(motionEvent);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return chess.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;

            final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

            final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setLayoutParams(new GridView.LayoutParams(width, height));

            imageView.setImageResource(chess[position]);

            return imageView;
        }

    }

    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            touch_cnt++;
            Log.i("touch_cnt", "" + touch_cnt);

            if(pre_chess_Position == 4){
                Toast.makeText(this, "Game Clear!!!", Toast.LENGTH_SHORT).show();
                //finish();
                alertDialog();
            }

            TextView textview = (TextView) findViewById(R.id.cnt);
            textview.setText(""+touch_cnt);

            imageAdapter.notifyDataSetChanged();

        }

        return super.onTouchEvent(event);
    }

    private void alertDialog(){
        AlertDialog.Builder ab = new AlertDialog.Builder(SingleGameActivity.this);
        ab.setMessage(Html.fromHtml("<strong><font color=\"#ff0000\"> " + "Html 표현여부 "
                + "</font></strong><br>HTML 이 제대로 표현되는지 본다."));
        ab.setPositiveButton("ok", null);
        ab.show();

    }
}
