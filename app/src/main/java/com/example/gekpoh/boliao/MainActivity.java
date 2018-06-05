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
        /*
        //==================================CODE FOR GESTURES START=================================================//
        GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.v(TAG, "SCROLL DETECTED");
                if(Math.abs(distanceX) < GestureSettings.ACTIVATEDISTANCE) return true;
                if(distanceX > 0){
                    JoinedGroupFragment jgfragment = new JoinedGroupFragment();
                    Bundle args = new Bundle();
                    args.putParcelableArrayList(getResources().getString(R.string.joined_groups), joinedGroups);
                    jgfragment.setArguments(args);
                    //changeFragment(jgfragment, true);
                }else{
                    SearchGroupFragment sgfragment = new SearchGroupFragment();
                    Bundle args = new Bundle();
                    args.putParcelableArrayList(getResources().getString(R.string.searched_groups), joinedGroups);
                    sgfragment.setArguments(args);
                    //changeFragment(sgfragment, false);
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //if(Math.abs(velocityX) < GestureSettings.ACTIVATEVELOCITY) return true;
                Log.v(TAG, "FLING DETECTED");
                if(velocityX > 0){
                    JoinedGroupFragment jgfragment = new JoinedGroupFragment();
                    Bundle args = new Bundle();
                    args.putParcelableArrayList(getResources().getString(R.string.joined_groups), joinedGroups);
                    jgfragment.setArguments(args);
                    //changeFragment(jgfragment, true);
                }else{
                    SearchGroupFragment sgfragment = new SearchGroupFragment();
                    Bundle args = new Bundle();
                    args.putParcelableArrayList(getResources().getString(R.string.searched_groups), joinedGroups);
                    sgfragment.setArguments(args);
                    //changeFragment(sgfragment, false);
                }
                return true;
            }
        };
        mDetector = new GestureDetector(this, mGestureListener);
        View fragmentLayout= findViewById(R.id.fragmentHolder);
        fragmentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });*/
        //===================================================CODE FOR GESTURES END============================================//
    }
    //Changes the fragment from one to another direction = true for left to right else right to left
    /*public void changeFragment(Fragment fragment, boolean direction) {
        FragmentTransaction fTran = getFragmentManager().beginTransaction();
        fTran.add(R.id.fragmentHolder, fragment);
        if(direction) {
            fTran.setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right, R.animator.enter_from_right, R.animator.exit_to_left);
        }else{
            fTran.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left, R.animator.enter_from_left, R.animator.exit_to_right);
        }
        fTran.commit();
    }*/
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
