package com.sy.mazeofmemory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import java.util.ArrayList;
import java.util.HashMap;


public class HelpActivity extends ActionBarActivity {

    ExpandableListView helpListView;
    private ArrayList<String> arrayGroup;
    private HashMap<String, ArrayList<String>> arrayChild;

    // facebook 관련 멤버
    private UiLifecycleHelper uiHelper;

    // 세션 변경 이벤트 리스너
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    // 세션 상태 변경시 호출되는 메서드
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 레이아웃 설정
        setContentView(R.layout.activity_help);

        // 그룹 배열 초기화
        arrayGroup = new ArrayList<>();

        // 차일드 해시맵 초기화
        arrayChild = new HashMap<>();

        setArrayData();

        // 리스트뷰 참조 및 초기화
        helpListView = (ExpandableListView)findViewById(R.id.helpListView);
        helpListView.setAdapter(new HelpListAdapter(this, arrayGroup, arrayChild));
        helpListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    String menu = arrayChild.get(arrayGroup.get(groupPosition)).get(childPosition);
                    Toast.makeText(HelpActivity.this, menu, Toast.LENGTH_SHORT).show();
                    return false;
                }
        });

        helpListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                // 차일드가 없는 그룹에 대해서만 이벤트를 준다.
               if(arrayChild.get(arrayGroup.get(groupPosition)).isEmpty()) {
                   String menu = arrayGroup.get(groupPosition);
                   Toast.makeText(HelpActivity.this, menu, Toast.LENGTH_SHORT).show();
               }
                return false;
            }
        });

        // uiHelper 초기화
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
    }

    private void setArrayData() {
        arrayGroup.add("게임 조작법");
        arrayGroup.add("공지사항");
        arrayGroup.add("TIP");
        arrayGroup.add("FAQ");
        arrayGroup.add("계정 등록");

        ArrayList<String> arrayGameControl = new ArrayList<>();
        arrayGameControl.add("싱글 플레이");
        arrayGameControl.add("멀티 플레이");

        ArrayList<String> arrayAccount = new ArrayList<>();
        arrayAccount.add("Facebook");
        arrayAccount.add("Google+");

        // 각 그룹에 차일드를 붙힌다. 차일드가 없으면 빈 리스트를 붙힌다.
        arrayChild.put(arrayGroup.get(0), arrayGameControl);
        arrayChild.put(arrayGroup.get(1), new ArrayList<String>());
        arrayChild.put(arrayGroup.get(2), new ArrayList<String>());
        arrayChild.put(arrayGroup.get(3), new ArrayList<String>());
        arrayChild.put(arrayGroup.get(4), arrayAccount);
    }

    @Override
    protected void onResume() {
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
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
