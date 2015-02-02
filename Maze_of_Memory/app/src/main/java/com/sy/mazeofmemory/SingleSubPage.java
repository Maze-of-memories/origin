package com.sy.mazeofmemory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SingleSubPage extends android.support.v4.app.Fragment {

    private static final String dbName = "single.db";
    private static final String tableName = "SINGLE_MAP_5";
    public static final int dbVersion = 1;
    private SQLiteOpenHelper opener;
    private SQLiteDatabase db;
    private Context context;
    private int position;
    private int btnCnt = 9;
    private int pageCnt;

    private int number;
    private ArrayList<String> map = new ArrayList<String>();

    public SingleSubPage(Context context, int position) {
        this.context = context;
        this.position = position * btnCnt;
        this.pageCnt = position;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isDBExists()) {
            copyDB();
        }

        opener = new MySQLiteOpenHelper(context, dbName, null, dbVersion);
        db = opener.getReadableDatabase();
        select();
        db.close();

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());

        Button[] btnWord = new Button[btnCnt];
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.activity_single_sub_page, container, false);
        LinearLayout R_sub_page = (LinearLayout) linearLayout.findViewById(R.id.R_sub_page);
        LinearLayout R_sub_btn = (LinearLayout) linearLayout.findViewById(R.id.R_sub_btn);

        for (int i = 0; i < btnWord.length; i++) {
            btnWord[i] = new Button(this.context);
            btnWord[i].setHeight(height);
            btnWord[i].setWidth(width);

            btnWord[i].setTag(i);
            btnWord[i].setOnClickListener(btnClicked);
            R_sub_btn.addView(btnWord[i]);
        }

        TextView page_num = (TextView) linearLayout.findViewById(R.id.page_num);
        page_num.setText(String.valueOf(pageCnt));
        R_sub_page.setBackground(new ColorDrawable(0xff6dc6d2));

        return linearLayout;
    }

    View.OnClickListener btnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            Intent intent;

            //튜토리얼 클릭
            if (pageCnt == 0 && tag.toString().equals("0")) {
                intent = new Intent(context, SingleTutorialActivity.class);
            } else {
                intent = new Intent(context, SingleGameActivity.class);
                //Log.i("map_info , pageCnt", map.get((int) tag) + "," + pageCnt);
            }
            intent.putExtra("map_info", map.get((int) tag));
            startActivity(intent);
            getActivity().finish();
            //Toast.makeText(context, "버튼" + tag + " : 클릭됨", Toast.LENGTH_SHORT).show();
        }
    };

    public void select() {

        Cursor cursr = db.query(tableName, null, null, null, null, null, null);

        cursr.moveToPosition(position);

        for (int i = 0; i < btnCnt; i++) {

            number = cursr.getInt(cursr.getColumnIndex("NUMBER"));

            map.add(cursr.getString(cursr.getColumnIndex("MAP_INFO")));

            cursr.moveToNext();
        }
    }

    public void copyDB() {
        AssetManager assetMgr = context.getAssets();
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

