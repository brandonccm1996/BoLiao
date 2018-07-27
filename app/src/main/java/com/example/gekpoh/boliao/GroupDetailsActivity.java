package com.example.gekpoh.boliao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, EventInfoFragment.eventInfoCallBack, MembersFragment.reloadDetailsInterface {
    private static boolean instanceCreated = false;
    private boolean inEvent;
    private static final String TAG = "GroupDetailsActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static int JOIN_NUM_PAGES = 4;
    private static int NOT_JOIN_NUM_PAGES = 3;
    private static final int DEFAULT_ZOOM = 15;
    private boolean locationPermissionGranted = false;
    private String groupId;
    private Group mGroup;//The group to refer to when we want to access required information
    private GroupUsersInformation mGroupUsersInformation;
    private FirebaseDatabase mFirebaseDatabase;
    private ChatFragment chatFragment;
    private EventInfoFragment eventInfoFragment;
    private SupportMapFragment mapFragment;
    private MembersFragment membersFragment;
    private GoogleMap googleMap;
    private Place place;
    //Client to get data from placeid
    private GeoDataClient mGeoDataClient;
    private DatabaseReference ref;
    private ValueEventListener listener;
    private ViewPager detailsPager;

    // Client to get current Location
    //private FusedLocationProviderClient mFusedLocationProviderClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instanceCreated = true;
        setContentView(R.layout.group_details_activity_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        groupId = getIntent().getStringExtra(getString(R.string.groupKey));
        inEvent = getIntent().getBooleanExtra(getString(R.string.InActivityKey), false);
        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        //reloadGroupDetails();

        mGroupUsersInformation = new GroupUsersInformation(groupId);

        if (inEvent) {
            Bundle args = new Bundle();
            args.putString(getString(R.string.groupIdKey), groupId);
            chatFragment = new ChatFragment();
            chatFragment.setArguments(args);
        }


        //mGroup = getIntent().getParcelableExtra(getString(R.string.groupKey));//Need to pass on group details before starting this activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadGroupDetails();
    }

    private void checkRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            locationPermissionGranted = true;
        }
    }

    private void updateMapUI() {
        googleMap.clear();
        mGeoDataClient.getPlaceById(mGroup.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    place = places.get(0);
                    googleMap.addMarker(new MarkerOptions().position(place.getLatLng())
                            .title(place.getName().toString())
                            .snippet(place.getAddress().toString()));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));
                    places.release();
                } else {
                    //Toast.makeText(GroupDetailsActivity.this, "Place not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (place == null) {
            Log.v(TAG, "place not in");
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //checkRequestLocationPermission();
        updateMapUI();
    }

    @Override
    public void onJoinLeaveClick() {
        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
            Toasty.error(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        final String id = mGroup.getChatId();
        final DatabaseReference joinedlistref = mFirebaseDatabase.getReference().child("joinedlists").child(MainActivity.userUid).child(id);
        final DatabaseReference userlistref = mFirebaseDatabase.getReference().child("userlists").child(id).child(MainActivity.userUid);
        final DatabaseReference numParticipantsRef = mFirebaseDatabase.getReference().child("groups").child(id).child("numParticipants");
        if (inEvent) {
            if (mGroup.getOrganizerId().equals(MainActivity.userUid)) {
                Toasty.error(this, "Organizer can't quit.", Toast.LENGTH_SHORT).show();
                return;
            }

            joinedlistref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        //update userlist and join list
                        joinedlistref.removeValue();
                        //update num of participants
                        numParticipantsRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                long numParticipants = (long) mutableData.getValue();
                                numParticipants = numParticipants - 1;
                                Log.v(TAG, "DOING TRANSACTION" + numParticipants);
                                mutableData.setValue(numParticipants);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                if (databaseError != null) {
                                    return;
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            userlistref.removeValue();
            TimeNotificationScheduler.cancelReminder(this, mGroup.getChatId());

            finish();
        } else {
            numParticipantsRef.runTransaction(new numParticipantJoinTransactionHandler(id, userlistref, joinedlistref));
        }
    }


    @Override
    protected void onDestroy() {
        instanceCreated = false;
        super.onDestroy();
    }

    public static boolean isInstanceCreated() {
        return instanceCreated;
    }

    private class GroupDetailsPagerAdapter extends FragmentStatePagerAdapter {
        public GroupDetailsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return eventInfoFragment;
                case 1:
                    return mapFragment;
                case 2:
                    return membersFragment;
                case 3:
                    return chatFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return inEvent ? JOIN_NUM_PAGES : NOT_JOIN_NUM_PAGES;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.GroupDetailsActivityTab1Name);
                case 1:
                    return getString(R.string.GroupDetailsActivityTab2Name);
                case 2:
                    return getString(R.string.GroupDetailsActivityTab3Name);
                case 3:
                    return getString(R.string.GroupDetailsActivityTab4Name);
                default:
                    return null;
            }
        }
    }

    private class numParticipantJoinTransactionHandler implements Transaction.Handler {
        private boolean allowJoin = false;
        private DatabaseReference userlistref, joinedlistref;
        private String id;

        private numParticipantJoinTransactionHandler(String id, DatabaseReference userlistref, DatabaseReference joinedlistref) {
            this.id = id;
            this.userlistref = userlistref;
            this.joinedlistref = joinedlistref;
        }

        @NonNull
        @Override
        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
            long numParticipants = (long) mutableData.getValue();
            if (numParticipants >= mGroup.getMaxParticipants()) {
                allowJoin = false;
            } else {
                allowJoin = true;
                numParticipants = numParticipants + 1;
                mutableData.setValue(numParticipants);
            }
            Log.v(TAG, "DOING TRANSACTION" + numParticipants);
            return Transaction.success(mutableData);
        }

        @Override
        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
            if (databaseError != null) {
                reloadGroupDetails();
                return;
            }
            if (!allowJoin) {
                Toasty.error(GroupDetailsActivity.this, "Sorry, group is already full", Toast.LENGTH_SHORT).show();
                if (!GroupDetailsActivity.instanceCreated || eventInfoFragment == null || eventInfoFragment.isDetached())
                    return;
                eventInfoFragment.updateNumParticipants((long) dataSnapshot.getValue());
                return;
            }
            HashMap<String, Boolean> map = new HashMap<>();
            map.put("isAdmin", false);
            map.put("isOrganizer", false);
            userlistref.setValue(map);
            joinedlistref.setValue("true");
            TimeNotificationScheduler.setNewReminder(GroupDetailsActivity.this, mGroup.getChatId(), mGroup.getNames(), mGroup.getStartDateTime(), TimeNotificationScheduler.DELAY_2HRS);
            SearchGroupFragment.getInstance().removeFromList(id);
            GroupDetailsActivity.this.finish();
        }
    }

    @Override
    public void reloadGroupDetails() {
        if (ref == null) ref = mFirebaseDatabase.getReference().child("groups").child(groupId);
        if (listener == null) {
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mGroup = dataSnapshot.getValue(Group.class);
                    if (inEvent) {
                        JoinedGroupFragment.getInstance().updateGroupDetails(groupId, mGroup, getIntent().getIntExtra(getString(R.string.TapPositionKey), -1));
                    } else {
                        SearchGroupFragment.getInstance().updateGroupDetails(groupId, mGroup, getIntent().getIntExtra(getString(R.string.TapPositionKey), -1));
                    }
                    if (mGroup != null) {
                        if (mapFragment == null) mapFragment = SupportMapFragment.newInstance();
                        mapFragment.getMapAsync(GroupDetailsActivity.this);
                        mGeoDataClient = Places.getGeoDataClient(GroupDetailsActivity.this);
                        if (eventInfoFragment == null) {
                            eventInfoFragment = new EventInfoFragment();
                            Bundle args2 = new Bundle();
                            args2.putBoolean(getString(R.string.InActivityKey), inEvent);
                            args2.putString(getString(R.string.groupNameKey), mGroup.getNames());
                            args2.putString(getString(R.string.groupPlaceKey), mGroup.getLocation());
                            args2.putString(getString(R.string.groupStartKey), mGroup.getStartDateTimeString());    // using E, dd/MM/yyyy hh:mma
                            args2.putString(getString(R.string.groupEndKey), mGroup.getEndDateTimeString());    // using E, dd/MM/yyyy hh:mma
                            args2.putString("eventstart2", mGroup.getStartDateTimeString2());   // using dd/MM/yyyy hh:mma
                            args2.putString("eventend2", mGroup.getEndDateTimeString2());   // using dd/MM/yyyy hh:mma
                            args2.putString(getString(R.string.groupPhotoUrlKey), mGroup.getPhotoUrl());
                            args2.putString(getString(R.string.groupDescriptionKey), mGroup.getDescription());
                            args2.putString("groupId", groupId);
                            args2.putString("placeId", mGroup.getPlaceId());
                            args2.putInt(getString(R.string.groupCurrentSizeKey), mGroup.getNumParticipants());
                            args2.putInt(getString(R.string.groupMaxSizeKey), mGroup.getMaxParticipants());
                            eventInfoFragment.setArguments(args2);
                            membersFragment = new MembersFragment();
                            membersFragment.setArguments(args2);
                            Log.d("GroupDetailsAct", "reloading" + args2.getString("eventplace"));
                            Log.d("GroupDetailsAct", "reloading" + mGroup.getLocation());
                        } else {
                            eventInfoFragment.setPhoto(mGroup.getPhotoUrl());
                            eventInfoFragment.setActivityName(mGroup.getNames());
                            eventInfoFragment.setPlaceName(mGroup.getLocation());
                            eventInfoFragment.setStartDate(mGroup.getStartDateTimeString(),mGroup.getStartDateTimeString2());
                            eventInfoFragment.setEndDate(mGroup.getEndDateTimeString(),mGroup.getEndDateTimeString2());
                            eventInfoFragment.setDescription(mGroup.getDescription());
                            eventInfoFragment.setParticipantsText(mGroup.getNumParticipants(), mGroup.getMaxParticipants());
                            eventInfoFragment.setPlaceId(mGroup.getPlaceId());
                        }
                        if (detailsPager == null) {
                            detailsPager = findViewById(R.id.groupDetailsPager);
                            detailsPager.setOffscreenPageLimit(5);
                            detailsPager.setAdapter(new GroupDetailsPagerAdapter(getSupportFragmentManager()));
                            TabLayout tabLayout = findViewById(R.id.detailsTabLayout);
                            tabLayout.setupWithViewPager(detailsPager);
                        }
                    } else {
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toasty.error(GroupDetailsActivity.this, "Activity not found", Toast.LENGTH_SHORT).show();
                }
            };
        }
        ref.addListenerForSingleValueEvent(listener);
    }
}
