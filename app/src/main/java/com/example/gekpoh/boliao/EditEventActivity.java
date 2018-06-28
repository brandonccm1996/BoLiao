package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditEventActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Button buttonSubmit;

    private EditEventFragment1 fragment1;
    private EditEventFragment2 fragment2;
    private EditEventFragment3 fragment3;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupsDatabaseReference;
    private DatabaseReference mChatsDatabaseReference;
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mJoinedListsReference;
    private String chatId;
    private LatLng mLatLng;

    private EditEventFragment1 editEventFragment1;
    private EditEventFragment2 editEventFragment2;
    private EditEventFragment3 editEventFragment3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.common_google_signin_btn_icon_dark);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        Bundle extras = getIntent().getExtras();
        String groupId = extras.getString("groupId");
        editEventFragment1 = new EditEventFragment1();
        editEventFragment1.setArguments(extras);
        editEventFragment2 = new EditEventFragment2();
        editEventFragment2.setArguments(extras);
        editEventFragment3 = new EditEventFragment3();
        editEventFragment3.setArguments(extras);

        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        mGroupsDatabaseReference = mFirebaseDatabase.getReference().child("groups");
        mChatsDatabaseReference = mFirebaseDatabase.getReference().child("chats");
        mUserListsDatabaseReference = mFirebaseDatabase.getReference().child("userlists");
        mJoinedListsReference = mFirebaseDatabase.getReference().child("joinedlists");
        buttonSubmit = findViewById(R.id.buttonSubmit);

        mViewPager = findViewById(R.id.view_pager_create_new_event);
        mViewPager.setOffscreenPageLimit(3);
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
            if (position == 0) return new EditEventFragment1();
            else if (position == 1) return new EditEventFragment2();
            else if (position == 2) return new EditEventFragment3();
            else return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) return "Step 1";
            else if (position == 1) return "Step 2";
            else if (position == 2) return "Step 3";
            else return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            if (position == 0) fragment1 = (EditEventFragment1) createdFragment;
            else if (position == 1) fragment2 = (EditEventFragment2) createdFragment;
            else if (position == 2) fragment3 = (EditEventFragment3) createdFragment;
            return createdFragment;
        }
    }

    public void setLatLng(LatLng newLatLng){
        mLatLng = newLatLng;
    }
}