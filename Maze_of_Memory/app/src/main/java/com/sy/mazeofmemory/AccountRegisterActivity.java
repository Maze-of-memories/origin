package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import java.util.Arrays;
import java.util.List;

// 계정등록 액티비티
public class AccountRegisterActivity extends Activity implements ToggleButton.OnCheckedChangeListener {

    private static final String TAG = "AccountRegisterActivity";

    ToggleButton fbLoginButton;     // 페이스북 계정 on/off 버튼
    ToggleButton ggLoginButton;     // 구글 계정 on/off 버튼

    // 페이스북 세션 상태 변경 이벤트 리스너
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private UiLifecycleHelper uiHelper;

    // entry point
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 레이아웃 설정
        setContentView(R.layout.activity_account_register);

        // 페이스북 버튼 참조
        fbLoginButton = (ToggleButton)findViewById(R.id.fbLoginButton);
        fbLoginButton.setOnCheckedChangeListener(this);

        // 구글 버튼 참조
        ggLoginButton = (ToggleButton)findViewById(R.id.ggLoginButton);
        ggLoginButton.setOnCheckedChangeListener(this);

        // uiHelper 초기화
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        // 이미 세션이 열려있으면, callback 메서드를 직접 호출한다.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    // 토글버튼 클릭 callback 메서드
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // 페이스북 버튼
        if(buttonView.getId() == R.id.fbLoginButton) {
            if(isChecked) {
                // 어플 설치 후 최초 로그인 시도
                if(!Session.getActiveSession().isOpened() && !Session.getActiveSession().isClosed()) {
                    Log.i(TAG, "세션이 열리지도 않고 닫히지도 않음");
                    openActiveSession(this, true, Arrays.asList("user_friends"), callback);
                }
                else if(Session.getActiveSession().isClosed()) {
                    Log.i(TAG, "session is closed");
                    // open active session with 권한
                    openActiveSession(this, true, Arrays.asList("user_friends"), callback);
                }

                Log.i(TAG, "fbLoginButton is checked");
            } else {
                // 세션이 열려있는 경우에만 닫는다.
                if(Session.getActiveSession().isOpened()) {
                    Session.getActiveSession().close();
                }
                Log.i(TAG, "fbLoginButton is unchecked");
            }

        }


        // 구글 버튼
        else if(buttonView.getId() == R.id.ggLoginButton) {
            if(isChecked) {
                Log.i(TAG, "ggLoginButton is checked");
            } else {
                Log.i(TAG, "ggLoginButton is unchecked");
            }
        }
    }

    // 페이스북 세션 상태 변경 이벤트 콜백 메소드
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            // 버튼을 on으로 표시한다.
            fbLoginButton.setChecked(true);
            Log.i(TAG, "Facebook Logged in...");
        } else if (state.isClosed()) {
            // 버튼을 off로 표시한다.
            fbLoginButton.setChecked(false);
            Log.i(TAG, "Facebook Logged out...");
        }
    }

    // 권한을 설정한 후 페이스북 세션을 연다.
    private static Session openActiveSession(Activity activity, boolean allowLoginUI, List permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }
}
