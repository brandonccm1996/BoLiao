package com.example.gekpoh.boliao;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class GroupDetailsActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG = "GroupDetailsActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static int NUM_PAGES = 3;
    private static final int DEFAULT_ZOOM = 15;
    private boolean locationPermissionGranted = false;
    private Group mGroup;//The group to refer to when we want to access required information
    private ChatFragment chatFragment;
    private EventInfoFragment eventInfoFragment;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private Place place;
    private CameraPosition mCameraPosition;
    //Client to get data from placeid
    private GeoDataClient mGeoDataClient;
    // Client to get current Location
    //private FusedLocationProviderClient mFusedLocationProviderClient;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_details_activity_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mGroup = getIntent().getParcelableExtra(getString(R.string.groupKey));//Need to pass on group details before starting this activity
        chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(getString(R.string.groupIdKey), mGroup.getGroupId());
        mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        eventInfoFragment = new EventInfoFragment();
        Bundle args2 = new Bundle();
        args2.putString(getString(R.string.groupNameKey), mGroup.getName());
        args2.putString(getString(R.string.groupPlaceKey), mGroup.getPlaceName());
        args2.putLong(getString(R.string.groupStartKey), mGroup.getStartDate());
        args2.putLong(getString(R.string.groupEndKey), mGroup.getEndDate());
        args2.putString(getString(R.string.groupPhotoUrlKey), mGroup.getPhotoUrl());
        args2.putInt(getString(R.string.groupCurrentSizeKey),mGroup.getCurrentSize());
        args2.putInt(getString(R.string.groupMaxSizeKey),mGroup.getMaxSize());
        eventInfoFragment.setArguments(args2);
        ViewPager detailsPager = findViewById(R.id.groupDetailsPager);
        detailsPager.setAdapter(new GroupDetailsPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.detailsTabLayout);
        tabLayout.setupWithViewPager(detailsPager);
        mGeoDataClient = Places.getGeoDataClient(this);
    }

    private void checkRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }else{
            locationPermissionGranted = true;
        }
    }

    private void updateMapUI(){
        mGeoDataClient.getPlaceById(mGroup.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    place = places.get(0);
                    places.release();
                } else {
                    Toast.makeText(GroupDetailsActivity.this, "Place not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if(place == null)return;
        googleMap.addMarker(new MarkerOptions().position(place.getLatLng())
                .title(place.getName().toString())
                .snippet(place.getAddress().toString()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),DEFAULT_ZOOM));
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
                    return chatFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
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
                default:
                    return null;
            }
        }
    }
}
