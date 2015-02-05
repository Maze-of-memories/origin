package com.sy.mazeofmemory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class NicknameActivity extends Activity {

    private boolean isNicname;
    private String myResult;

    private String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        final EditText editText = (EditText) findViewById(R.id.nicname);

        Button btn = (Button) findViewById(R.id.nic_btn);
        btn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                nickname = editText.getText().toString();

                //server요청
                regNickname();

            }
        });
    }
    public void regNickname(){
        nicknameExist();
    }

    public void nicknameExist() {
        new AsyncTask<Void, Void, Void>() {

            ProgressDialog dialog;
            @Override
            protected void onPreExecute() {
                // 작업을 시작하기 전 할일
                dialog = new ProgressDialog(NicknameActivity.this);
                dialog.setMessage("Wait...");
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.show();

                super.onPreExecute();
            }

            boolean exist;

            @Override
            protected Void doInBackground(Void... params) {
                StringBuffer sb = new StringBuffer();

                try{

                    //공백 예외
                    URL url = new URL("http://53.vs.woobi.co.kr/MOM/Nickname.php?nickname=" + nickname);

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream()));

                    String str = null;
                    while ((str = reader.readLine()) != null) {
                        sb.append(str);

                    }
                    Log.i("sb.toString", sb.toString());
                    if (sb.toString().equals("success")) {
                        exist = true;
                    } else if (sb.toString().equals("fault")) {
                        exist = false;
                    }

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                //작업 후 보여질 화면
                //super.onPostExecute(aVoid);
                dialog.dismiss();

                if(exist){
                    Intent intent = new Intent(NicknameActivity.this, SingleActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(NicknameActivity.this, "Exist!!! The Nickname.", Toast.LENGTH_LONG).show();
                }

            }
        }.execute();

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



