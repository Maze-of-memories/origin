package com.sy.mazeofmemory;

import android.app.Activity;
import android.widget.Toast;

public class BackPressCloseHandler extends Activity {
    private long backKeyPressedTime = 0;

    private Toast toast;

    private Activity activity;

    public BackPressCloseHandler(MainActivity context) {

        this.activity = context;

    }

    public void onBackPressed() {

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {

            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {

            activity.finish();
            toast.cancel();
        }

    }

    private void showGuide() {

        toast = Toast.makeText(activity, "뒤로 버튼을 한번 더 터치하시면 종료됩니다.",
                Toast.LENGTH_SHORT);

        toast.show();

    }

}
