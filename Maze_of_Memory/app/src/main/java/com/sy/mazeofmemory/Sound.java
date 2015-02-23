package com.sy.mazeofmemory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class Sound extends Service {

    SoundPool soundPool;
    int sound;
    MediaPlayer mediaPlayer;

    //버튼음
    public void initBtnSound(Context context) {
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sound = soundPool.load(context, R.raw.sms_iphone_sms, 1);
    }

    public void playBtnSound() {
        soundPool.play(sound, 1, 1, 0, 0, 1);
    }

    //배경음
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        mediaPlayer = MediaPlayer.create(this, R.raw.sms_iphone_sms);
        mediaPlayer.setLooping(true);
    }

    public void onStart(Intent intent, int startId){
        super.onStart(intent, startId);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mediaPlayer.start();
                return null;
            }
        }.execute();
    }

    public void onDestroy(){
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
