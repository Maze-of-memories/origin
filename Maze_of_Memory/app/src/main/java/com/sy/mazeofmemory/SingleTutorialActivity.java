package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnClickListener;


public class SingleTutorialActivity extends Activity {



    public Integer[] chess = {
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            0, 0, 0, 0, 0,
            R.drawable.left_game_profile, 0, 0, 0, 0
    };
    public ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tutorial);

        GridView gridView = (GridView)findViewById(R.id.game_move);
        gridView.setAdapter(imageAdapter = new ImageAdapter(this));
        gridView.setOnItemClickListener(gridviewOnItemClickListener);

        /*해상도 구하기
        DisplayMetrics metrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        float xdpi = metrics.xdpi;
        float ydpi = metrics.ydpi;

        Log.i("normal", "( " + screenWidth + ", " + screenHeight + " )");
        Log.i("normal", "DPI : " + metrics.xdpi + ", " + metrics.ydpi);
        */

    }
    public GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            Toast.makeText(SingleTutorialActivity.this,""+position, Toast.LENGTH_LONG).show();

            for(int i=0; i<25; i++) {
                chess[i] = 0;
            }
            chess[position] = R.drawable.left_game_profile;
            imageAdapter.notifyDataSetChanged();

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

}
