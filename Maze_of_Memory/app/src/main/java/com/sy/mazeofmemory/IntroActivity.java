package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class IntroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(IntroActivity.this, NicnameActivity.class);
                startActivity(intent);
                //뒤로 가기 했을경우 안나오도록 없애주기
                finish();
            }
        }, 3000);

    }


}
