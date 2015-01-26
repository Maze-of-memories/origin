package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jun on 2015-01-18.
 * 도움말 리스트뷰의 어댑터
 */
public class HelpListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<String> arrayGroup;
    private HashMap<String, ArrayList<String>> arrayChild;

    public HelpListAdapter(Context context, ArrayList<String> arrayGroup, HashMap<String, ArrayList<String>> arrayChild) {
        super();

        this.context = context;
        this.arrayGroup = arrayGroup;
        this.arrayChild = arrayChild;
    }

    @Override
    public int getGroupCount() {
        return arrayGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return arrayChild.get(arrayGroup.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return arrayGroup.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return arrayChild.get(arrayGroup.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupName = arrayGroup.get(groupPosition);
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (LinearLayout) inflater.inflate(R.layout.activity_help_listview_group_item, null);
        }

        TextView textGroup = (TextView) v.findViewById(R.id.textGroup);
        textGroup.setText(groupName);

        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String childName = arrayChild.get(arrayGroup.get(groupPosition)).get(childPosition);
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (LinearLayout) inflater.inflate(R.layout.activity_help_listview_child_item, null);
        }

        TextView textChild = (TextView) v.findViewById(R.id.textChild);
        textChild.setText(childName);

        ToggleButton facebookButton = (ToggleButton) v.findViewById(R.id.facebookButton);

        // 페이스북 세션 상태에 따라 on, off 여부를 결정한다.
        if (Session.getActiveSession().isOpened())
            facebookButton.setChecked(true);
        else
            facebookButton.setChecked(false);

        // 버튼이 on으로 변하면 facebook으로 로그인하고 off로 변하면 로그아웃 한다.
        facebookButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // start Facebook Login
                    Session.openActiveSession((Activity) context, true, new Session.StatusCallback() {

                        // callback when session changes state
                        @Override
                        public void call(Session session, SessionState state, Exception exception) {
                            if (session.isOpened()) {

                                // make request to the /me API
                                Request.newMeRequest(session, new Request.GraphUserCallback() {
                                    @Override
                                    public void onCompleted(GraphUser graphUser, Response response) {
                                        ;
                                    }
                                });
                            }
                        }
                    });
                } else {
                    // start Facebook logout
                    Session session = Session.getActiveSession();
                    if (session != null) {

                        if (!session.isClosed()) {
                            session.closeAndClearTokenInformation();
                        }
                    } else {
                        session = new Session(context);
                        Session.setActiveSession(session);
                        session.closeAndClearTokenInformation();
                    }
                }
            }
        });

        // 구글 버튼 이벤트 설정 필요
        ToggleButton googleButton = (ToggleButton) v.findViewById(R.id.googleButton);
        googleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 로그인 코드
                } else {
                    // 로그아웃 코드
                }
            }
        });


        // 보이기 여부 설정
        if (groupPosition == 4 && childPosition == 0) {
            facebookButton.setVisibility(View.VISIBLE);
            googleButton.setVisibility(View.GONE);
        } else if (groupPosition == 4 && childPosition == 1) {
            facebookButton.setVisibility(View.GONE);
            googleButton.setVisibility(View.VISIBLE);
        } else {
            facebookButton.setVisibility(View.GONE);
            googleButton.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
