package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class GroupDetailsActivity extends AppCompatActivity {
    public static int NUM_PAGES = 3;
    private Group mGroup;//The group to refer to when we want to access required information
    private ChatFragment chatFragment;
    private LocationFragment locationFragment;
    private EventInfoFragment eventInfoFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_details_activity_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mGroup = getIntent().getParcelableExtra(getResources().getString(R.string.groupDetails));//Need to pass on group details before starting this activity
        chatFragment = new ChatFragment();
        locationFragment = new LocationFragment();
        eventInfoFragment = new EventInfoFragment();
        ViewPager detailsPager = findViewById(R.id.groupDetailsPager);
        detailsPager.setAdapter(new GroupDetailsPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.detailsTabLayout);
        tabLayout.setupWithViewPager(detailsPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private class GroupDetailsPagerAdapter extends FragmentStatePagerAdapter {
        public GroupDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0: return eventInfoFragment;
                case 1: return locationFragment;
                case 2: return chatFragment;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0: return getResources().getString(R.string.GroupDetailsActivityTab1);
                case 1: return getResources().getString(R.string.GroupDetailsActivityTab2);
                case 2: return getResources().getString(R.string.GroupDetailsActivityTab3);
                default: return null;
            }
        }
    }
}
