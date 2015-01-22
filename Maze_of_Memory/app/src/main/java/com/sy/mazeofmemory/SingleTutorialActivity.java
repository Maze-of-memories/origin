package com.sy.mazeofmemory;

import android.app.Activity;
import android.os.Bundle;


public class SingleTutorialActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tutorial);




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

}
