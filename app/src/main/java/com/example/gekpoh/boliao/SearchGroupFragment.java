package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

import es.dmoral.toasty.Toasty;

public class SearchGroupFragment extends Fragment implements GroupRecyclerAdapter.GroupTouchCallBack {
    private Context mContext;
    private reloadFilterInterface mReloadInterface;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    private static final int LOCATION_SETTINGS_RC = 6;
    private boolean locationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private RecyclerView groupView;
    private GroupRecyclerAdapter adapter;
    private TextView searchTextView, noActivitiesTextView;
    private ImageButton initialSearchButton;
    private static SearchGroupFragment sgFragment;
    private final ArrayList<Group> searchedgroups = new ArrayList<>();
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener, mGetLocationSingleValueEventListener;
    private final String TAG = "SEARCHGROUPFRAGMENT";
    private boolean signedIn = false;
    private long reloadTimer = 0;
    private LatLng lastKnownLatLng;
    private boolean stillLoading = false;
    private HashSet<String> loadingList = new HashSet<>();

    public static SearchGroupFragment getInstance() {
        if (sgFragment == null) {
            sgFragment = new SearchGroupFragment();
        }
        return sgFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            mReloadInterface = (reloadFilterInterface) context;
        } catch (ClassCastException e) {
            Toasty.error(mContext, "Need to implement reload interface", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.groups_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        lastKnownLatLng = new LatLng(prefs.getFloat(getString(R.string.sharedprefs_lastknownlatitudekey), (float) 1.3521), prefs.getFloat(getString(R.string.sharedprefs_lastknownlongitudekey), (float) 103.8198));
        checkRequestLocationPermission();
        if (locationPermissionGranted) {
            getDeviceLocation();
        }
        super.onViewCreated(view, savedInstanceState);
        //groupList = getArguments().getParcelableArrayList(getResources().getString(R.string.searched_groups));
        groupView = getView().findViewById(R.id.groupList);
        noActivitiesTextView = getView().findViewById(R.id.noActivitiesTextView);
        searchTextView = getView().findViewById(R.id.SearchActivitiesTextView);
        noActivitiesTextView.setVisibility(View.INVISIBLE);
        initialSearchButton = getView().findViewById(R.id.initialSearchButton);
        initialSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReloadInterface.startSearch();
            }
        });
        groupView.setLayoutManager(new reloadLayoutManager(getActivity()));
        groupView.setVisibility(View.GONE);
        if(adapter != null) groupView.setAdapter(adapter);
    }

    public void onSignIn() {
        if (signedIn) return;
        if(adapter == null){
            adapter = new GroupRecyclerAdapter(this, null);
            if(groupView != null) groupView.setAdapter(adapter);
        }
        signedIn = true;
        mDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("groups");
        mDatabaseReference.keepSynced(true);
    }

    public void onSignOut() {
        if (!signedIn) return;
        signedIn = false;
        adapter.clearList();
        displayInitialLayout();
    }

    @Override
    public boolean touchGroup(int pos) {
        /*if (!FirebaseDatabaseUtils.connectedToDatabase()) {
            Toast.makeText(mContext, "Unable to retrieve information of selected activity. Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (GroupDetailsActivity.isInstanceCreated()) return;
        Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
        intent.putExtra(getString(R.string.groupKey), searchedgroups.get(pos).getChatId());
        startActivity(intent);*/
        return false;
    }

    public boolean reloadList(String quer) {
        //Add other filters here
        if (SystemClock.elapsedRealtime() - reloadTimer < 2000)
            return false;//Can only reload once every 2 second
        reloadTimer = SystemClock.elapsedRealtime();

        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
            Toasty.error(mContext, "Offline searching not available. Please check your internet connection", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!signedIn){
            Toasty.error(mContext, "Please sign in before joining new activities", Toast.LENGTH_SHORT).show();
            return false;
        }

        boolean distanceFilter = mReloadInterface.distanceFilterChecked();
        long distance = mReloadInterface.getDistanceFilter();
        if (distanceFilter && distance == -1) {
            Toasty.error(mContext, "Please set the distance filter", Toast.LENGTH_SHORT).show();
            return false;
        }

        final boolean timeFilter = mReloadInterface.timeFilterChecked();
        final long[] timefilters = mReloadInterface.getTimeFilter();
        boolean categoriesFilter = mReloadInterface.categoriesFilterChecked();
        if (timeFilter && (timefilters[0] == -1 || timefilters[1] == -1)) {
            Toasty.error(mContext, "Please set an appropriate date in time filter", Toast.LENGTH_SHORT).show();
            return false;
        }
        final String query = quer.toLowerCase();
        searchedgroups.clear();
        adapter.clearList();
        getDeviceLocation();
        if (distanceFilter) {
            if(locationPermissionGranted) {
                changeLocationSettings();
                mGetLocationSingleValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        //Need to implement extra filters here for both time and categories etc.
                        loadingList.remove(dataSnapshot.getKey());
                        if(group != null) {
                            if (group.getNames().toLowerCase().contains(query) || group.getDescription().toLowerCase().contains(query) || group.getLocation().toLowerCase().contains(query)) {
                                if (!timeFilter || (group.getStartDateTime() >= timefilters[0] && group.getEndDateTime() <= timefilters[1])) {
                                    Log.v(TAG, "group added");
                                    searchedgroups.add(group);
                                    adapter.addGroup(group);
                                }
                            }
                        }
                        if (loadingList.isEmpty() && !stillLoading) {
                            //adapter.notifyDataSetChanged();
                            if (adapter.isListEmpty()) {
                                displayActivitiesNotFound();
                            } else {
                                displayActivitiesFound();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };
                GeoFire geoFire = FirebaseDatabaseUtils.getGeoFireInstance();
                final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lastKnownLatLng.latitude, lastKnownLatLng.longitude), distance);
                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        stillLoading = true;
                        if (JoinedGroupFragment.alreadyJoinedGroup(key)) return;
                        loadingList.add(key);
                        mDatabaseReference.child(key).addListenerForSingleValueEvent(mGetLocationSingleValueEventListener);
                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        stillLoading = false;
                        if (loadingList.isEmpty() && !stillLoading) {
                            //adapter.notifyDataSetChanged();
                            if (adapter.isListEmpty()) {
                                displayActivitiesNotFound();
                            } else {
                                displayActivitiesFound();
                            }
                        }
                        geoQuery.removeGeoQueryEventListener(this);
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {
                        Toasty.error(getActivity(), "There is a database error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                checkRequestLocationPermission();
            }
        } else {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (JoinedGroupFragment.alreadyJoinedGroup(data.getKey())) continue;
                        //Need to implement extra filters for categories and end timehere etc.
                        Group group = data.getValue(Group.class);
                        if (group == null) {
                            continue;
                        } else if (group.getNames().toLowerCase().contains(query) || group.getDescription().toLowerCase().contains(query) || group.getLocation().toLowerCase().contains(query)) {
                            if (!timeFilter || group.getEndDateTime() <= timefilters[1])
                                searchedgroups.add(group);
                                adapter.addGroup(group);
                        }
                    }
                    //adapter.notifyDataSetChanged();
                    if(adapter.isListEmpty()){
                        displayActivitiesNotFound();
                    }else{
                        displayActivitiesFound();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            //Need to implement extra filters for starttime here
            if (timeFilter) {
                mDatabaseReference.orderByChild("startDateTime").startAt(timefilters[0]).endAt(timefilters[1]).addListenerForSingleValueEvent(mValueEventListener);
            } else {
                mDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);
            }
        }
        return true;
    }

    public void removeFromList(String id) {
        for (Group group : searchedgroups) {
            if (group.getChatId().equals(id)) {
                searchedgroups.remove(group);
                adapter.removeGroup(group);
                //adapter.notifyDataSetChanged();
                if(adapter.isListEmpty()) displayActivitiesNotFound();
                break;
            }
        }
    }

    public void updateGroupDetails(Group group, final int pos) {
        if (pos == -1) return;
        if (group != null) {
            searchedgroups.set(pos, group);
            adapter.updateGroup(pos,group);
        } else {
            Toasty.error(mContext, "For some reason, this activity has been deleted.", Toast.LENGTH_SHORT).show();
            searchedgroups.remove(pos);
            adapter.removeGroupAtPos(pos);
        }
        //adapter.notifyDataSetChanged();
    }

    private class reloadLayoutManager extends LinearLayoutManager {
        public reloadLayoutManager(Context context) {
            super(context);
        }

        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int scrollRange = super.scrollVerticallyBy(dy, recycler, state);
            int overscroll = dy - scrollRange;
            /*if (overscroll < -20) {//any value lesser than 0 is overscroll
                // top overscroll
                reloadList("");
            }*/
            return scrollRange;
        }
    }

    private void checkRequestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Location Permission required");
                alertBuilder.setMessage("Location Permission is required for finding activities close to your current location.");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            locationPermissionGranted = true;
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

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location currentLocation = (Location) task.getResult();
                        lastKnownLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public interface reloadFilterInterface {
        boolean distanceFilterChecked();

        boolean timeFilterChecked();

        boolean categoriesFilterChecked();

        long getDistanceFilter();

        long[] getTimeFilter();

        HashSet<String> getCatergoriesFilter();

        void startSearch();
    }

    public LatLng getLastKnownLatLng() {
        return lastKnownLatLng;
    }

    public void changeLocationSettings() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                LOCATION_SETTINGS_RC);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        /*LocationManager lm = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setMessage("Please enable GPS to utilize the distance filter");
            dialog.show();
        }
        return gps_enabled||network_enabled;*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOCATION_SETTINGS_RC);
    }

    public void displayActivitiesFound(){
        searchTextView.setVisibility(View.INVISIBLE);
        initialSearchButton.setVisibility(View.INVISIBLE);
        initialSearchButton.setEnabled(false);
        groupView.setVisibility(View.VISIBLE);
        noActivitiesTextView.setVisibility(View.INVISIBLE);
    }

    public void displayActivitiesNotFound(){
        searchTextView.setVisibility(View.INVISIBLE);
        initialSearchButton.setVisibility(View.INVISIBLE);
        initialSearchButton.setEnabled(false);
        groupView.setVisibility(View.GONE);
        noActivitiesTextView.setVisibility(View.VISIBLE);
    }

    public void displayInitialLayout(){
        searchTextView.setVisibility(View.VISIBLE);
        initialSearchButton.setVisibility(View.VISIBLE);
        initialSearchButton.setEnabled(true);
        groupView.setVisibility(View.GONE);
        noActivitiesTextView.setVisibility(View.GONE);
    }
}
