package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.data.model.User;
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

        reloadRecycler();
        membersRecyclerView = getView().findViewById(R.id.membersRecyclerView);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        membersAdapter = new MembersAdapter(getActivity(), membersList, MembersFragment.this);
        membersRecyclerView.setAdapter(membersAdapter);
    }

    public void removeMember(String memberId, String memberName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle("Removing member")
                .setMessage("Are you sure you want to remove " + memberName + " from the group? Member removal is not reversible.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reloadRecycler();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void rateMember(String memberId, String memberName) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setView(inflater.inflate(R.layout.rate_popup_window, null))
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reloadRecycler();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setTitle("Rate " + memberName)
                .setMessage("Help make a friendly community on BoLiao by rating your fellow group members.");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void reloadRecycler() {
        membersList.clear();

        mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(MainActivity.userUid).child("isAdmin").getValue(Boolean.class) == true) userIsAdmin = true;
                else userIsAdmin = false;

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final String memberId = childSnapshot.getKey();
                    final boolean memberIsAdmin = childSnapshot.child("isAdmin").getValue(Boolean.class);
                    if (memberIsAdmin) Log.d("MembersFrag", "true");
                    else Log.d("MembersFrag", "false");

                    mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserInformation memberInfo = dataSnapshot.getValue(UserInformation.class);

                            if (userIsAdmin && memberId.equals(MainActivity.userUid)) membersList.add(new UserInformation2(memberInfo, false, true, memberId)); // card for myself when i am admin
                            else if (userIsAdmin && memberIsAdmin) membersList.add(new UserInformation2(memberInfo, true, true, memberId)); // card for other admin when i am admin
                            else if (userIsAdmin && !memberIsAdmin) membersList.add(new UserInformation2(memberInfo, true, false, memberId));   // card for other non-admin when i am admin
                            else if (!userIsAdmin && memberIsAdmin) membersList.add(new UserInformation2(memberInfo, false, true, memberId));   // card for other admin when i am non-admin
                            else if (!userIsAdmin && !memberIsAdmin) membersList.add(new UserInformation2(memberInfo, false, false, memberId)); // card for other non-admin (including myself) when i am non-admin

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
