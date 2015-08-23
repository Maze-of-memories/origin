package com.sy.mazeofmemory;


import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class SingleActivity extends FragmentActivity {

    int MAX_PAGE = 3;
    Fragment cur_fragment = new Fragment();

    AdView mAdView;

    private static final String dbName = "single.db";
    private static final String tableName = "SINGLE_MAP_5";
    public static final int dbVersion = 1;
    private SQLiteOpenHelper opener;
    private SQLiteDatabase db;
    private int Star_cnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new adapter(getSupportFragmentManager()));

        if (!isDBExists()) {
            copyDB();
        }
        opener = new MySQLiteOpenHelper(SingleActivity.this, dbName, null, dbVersion);
        db = opener.getReadableDatabase();
        select();
        db.close();


        mAdView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .addTestDevice("D69C8A1906CE1CC38958923B886C1F00")
                .build();

        mAdView.loadAd(adRequest);

        TextView textView = (TextView) findViewById(R.id.star_cnt);
        textView.setText("X " + Star_cnt);

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //advertise
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    private class adapter extends FragmentPagerAdapter {

        public adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position < 0 || MAX_PAGE <= position)
                return null;
            switch (position) {
                case 0:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
                case 1:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
                case 2:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
                default:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
            }
            return cur_fragment;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }

    //DB
    public void select() {

        Cursor cursr = db.query(tableName, null, null, null, null, null, null);

        cursr.moveToFirst();
            Star_cnt += cursr.getInt(cursr.getColumnIndex("STAR"));

        while(cursr.moveToNext()){
            Star_cnt += cursr.getInt(cursr.getColumnIndex("STAR"));
        }

        Log.i("Star_cnt", "" + Star_cnt);

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
