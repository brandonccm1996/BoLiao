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

public class MembersFragment extends Fragment {

    private String groupId;
    private RecyclerView membersRecyclerView;
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private ArrayList<UserInformation2> membersList = new ArrayList<>();
    private String memberId;
    private UserInformation memberInfo;
    private UserInformation2 memberInfo2;
    private boolean userIsAdmin;
    private MembersAdapter membersAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.members_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle args = getArguments();
        groupId = args.getString("groupId");

        mUserListsDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("userlists").child(groupId);
        mUsersDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("users");
        mUserListsDatabaseReference.keepSynced(true);

//        Example
        membersList.add(new UserInformation2(new UserInformation("user1", "desc1", null, 5, 2), false, "abc"));
        membersList.add(new UserInformation2(new UserInformation("user2", "desc2", null, 3, 3), false, "abc"));

        membersRecyclerView = getView().findViewById(R.id.membersRecyclerView);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        membersAdapter = new MembersAdapter(getActivity(), membersList);
        membersRecyclerView.setAdapter(membersAdapter);

//        memberInfo2 = new UserInformation2(
//                new UserInformation("user1", "desc1", null, 5, 2), false, "abc"); // VERSION 1

        mUserListsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(MainActivity.userUid).child("isAdmin").getValue(Boolean.class) == true) userIsAdmin = true;   // if current user is admin
                else memberInfo2.setIsAdmin(false); // if current user not admin

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    memberId = childSnapshot.getKey();
                    Log.d("MembersFrag", memberId);

                    mUsersDatabaseReference.child(memberId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d("MembersFrag2", memberId);
                            memberInfo = dataSnapshot.getValue(UserInformation.class);
//                            memberInfo2.setUserId(memberId);              // VERSION 1
//                            memberInfo2.setUserInformation(memberInfo);   // VERSION 1
//                            memberInfo2.setIsAdmin(true);                 // VERSION 1
//                            membersList.add(memberInfo2);                 // VERSION 1
                            membersList.add(new UserInformation2(memberInfo, true, memberId));    // VERSION 2
                            membersAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
