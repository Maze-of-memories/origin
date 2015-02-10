package com.sy.mazeofmemory;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;


public class SingleActivity extends FragmentActivity {

    int MAX_PAGE = 3;
    Fragment cur_fragment = new Fragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new adapter(getSupportFragmentManager()));

    }

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



}
