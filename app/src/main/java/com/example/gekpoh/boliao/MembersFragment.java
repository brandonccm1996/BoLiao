package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MembersFragment extends Fragment {

    private RecyclerView membersRecyclerView;
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ArrayList<UserInformation> membersList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.members_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserListsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("userlists");
        mUsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        mUserListsDatabaseReference.keepSynced(true);
        mUsersDatabaseReference.keepSynced(true);

        // Example
        membersList.add(new UserInformation("user1", "desc1", "photoUrl1", 5, 2));
        membersList.add(new UserInformation("user2", "desc2", "photoUrl2", 3, 3));

        membersRecyclerView = getView().findViewById(R.id.membersRecyclerView);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MembersAdapter membersAdapter = new MembersAdapter(getActivity(), membersList);
        membersRecyclerView.setAdapter(membersAdapter);

        mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
