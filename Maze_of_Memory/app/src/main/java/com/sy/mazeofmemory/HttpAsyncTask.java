package com.sy.mazeofmemory;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class HttpAsyncTask extends AsyncTask<Void, Void, String> {

    String _url;					/* 데이터를 가져올 웹서버의 주소 */
    String _parameter;				/* 서버에 넘길 파라미터 */
    ProgressDialog _dialog = null;

    // 생성자
    HttpAsyncTask(String url, String parameter) {
        super();
        this._url = url;
        this._parameter = parameter;
    }

    // 다이어로그를 출력하는 생성자
    HttpAsyncTask(String url, String parameter, Context context, String message) {
        this(url, parameter);
        _dialog = new ProgressDialog(context);
        _dialog.setMessage(message);
        _dialog.show();
    }

    // 서브클래스에서 반드시 구현하도록 추상메서드로 선언
    @Override
    protected abstract void onPostExecute(String result);

    // 웹페이지에 접속해 내용을 리턴하는 메서드
    @Override
    protected String doInBackground(Void... params) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String contents = null;

        try {
            URL url = new URL(_url);
            HttpURLConnection conn = (HttpURLConnection) url
                    .openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // URL에 파리미터 넘기기
            out = new OutputStreamWriter(
                    conn.getOutputStream(), "euc-kr");
            if(_parameter != null)
                out.write(_parameter);
            out.flush();

            // URL 결과 가져오기
            String buffer = null;
            contents = "";
            in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "euc-kr"));
            while ((buffer = in.readLine()) != null) {
                contents += buffer;
            }

        } catch (MalformedURLException e) {
            Log.e("HttpAsyncTask/MalFormURL", e.getMessage());
        } catch (IOException e) {
            Log.e("HttpAsyncTask/IOE", e.getMessage());
        } finally {
            try {
                if (out != null && in != null) {
                    out.close();
                    in.close();
                }
            } catch (IOException e) {
                Log.e("HttpAsyncTask", e.getMessage());
                out = null;
                in = null;
            }
        }

        if (_dialog != null && _dialog.isShowing())
            _dialog.dismiss();

        return contents;
    }
}
