package com.sy.mazeofmemory;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RankingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class RankingFragment extends Fragment {

    private static final String TAG = "RankingFragment";

    private OnFragmentInteractionListener mListener;
    private LinearLayout onLoginLayout;
    private LinearLayout onLogoutLayout;
    private ListView rankListView;
    ArrayList<RankingItem> items;
    RankingListAdapter adapter;

    // 세션 변경 이벤트 리스너
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private UiLifecycleHelper uiHelper;

    public RankingFragment() {
        // Required empty public constructor
    }

    // 세션 상태 변경시 호출되는 메서드
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            // 계정 인증 후의 화면을 보여준다.
            onLoginLayout.setVisibility(LinearLayout.VISIBLE);
            onLogoutLayout.setVisibility(LinearLayout.GONE);

            // 리스트뷰 초기화
            rankListView = (ListView)getView().findViewById(R.id.rankListView);
            rankListView.setEmptyView(getView().findViewById(R.id.empty));
            items = new ArrayList();

            // 더미 데이터 추가
            /*
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));
            items.add(new RankingItem("http://www.google.co.kr", "me", 5, 100));
            items.add(new RankingItem("http://www.google.co.kr", "you", 10, 3200));*/
            adapter = new RankingListAdapter(getActivity(), R.layout.activity_ranking_listview_item, items);
            rankListView.setAdapter(adapter);

            getRankingList(session);

            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            // 계정이 인증되지 않았을 때의 화면을 보여준다.
            onLoginLayout.setVisibility(LinearLayout.GONE);
            onLogoutLayout.setVisibility(LinearLayout.VISIBLE);
            Log.i(TAG, "Logged out...");
        }
    }

    // 친구 랭킹 리스트를 가져온다.
    private void getRankingList(Session session) {
        Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {

            @Override
            public void onCompleted(List<GraphUser> graphUsers, Response response) {
                items.clear();

                List<GraphUser> friends = graphUsers;
                if(!friends.isEmpty()) {
                    for(GraphUser friend : friends) {
                        items.add(new RankingItem("https://graph.facebook.com/" + friend.getId() + "/picture", friend.getName(), 0, 0));
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }).executeAsync();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // uiHelper 초기화
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 프래그먼트의 레이아웃을 생성하고 리턴한다.
        View view = inflater.inflate(R.layout.activity_ranking, container, false);

        // 로그아웃 했을 때 보여지는 레이아웃
        onLogoutLayout = (LinearLayout)view.findViewById(R.id.onLogoutLayout);

        // 로그인 했을 때 보여지는 레이아웃
        onLoginLayout = (LinearLayout)view.findViewById(R.id.onLoginLayout);

        // 랭킹 리스트 뷰
        rankListView = (ListView)view.findViewById(R.id.rankListView);


        // 페이스북 로그인 버튼
        LoginButton authButton = (LoginButton)view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_friends"));

        return view;
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
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
