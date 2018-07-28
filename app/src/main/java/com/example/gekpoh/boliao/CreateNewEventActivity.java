package com.example.gekpoh.boliao;

import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class CreateNewEventActivity extends AppCompatActivity implements CreateNewEventFragment3.fragment3CallBack{

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
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mJoinedListsReference;
    private String chatId;
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logowhitesmall);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toasty.error(CreateNewEventActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (fragment1.sendName().equals("") || fragment1.sendLocation().equals("") || fragment1.sendSDate().equals("") ||
                            fragment1.sendSTime().equals("") || fragment1.sendEDate().equals("") || fragment1.sendETime().equals("") ||
                            fragment2.sendDescription().equals("") || fragment2.sendNumPeople().equals("") || fragment3.sendPlaceId() == null) {
                        Toasty.error(CreateNewEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    }
                    else if (Integer.parseInt(fragment2.sendNumPeople()) < 1) {
                        Toasty.error(CreateNewEventActivity.this, "Number of participants must be at least 1", Toast.LENGTH_SHORT).show();
                    }
                    else if (fragment1.sendName().contains(".") || fragment1.sendName().contains("#") || fragment1.sendName().contains("$") ||
                            fragment1.sendName().contains("[") || fragment1.sendName().contains("]") || fragment1.sendName().length() > 15) {
                        Toasty.error(CreateNewEventActivity.this, "Activity name must not contain the characters: '.', '#', '$', '[', or ']' and must not have more than 15 characters", Toast.LENGTH_LONG).show();
                    }
                    else if (fragment1.sendLocation().length() > 15) {
                        Toasty.error(CreateNewEventActivity.this, "Activity location must not have more than 15 characters", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        chatId = mChatsDatabaseReference.push().getKey();

                        Map mapToUpload = new HashMap();
                        Map mapToUpload2 = new HashMap();
                        Map mapToUpload3 = new HashMap();
                        long startTimeStamp, endTimeStamp;
                        String startDateTime = fragment1.sendSDate() + " " + fragment1.sendSTime();
                        String endDateTime = fragment1.sendEDate() + " " + fragment1.sendETime();
                        try {
                            startTimeStamp = Group.groupDateFormatter2.parse(startDateTime).getTime();
                            endTimeStamp = Group.groupDateFormatter2.parse(endDateTime).getTime();
                        } catch (ParseException e) {
                            Toasty.error(CreateNewEventActivity.this, "Failed to create activity due to parsing error", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(mLatLng == null){
                            Toasty.error(CreateNewEventActivity.this, "Failed to create activity as LatLng is not set", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //Create group in database with relevant information
                        mapToUpload.put("names", fragment1.sendName());
                        mapToUpload.put("location", fragment1.sendLocation());
                        mapToUpload.put("startDateTime", startTimeStamp);
                        mapToUpload.put("endDateTime", endTimeStamp);
                        mapToUpload.put("maxParticipants", Integer.parseInt(fragment2.sendNumPeople()));
                        mapToUpload.put("description", fragment2.sendDescription());
                        mapToUpload.put("placeId", fragment3.sendPlaceId());
                        mapToUpload.put("numParticipants", 1);
                        mapToUpload.put("chatId", chatId);
                        mapToUpload.put("organizerId", MainActivity.userUid);
                        mapToUpload.put("photoUrl", fragment2.sendPhotoUri());
                        mGroupsDatabaseReference.child(chatId).setValue(mapToUpload);

                        //Create lists of users for this group
                        mapToUpload3.put("isAdmin", true);
                        mapToUpload3.put("isOrganizer", true);
                        mapToUpload2.put(MainActivity.userUid, mapToUpload3);
                        mUserListsDatabaseReference.child(chatId).setValue(mapToUpload2);

                        //Create chat in database
                        //String key = mChatsDatabaseReference.child(chatId).push().getKey();
                        //mChatsDatabaseReference.child(chatId).child(key).setValue(new ChatMessage("Welcome to activity chat", "",null,0)); // just some dummy values

                        //add this group to list of joinedgroups for this user
                        mJoinedListsReference.child(MainActivity.userUid).child(chatId).setValue("true");
                        //DatabaseReference ref = mFirebaseDatabase.getReference().child("geoFireObjects");
                        GeoFire geoFire = FirebaseDatabaseUtils.getGeoFireInstance();

                        geoFire.setLocation(chatId, new GeoLocation(mLatLng.latitude, mLatLng.longitude), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error != null) {
                                    Toasty.error(CreateNewEventActivity.this, "Error in setting location in geofire", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        TimeNotificationScheduler.setNewReminder(CreateNewEventActivity.this, chatId, fragment1.sendName(), startTimeStamp, -1);

                        Toasty.success(getApplicationContext(), "Activity created", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

    public void setLatLng(LatLng newLatLng){
        mLatLng = newLatLng;
    }
}
