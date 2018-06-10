package com.example.gekpoh.boliao;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CreateNewEventActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 2; // will change to 3 after including map
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        mViewPager = findViewById(R.id.view_pager_create_new_event);
        CreateNewEventAdapter adapter = new CreateNewEventAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout = findViewById(R.id.tab_layout_create_new_event);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private class CreateNewEventAdapter extends FragmentStatePagerAdapter {
        public CreateNewEventAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem (int position) {
            if (position == 0) return new CreateNewEventFragment1();
            else if (position == 1) return new CreateNewEventFragment2();
            else return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) return "Step 1: Main Details";
            else if (position == 1) return "Step 2: Description";
            else return null;
        }
    }
}
