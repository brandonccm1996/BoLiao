package com.example.gekpoh.boliao;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class JoinedGroupFragment extends Fragment {
    private static Fragment jgFragment;
    private boolean signedIn = false;
    private RecyclerView groupView;
    private GroupRecyclerAdapter adapter;
    private DatabaseReference mGroupDatabaseReference, mUserListDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;
    private final ArrayList<Group> joinedgroups = new ArrayList<>();
    private final String TAG = "JoinedGroupFragment";

    public static Fragment getInstance() {
        if (jgFragment == null) {
            jgFragment = new JoinedGroupFragment();
        }
        return jgFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "Attaching View");
        return inflater.inflate(R.layout.groups_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new GroupRecyclerAdapter(getActivity(), joinedgroups);
        groupView = getView().findViewById(R.id.groupList);
        groupView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupView.setAdapter(adapter);
    }

    public void onSignIn() {
        if (signedIn) return;
        signedIn = true;
        mUserListDatabaseReference = FirebaseDatabase.getInstance().getReference().child("joinedlists").child(MainActivity.userUid);
        mUserListDatabaseReference.keepSynced(true);
        mGroupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");
        mGroupDatabaseReference.keepSynced(true);
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG,"Adding group");
                Group group = dataSnapshot.getValue(Group.class);
                joinedgroups.add(group);
                adapter.notifyDataSetChanged();
                Log.e(TAG,"Group added");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.v(TAG, (String)dataSnapshot.getValue());
                if(((String)dataSnapshot.getValue()).equals("true")) {
                    mGroupDatabaseReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(mValueEventListener);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUserListDatabaseReference.addChildEventListener(mChildEventListener);
    }

    public void onSignOut() {
        if(!signedIn) return;
        signedIn = false;
        joinedgroups.clear();
        adapter.notifyDataSetChanged();
        if (mUserListDatabaseReference != null && mChildEventListener != null) {
            mUserListDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
