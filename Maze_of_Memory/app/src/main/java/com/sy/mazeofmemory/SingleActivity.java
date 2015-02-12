package com.sy.mazeofmemory;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class SingleActivity extends FragmentActivity {

    int MAX_PAGE = 3;
    Fragment cur_fragment = new Fragment();

    AdView mAdView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new adapter(getSupportFragmentManager()));


        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();

        mAdView.loadAd(adRequest);

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //광고


    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////

    private class adapter extends FragmentPagerAdapter {

        public adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position < 0 || MAX_PAGE <= position)
                return null;
            switch (position) {
                case 0:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
                case 1:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
                case 2:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
                default:
                    cur_fragment = new SingleSubPage(getApplicationContext(), position);
                    break;
            }
            return cur_fragment;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////


}
