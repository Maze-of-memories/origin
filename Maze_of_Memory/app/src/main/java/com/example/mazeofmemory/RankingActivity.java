package com.example.mazeofmemory;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class RankingActivity extends FragmentActivity implements RankingFragment.OnFragmentInteractionListener {

    private RankingFragment rankingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 프래그먼트를 부착한다.
        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            rankingFragment = new RankingFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, rankingFragment)
                    .commit();
        } else {
            // Or set the fragment from restored state info
            rankingFragment = (RankingFragment) getSupportFragmentManager()
                    .findFragmentById(android.R.id.content);
        }
    }

    // onFragmentInteractoin 메서드를 반드시 구현해주어야 한다.
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
