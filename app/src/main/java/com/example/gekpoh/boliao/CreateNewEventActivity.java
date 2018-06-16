package com.example.gekpoh.boliao;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateNewEventActivity extends AppCompatActivity{

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Button buttonSubmit;

    private CreateNewEventFragment1 fragment1;
    private CreateNewEventFragment2 fragment2;
    private CreateNewEventFragment3 fragment3;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupsDatabaseReference;
    private DatabaseReference mChatsDatabaseReference;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.common_google_signin_btn_icon_dark);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupsDatabaseReference = mFirebaseDatabase.getReference().child("groups");
        mChatsDatabaseReference = mFirebaseDatabase.getReference().child("chats");

        buttonSubmit = findViewById(R.id.buttonSubmit);

        mViewPager = findViewById(R.id.view_pager_create_new_event);
        mViewPager.setOffscreenPageLimit(3);
        CreateNewEventAdapter adapter = new CreateNewEventAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabLayout = findViewById(R.id.tab_layout_create_new_event);
        mTabLayout.setupWithViewPager(mViewPager);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragment1.sendName().equals("") || fragment1.sendLocation().equals("") || fragment1.sendSDate().equals("") ||
                        fragment1.sendSTime().equals("") || fragment1.sendEDate().equals("") || fragment1.sendETime().equals("") ||
                        fragment2.sendDescription().equals("") || fragment2.sendNumPeople().equals("") || fragment3.sendPlaceId() == null)
                    Toast.makeText(CreateNewEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                else {
                    Map mapToUpload = new HashMap();
                    Map mapToUpload2 = new HashMap();
                    Map mapToUpload3 = new HashMap();

                    chatId = mChatsDatabaseReference.push().getKey();
                    mChatsDatabaseReference.child(chatId).setValue(new ChatMessage("Welcome to activity chat", "",0)); // just some dummy values

                    mapToUpload3.put("isOrganiser", true);
                    mapToUpload2.put(MainActivity.userUid, mapToUpload3);
                    mapToUpload.put("users", mapToUpload2);
                    mapToUpload.put("names", fragment1.sendName());
                    mapToUpload.put("location", fragment1.sendLocation());
                    mapToUpload.put("startDateTime", fragment1.sendSDate() + " " + fragment1.sendSTime());
                    mapToUpload.put("endDateTime", fragment1.sendEDate() + " " + fragment1.sendETime());
                    mapToUpload.put("maxParticipants", Integer.parseInt(fragment2.sendNumPeople()));
                    mapToUpload.put("description", fragment2.sendDescription());
                    mapToUpload.put("placeId", fragment3.sendPlaceId());
                    mapToUpload.put("numParticipants", 0);
                    mapToUpload.put("chatId", chatId);
                    if (fragment2.sendPhotoUri() == null) mapToUpload.put("photoUrl", "");
                    else mapToUpload.put("photoUrl", fragment2.sendPhotoUri());

                    mGroupsDatabaseReference.push().setValue(mapToUpload);
                    finish();
                }
            }
        });
    }



    private class CreateNewEventAdapter extends FragmentStatePagerAdapter {
        public CreateNewEventAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem (int position) {
            if (position == 0) return new CreateNewEventFragment1();
            else if (position == 1) return new CreateNewEventFragment2();
            else if (position == 2) return new CreateNewEventFragment3();
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
            if (position == 0) fragment1 = (CreateNewEventFragment1) createdFragment;
            else if (position == 1) fragment2 = (CreateNewEventFragment2) createdFragment;
            else if (position == 2) fragment3 = (CreateNewEventFragment3) createdFragment;
            return createdFragment;
        }
    }
}
