package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


public class SingleTutorialActivity extends Activity{

    public Integer[] chess = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            R.drawable.single_toturial1, 0, 0, 0, 0
    };
    public ImageAdapter imageAdapter;
    private int touch_cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tutorial);

        GridView gridView = (GridView)findViewById(R.id.game_move);
        gridView.setAdapter(imageAdapter = new ImageAdapter(this));
        gridView.setOnItemClickListener(gridviewOnItemClickListener);

    }
    public GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener()  {

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            for (int i = 0; i < 25; i++) {
                chess[i] = 0;
            }
            if(touch_cnt >= 1) {
                chess[position] = R.drawable.left_game_profile;
            }

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
    };

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c){
            mContext = c;
        }
        public int getCount(){
            return chess.length;
        }
        public Object getItem(int position){
            return null;
        }
        public long getItemId(int position){
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent){

            ImageView imageView;

            final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

            final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());

            if(convertView == null){
                imageView = new ImageView(mContext);
            }else{
                imageView = (ImageView)convertView;
            }
            imageView.setLayoutParams(new GridView.LayoutParams( width, height ));

            imageView.setImageResource(chess[position]);

            return imageView;
        }
    }
    public boolean onTouchEvent(MotionEvent event){

        if(event.getAction() == MotionEvent.ACTION_DOWN){

            touch_cnt++;
            if(touch_cnt == 1) {
                findViewById(R.id.single_tutorial_message).setVisibility(View.GONE);
                chess[20] = R.drawable.left_game_profile;
            }
            imageAdapter.notifyDataSetChanged();
            if(touch_cnt == 5){
                /*
                Intent intent = new Intent(SingleTutorialActivity.this, NicknameActivity.class);
                startActivity(intent);
                finish();
                */
            }

        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent(SingleTutorialActivity.this, SingleActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
