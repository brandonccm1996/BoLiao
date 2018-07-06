package com.example.gekpoh.boliao;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity implements SearchGroupFragment.reloadFilterInterface {
    public static String userDisplayName;
    public static String userUid;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private DrawerLayout mDrawerLayout;
    private EditText distanceText, startDateText, endDateText;
    private Switch distanceFilterSwitch, timeFilterSwitch;
    private MenuItem searchItem;
    //private ViewPager mViewPager;
    private static final String TAG = "MAINACTIVITY";
    private static final int NUM_PAGES = 2;
    private static final int RC_SIGN_IN = 1;
    String dateTimeString;
    private int startyear = -1,endyear = -1, startmonth, endmonth, startday, endday, starthour = 0, endhour = 0, startmin = 0, endmin = 0;
    private float currentOffset;
    //private int pointerID;
    //GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // App Logo
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.common_google_signin_btn_icon_dark);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if(currentOffset > slideOffset){
                    searchItem.collapseActionView();
                    currentOffset = 0;
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                currentOffset = 1;
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                currentOffset = 0;
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        FirebaseDatabaseUtils.setUpConnectionListener();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) { // user signed in
                    userDisplayName = user.getDisplayName();
                    userUid = user.getUid();

                    mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(userUid)) {  // new user, never signed in before
                                UserInformation newUserInfo = new UserInformation(userDisplayName, "", "", 0, 0);
                                mUsersDatabaseReference.child(userUid).setValue(newUserInfo);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mUsersDatabaseReference.child(userUid).child("devicetoken").setValue(deviceToken);

                    JoinedGroupFragment jg = JoinedGroupFragment.getInstance();
                    jg.onSignIn();
                    SearchGroupFragment sg = SearchGroupFragment.getInstance();
                    sg.onSignIn();
                } else {  // user signed out
                    JoinedGroupFragment jg = JoinedGroupFragment.getInstance();
                    jg.onSignOut();
                    SearchGroupFragment sg = SearchGroupFragment.getInstance();
                    sg.onSignOut();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                    ))
                                    .build(), RC_SIGN_IN);
                }
            }
        };
        ViewPager mViewPager = findViewById(R.id.fragmentHolder);
        mViewPager.setAdapter(new GroupPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        distanceText = findViewById(R.id.distancetext);
        distanceText.setEnabled(false);
        startDateText = findViewById(R.id.startDateText);
        startDateText.setEnabled(false);
        endDateText = findViewById(R.id.endDatetText);
        endDateText.setEnabled(false);
        distanceFilterSwitch = findViewById(R.id.distanceSwitch);
        timeFilterSwitch = findViewById(R.id.timeSwitch);
        distanceFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    distanceText.setEnabled(true);
                } else {
                    distanceText.setEnabled(false);
                }
            }
        });
        timeFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startDateText.setEnabled(true);
                    endDateText.setEnabled(true);
                } else {
                    startDateText.setEnabled(false);
                    endDateText.setEnabled(false);
                }
            }
        });
        startDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startyear == -1) {
                    Calendar calendar = Calendar.getInstance();
                    startyear = calendar.get(Calendar.YEAR);
                    startmonth = calendar.get(Calendar.MONTH);
                    startday = calendar.get(Calendar.DAY_OF_MONTH);

                }

                DatePickerDialog mDatePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startyear = year;
                        startmonth = month;
                        startday = dayOfMonth;
                        dateTimeString = dayOfMonth + "/" + (month + 1) + "/" + year;
                        final TimePickerDialog mTimePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                starthour = hourOfDay;
                                startmin = minute;
                                dateTimeString = dateTimeString + " " + String.format("%02d:%02d", hourOfDay, minute);
                                startDateText.setText(dateTimeString);
                            }
                        }, starthour, startmin, false);
                        mTimePickerDialog.show();
                    }
                }, startyear, startmonth, startday);
                mDatePickerDialog.show();
            }
        });
        endDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (endyear == -1) {
                    Calendar calendar = Calendar.getInstance();
                    endyear = calendar.get(Calendar.YEAR);
                    endmonth = calendar.get(Calendar.MONTH);
                    endday = calendar.get(Calendar.DAY_OF_MONTH);

                }
                DatePickerDialog mDatePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        endyear = year;
                        endmonth = month;
                        endday = dayOfMonth;
                        dateTimeString = dayOfMonth + "/" + (month + 1) + "/" + year;
                        final TimePickerDialog mTimePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                endhour = hourOfDay;
                                endmin = minute;
                                dateTimeString = dateTimeString + " " + String.format("%02d:%02d", hourOfDay, minute);
                                endDateText.setText(dateTimeString);
                            }
                        }, endhour, endmin, false);
                        mTimePickerDialog.show();
                    }
                }, endyear, endmonth, endday);
                mDatePickerDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        searchItem = menu.findItem(R.id.filter);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        SearchView mSearchView = (SearchView) searchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.v(TAG, "Query submitted");
                SearchGroupFragment.getInstance().reloadList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toast.makeText(this, "Please make sure that you have an internet connection", Toast.LENGTH_LONG).show();
                    return true;
                }
                Intent startEditProfileActivityIntent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(startEditProfileActivityIntent);
                return true;
            case R.id.create_new_act:
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toast.makeText(this, "Please make sure that you have an internet connection", Toast.LENGTH_LONG).show();
                    return true;
                }
                Intent startCreateNewEventActivityIntent = new Intent(MainActivity.this, CreateNewEventActivity.class);
                startActivity(startCreateNewEventActivityIntent);
                return true;
            case R.id.signout:
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        LatLng latLng = SearchGroupFragment.getInstance().getLastKnownLatLng();
        if (latLng != null) {
            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(getString(R.string.sharedprefs_lastknownlatitudekey), (float) latLng.latitude);
            editor.putFloat(getString(R.string.sharedprefs_lastknownlongitudekey), (float) latLng.longitude);
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerVisible(findViewById(R.id.filter_drawer))) {
            mDrawerLayout.closeDrawers();
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    public boolean distanceFilterChecked() {
        return distanceFilterSwitch.isChecked();
    }

    @Override
    public boolean timeFilterChecked() {
        return timeFilterSwitch.isChecked();
    }

    @Override
    public boolean categoriesFilterChecked() {
        //Need to implement something next time
        return false;
    }

    @Override
    public long getDistanceFilter() {
        if (!distanceText.getText().toString().equals("")) {
            return Long.parseLong(distanceText.getText().toString());
        } else {
            return -1;
        }
    }

    //pos 0 has start time filter, pos 1 has end time filter
    @Override
    public long[] getTimeFilter() {
        long[] timeFilter = {-1,-1};
        if(timeFilterSwitch.isChecked()){
            try{
                timeFilter[0] = Group.groupDateFormatter.parse(startDateText.getText().toString()).getTime();
                timeFilter[1] = Group.groupDateFormatter.parse(endDateText.getText().toString()).getTime();
            }catch(ParseException e){
                Log.e(TAG, "Failed to parse");
            }
        }
        return timeFilter;
    }

    @Override
    public HashSet<String> getCatergoriesFilter() {
        HashSet<String> hashSet = new HashSet<>();
        return hashSet;
    }

    private class GroupPagerAdapter extends FragmentStatePagerAdapter {
        public GroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return JoinedGroupFragment.getInstance();
            else return SearchGroupFragment.getInstance();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.MainActivityTab1Name);
            }
            return getString(R.string.MainActivityTab2Name);
        }
    }
}
