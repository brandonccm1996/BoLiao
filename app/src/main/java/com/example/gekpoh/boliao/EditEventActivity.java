package com.example.gekpoh.boliao;

import android.support.annotation.NonNull;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity implements EditEventFragment3.fragment3CallBack{

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private Button buttonSubmit;

    private EditEventFragment1 fragment1;
    private EditEventFragment2 fragment2;
    private EditEventFragment3 fragment3;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mEditEventNotifDatabaseReference;
    private DatabaseReference mUserListsDatabaseReference;
    private LatLng mLatLng;

    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.common_google_signin_btn_icon_dark);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        extras = getIntent().getExtras().getBundle("intentBundle");
        fragment1 = new EditEventFragment1();
        fragment1.setArguments(extras);
        fragment2 = new EditEventFragment2();
        fragment2.setArguments(extras);
        fragment3 = new EditEventFragment3();
        fragment3.setArguments(extras);

        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(extras.getString("groupId"));
        mEditEventNotifDatabaseReference = mFirebaseDatabase.getReference().child("editEventNotif").child(extras.getString("groupId"));
        mUserListsDatabaseReference = mFirebaseDatabase.getReference().child("userlists").child(extras.getString("groupId"));
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
                    Toast.makeText(EditEventActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                else if (Integer.parseInt(fragment2.sendNumPeople()) < extras.getInt("eventcurrentsize")) {
                    Toast.makeText(EditEventActivity.this, "You have too many participants. Please remove some participants before reducing the max number of participants.", Toast.LENGTH_LONG).show();
                }
                else {
                    Map mapToUpload = new HashMap();
                    long startTimeStamp, endTimeStamp;
                    String startDateTime = fragment1.sendSDate() + " " + fragment1.sendSTime();
                    String endDateTime = fragment1.sendEDate() + " " + fragment1.sendETime();
                    try {
                        startTimeStamp = Group.groupDateFormatter.parse(startDateTime).getTime();
                        endTimeStamp = Group.groupDateFormatter.parse(endDateTime).getTime();
                    }catch(ParseException e){
                        Toast.makeText(EditEventActivity.this, "Failed to create activity due to parsing error", Toast.LENGTH_SHORT).show();
                        return;
                    }
//                    //Create group in database with relevant information
//                    mapToUpload.put("names", fragment1.sendName());
//                    mapToUpload.put("location", fragment1.sendLocation());
//                    mapToUpload.put("startDateTime", startTimeStamp);
//                    mapToUpload.put("endDateTime", endTimeStamp);
//                    mapToUpload.put("maxParticipants", Integer.parseInt(fragment2.sendNumPeople()));
//                    mapToUpload.put("description", fragment2.sendDescription());
//                    mapToUpload.put("placeId", fragment3.sendPlaceId());
//                    mapToUpload.put("numParticipants", 1);
//                    mapToUpload.put("chatId", extras.getString("groupId"));
//                    mapToUpload.put("organizerId", MainActivity.userUid);
//
//                    if (fragment2.sendPhotoUri() != null) mapToUpload.put("photoUrl", fragment2.sendPhotoUri());
//                    mGroupsDatabaseReference.child(extras.getString("groupId")).setValue(mapToUpload);

                    mGroupDatabaseReference.child("names").setValue(fragment1.sendName());
                    mGroupDatabaseReference.child("location").setValue(fragment1.sendLocation());
                    mGroupDatabaseReference.child("startDateTime").setValue(startTimeStamp);
                    mGroupDatabaseReference.child("endDateTime").setValue(endTimeStamp);
                    mGroupDatabaseReference.child("maxParticipants").setValue(Integer.parseInt(fragment2.sendNumPeople()));
                    mGroupDatabaseReference.child("description").setValue(fragment2.sendDescription());
                    mGroupDatabaseReference.child("placeId").setValue(fragment3.sendPlaceId());
                    mGroupDatabaseReference.child("photoUrl").setValue(fragment2.sendPhotoUri());

                    //DatabaseReference ref = mFirebaseDatabase.getReference().child("geoFireObjects");
                    GeoFire geoFire = FirebaseDatabaseUtils.getGeoFireInstance();
                    geoFire.setLocation(extras.getString("groupId"), new GeoLocation(mLatLng.latitude, mLatLng.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if(error != null){
                                Toast.makeText(EditEventActivity.this, "Error in setting location in geofire", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    // create notification object
                    final String notifId = mEditEventNotifDatabaseReference.push().getKey();

                    mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map userList = new HashMap();
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                // don't send notification to the person editing event
                                if (!childSnapshot.getKey().equals(MainActivity.userUid)) userList.put(childSnapshot.getKey(), true);
                            }
                            mEditEventNotifDatabaseReference.child(notifId).setValue(userList);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

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
            if (position == 0) return fragment1;
            else if (position == 1) return fragment2;
            else if (position == 2) return fragment3;
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
