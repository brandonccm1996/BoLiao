package com.example.gekpoh.boliao;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.util.ArrayList;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    ArrayList<Group> joinedGroups, searchedGroups;
    private ViewPager mViewPager;
    private static final String TAG = "MAINACTIVITY";
    private static final int NUM_PAGES = 2;
    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
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

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) { // user signed in

                }
                else {  // user signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build()
                                    ))
                                    .build(), RC_SIGN_IN);
                }
            }
        };
        //===========TO BE REMOVED start===================
        joinedGroups = new ArrayList<>();//for testing
        joinedGroups.add(new Group("badminton","gekpoh"));
        joinedGroups.add(new Group("soccer","nus"));
        searchedGroups = new ArrayList<>();//for testing
        searchedGroups.add(new Group("badminton","gekpoh"));
        searchedGroups.add(new Group("soccer","nus"));
        searchedGroups.add(new Group("gohomeclub","yourhome"));
        Bundle args = new Bundle();
        args.putParcelableArrayList(getResources().getString(R.string.joined_groups), joinedGroups);
        Fragment jgFragment = JoinedGroupFragment.getInstance();
        jgFragment.setArguments(args);
        Bundle args2 = new Bundle();
        args2.putParcelableArrayList(getResources().getString(R.string.searched_groups), searchedGroups);
        Fragment sgFragment = SearchGroupFragment.getInstance();
        sgFragment.setArguments(args2);
        //===============TO BE REMOVED end==========
        mViewPager = findViewById(R.id.fragmentHolder);
        mViewPager.setAdapter(new GroupPagerAdapter(getSupportFragmentManager()));
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
    }
}
