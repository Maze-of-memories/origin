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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private int map_info_startPosition = 72;
    private int map_info_Postion = map_info_startPosition;

    private static final String dbName = "single.db";
    private static final String tableName = "SINGLE_MAP_5";
    public static final int dbVersion = 1;
    private SQLiteOpenHelper opener;
    private SQLiteDatabase db;
    private int position;
    boolean game_clear = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_game);

        init();

        GridView gridView = (GridView) findViewById(R.id.game_move);
        gridView.setAdapter(imageAdapter = new ImageAdapter(this));
        gridView.setOnItemClickListener(gridviewOnItemClickListener);

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

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //게임조작

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

    }

    public void move(int position) {

        move_cnt++;
        chess[pre_chess_Position] = 0;
        chess[position] = R.drawable.left_game_profile;
        pre_chess_Position = position;

    }

    public void game_Move(int position) {

        //아래 클릭
        if (position == pre_chess_Position + 5 && map[map_info_Postion + 9].equals("o")) {
            move(position);
            map_info_Postion = map_info_Postion + 18;
        }
        //위 클릭
        else if (position == pre_chess_Position - 5 && map[map_info_Postion - 9].equals("o")) {
            move(position);
            map_info_Postion = map_info_Postion - 18;
        }
        //왼쪽 클릭
        else if (position == pre_chess_Position - 1 && map[map_info_Postion - 1].equals("o")) {
            move(position);
            map_info_Postion = map_info_Postion - 2;
        }
        //오른쪽 클릭
        else if (position == pre_chess_Position + 1 && map[map_info_Postion + 1].equals("o")) {
            move(position);
            map_info_Postion = map_info_Postion + 2;
        } else if ((position == pre_chess_Position + 5 && map[map_info_Postion + 9].equals("x"))
                || (position == pre_chess_Position - 5 && map[map_info_Postion - 9].equals("x"))
                || (position == pre_chess_Position - 1 && map[map_info_Postion - 1].equals("x"))
                || (position == pre_chess_Position + 1 && map[map_info_Postion + 1].equals("x"))) {

            move_cnt = 0;
            fail_cnt++;
            Toast.makeText(this, "틀렸습니다. 시작 위치로 돌아갑니다.", Toast.LENGTH_SHORT).show();
            game_init();

        }
    }

    public GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            if (!game_clear) {
                game_Move(position);
                setText();

                if (pre_chess_Position == 4) {
                    game_clear();
                }

                imageAdapter.notifyDataSetChanged();
            }
        }
    };

    public void setText() {
        TextView textView = (TextView) findViewById(R.id.move_cnt);
        textView.setText("Move : " + move_cnt);

        textView = (TextView) findViewById(R.id.fail_cnt);
        textView.setText("Fail : " + fail_cnt + " / 8");

        textView = (TextView) findViewById(R.id.perfect_cnt);
        textView.setText("Perfect : " + perfect_cnt);
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
                imageAdapter.notifyDataSetChanged();

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

        setText();

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
