package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class NicknameActivity extends Activity {

    private boolean isNicname;
    private String myResult;

    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        final EditText editText = (EditText) findViewById(R.id.nicname);

        Button btn;

        btn = (Button) findViewById(R.id.nic_btn);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                nickname = editText.getText().toString();
                //server요청
                request();


                //if (isNicname == true) {
                    /*
                    Intent intent = new Intent(NicknameActivity.this, SingleActivity.class);
                    startActivity(intent);

                    finish();
                    */
                //    }
            }
        });
    }

    public void request() {

        try {
            //한글이 안들어감
            HttpGet httpGet = new HttpGet("http://53.vs.woobi.co.kr/NicknameLoad.php?nickname=" + nickname);
            HttpClient httpClient = new DefaultHttpClient();
            //왜 안될까?
            HttpResponse response = httpClient.execute(httpGet);

            HttpEntity httpEntity = response.getEntity();
            String res = "";
            res = EntityUtils.toString(httpEntity);

            Log.i("res", res);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void response(HttpResponse response) {
        try {
            HttpEntity httpEntity = response.getEntity();
            String res = "";
            res = EntityUtils.toString(httpEntity);

            Log.i("res", res);

        } catch (Exception e) {


        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent(NicknameActivity.this, SingleActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}



