package com.example.mazeofmemory;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

public class SingleTutorialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tutorial);

        DisplayMetrics metrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        float xdpi = metrics.xdpi;
        float ydpi = metrics.ydpi;

        Log.i("normal", "( " + screenWidth + ", " + screenHeight + " )");
        Log.i("normal", "DPI : " + metrics.xdpi + ", " + metrics.ydpi);


    }
}
