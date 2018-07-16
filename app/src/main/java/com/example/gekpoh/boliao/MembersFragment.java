package com.example.gekpoh.boliao;

import android.content.DialogInterface;
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
import android.widget.RatingBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MembersFragment extends Fragment {

    private String groupId;
    private boolean inEvent;
    private RecyclerView membersRecyclerView;
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mUsersRatedDatabaseReference;
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
        inEvent = args.getBoolean("inevent");

        mUserListsDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("userlists").child(groupId);
        mUsersDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("users");
        mUserListsDatabaseReference.keepSynced(true);
        mUsersRatedDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("usersRated").child(MainActivity.userUid);

        reloadRecycler();
        membersRecyclerView = getView().findViewById(R.id.membersRecyclerView);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        membersAdapter = new MembersAdapter(getActivity(), membersList, MembersFragment.this);
        membersRecyclerView.setAdapter(membersAdapter);
    }

    public void reloadRecycler() {
        membersList.clear();

        mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (inEvent) {
                    if (dataSnapshot.child(MainActivity.userUid).child("isAdmin").getValue(Boolean.class) == true) userIsAdmin = true;
                    else userIsAdmin = false;
                }

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final String memberId = childSnapshot.getKey();
                    final boolean memberIsAdmin = childSnapshot.child("isAdmin").getValue(Boolean.class);

                    mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final UserInformation memberInfo = dataSnapshot.getValue(UserInformation.class);

                            mUsersRatedDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    boolean memberRatedBefore;
                                    if (dataSnapshot.child(memberId).exists()) memberRatedBefore = true;
                                    else memberRatedBefore = false;

                                    if (userIsAdmin && memberId.equals(MainActivity.userUid)) membersList.add(new UserInformation2(memberInfo, false, true, memberId, memberRatedBefore, inEvent)); // card for myself when i am admin
                                    else if (userIsAdmin && memberIsAdmin) membersList.add(new UserInformation2(memberInfo, true, true, memberId, memberRatedBefore, inEvent)); // card for other admin when i am admin
                                    else if (userIsAdmin && !memberIsAdmin) membersList.add(new UserInformation2(memberInfo, true, false, memberId, memberRatedBefore, inEvent));   // card for other non-admin when i am admin
                                    else if (!userIsAdmin && memberIsAdmin) membersList.add(new UserInformation2(memberInfo, false, true, memberId, memberRatedBefore, inEvent));   // card for other admin when i am non-admin
                                    else if (!userIsAdmin && !memberIsAdmin) membersList.add(new UserInformation2(memberInfo, false, false, memberId, memberRatedBefore, inEvent)); // card for other non-admin (including myself) when i am non-admin

                                    membersAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
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

    public void rateMember(final String memberId, String memberName) {  // member not rated before
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rateMemberView = inflater.inflate(R.layout.rate_popup_window, null);
        final RatingBar ratingBar = (RatingBar) rateMemberView.findViewById(R.id.ratingBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setView(rateMemberView)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int memberNumRatings = dataSnapshot.child("numRatings").getValue(Integer.class);
                                float memberSumRating = dataSnapshot.child("sumRating").getValue(Float.class);
                                mUsersDatabaseReference.child(memberId).child("numRatings").setValue(memberNumRatings+1);
                                mUsersDatabaseReference.child(memberId).child("sumRating").setValue(memberSumRating+ratingBar.getRating());
                                mUsersRatedDatabaseReference.child(memberId).setValue(ratingBar.getRating());
                                reloadRecycler();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setTitle("Rate " + memberName)
                .setMessage("Help to make a friendly community on BoLiao by rating your group members based on your experiences with them.");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void rateMember2(final String memberId, String memberName) { // member rated before
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rateMemberView = inflater.inflate(R.layout.rate_popup_window, null);
        final RatingBar ratingBar = (RatingBar) rateMemberView.findViewById(R.id.ratingBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setView(rateMemberView)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final float memberSumRating = dataSnapshot.child("sumRating").getValue(Float.class);

                                mUsersRatedDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        float previousRating = dataSnapshot.getValue(Float.class);
                                        mUsersDatabaseReference.child(memberId).child("sumRating").setValue(memberSumRating-previousRating+ratingBar.getRating());
                                        mUsersRatedDatabaseReference.child(memberId).setValue(ratingBar.getRating());
                                        reloadRecycler();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setTitle("Rate " + memberName)
                .setMessage("You have rated this user before. Would you like to give a new rating?");

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
