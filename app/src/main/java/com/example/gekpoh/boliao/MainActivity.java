package com.example.gekpoh.boliao;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public static String userDisplayName;
    public static String userUid;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    //private ViewPager mViewPager;
    private static final String TAG = "MAINACTIVITY";
    private static final int NUM_PAGES = 2;
    private static final int RC_SIGN_IN = 1;

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
        mFirebaseDatabase = FirebaseDatabase.getInstance();
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

                }
                else {  // user signed out
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
        //===========TO BE REMOVED start===================
        //joinedGroups.add(new Group("badminton","gekpoh", (long)1, (long)2));
        //joinedGroups.add(new Group("soccer","nus", (long)1000, (long)2000));
        //searchedGroups.add(new Group("badminton","gekpoh", (long)5090, (long)3123213));
        //searchedGroups.add(new Group("soccer","nus", (long)412431234, (long)41234123));
        //searchedGroups.add(new Group("gohomeclub","yourhome1234567", (long)412341234, (long)41324241));
        //Bundle args = new Bundle();
        //args.putParcelableArrayList(getResources().getString(R.string.joined_groups), joinedGroups);
        Fragment jgFragment = JoinedGroupFragment.getInstance();
        //jgFragment.setArguments(args);
        //Bundle args2 = new Bundle();
        //args2.putParcelableArrayList(getResources().getString(R.string.searched_groups), searchedGroups);
        Fragment sgFragment = SearchGroupFragment.getInstance();
        //sgFragment.setArguments(args2);
        //===============TO BE REMOVED end==========
        ViewPager mViewPager = findViewById(R.id.fragmentHolder);
        mViewPager.setAdapter(new GroupPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.edit_profile:
                Intent startEditProfileActivityIntent = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(startEditProfileActivityIntent);
                return true;
            case R.id.create_new_act:
                Intent startCreateNewEventActivityIntent = new Intent(MainActivity.this, CreateNewEventActivity.class);
                startActivity(startCreateNewEventActivityIntent);
                return true;
            case R.id.signout:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }

    private class GroupPagerAdapter extends FragmentStatePagerAdapter {
        public GroupPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) return JoinedGroupFragment.getInstance();
            else return SearchGroupFragment.getInstance();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0){
                return getString(R.string.MainActivityTab1Name);
            }
            return getString(R.string.MainActivityTab2Name);
        }
    }

}
