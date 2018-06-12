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

import java.util.ArrayList;

public class JoinedGroupFragment extends Fragment {
    private static Fragment jgFragment;
    private RecyclerView groupView;
    private GroupRecyclerAdapter adapter;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private final ArrayList<Group> joinedgroups = new ArrayList<>();
    private final String TAG = "JoinedGroupFragment";

    public static Fragment getInstance(){
        if(jgFragment == null){
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
        //groupList = getArguments().getParcelableArrayList(getResources().getString(R.string.joined_groups));
        //joinedgroups.add(new Group("hi","hi","hi","hi","","hi",(long)123123,(long)1432432,132));
        adapter = new GroupRecyclerAdapter(getActivity(),joinedgroups);
        groupView = getView().findViewById(R.id.groupList);
        groupView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupView.setAdapter(adapter);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Group group = dataSnapshot.getValue(Group.class);
                joinedgroups.add(group);
                adapter.notifyDataSetChanged();
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
    }
}
