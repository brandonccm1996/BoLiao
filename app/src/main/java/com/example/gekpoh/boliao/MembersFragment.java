package com.example.gekpoh.boliao;

import android.content.Context;
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
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import es.dmoral.toasty.Toasty;

public class MembersFragment extends Fragment {

    private String groupId;
    private boolean inEvent;
    private RecyclerView membersRecyclerView;
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mUsersRatedDatabaseReference;
    private DatabaseReference mUsersRatingDatabaseReference;
    private DatabaseReference mJoinedListsDatabaseReference;
    private DatabaseReference mGroupsDatabaseReference;
    private DatabaseReference mRemoveNotifDatabaseReference;
    private ArrayList<UserInformation2> membersList = new ArrayList<>();
    private boolean userIsAdmin;
    private boolean userIsOrganizer;
    private MembersAdapter membersAdapter;
    private reloadDetailsInterface reloadInterface;

    private Bundle args;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.members_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        args = getArguments();
        groupId = args.getString("groupId");
        inEvent = args.getBoolean("inevent");

        mUserListsDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("userlists").child(groupId);
        mUsersDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("users");
        mUsersRatedDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("usersRated").child(MainActivity.userUid);
        mUsersRatingDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("usersRating");
        mJoinedListsDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("joinedlists");
        mGroupsDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("groups").child(groupId);
        mRemoveNotifDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("removeNotif").child(groupId);
        mUserListsDatabaseReference.keepSynced(true);
        mUsersRatedDatabaseReference.keepSynced(true);
        mUsersRatingDatabaseReference.keepSynced(true);
        mUsersDatabaseReference.keepSynced(true);

        reloadRecycler();
        membersRecyclerView = getView().findViewById(R.id.membersRecyclerView);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        membersAdapter = new MembersAdapter(getActivity(), membersList, MembersFragment.this);
        membersRecyclerView.setAdapter(membersAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            reloadInterface = (reloadDetailsInterface) context;
        } catch (ClassCastException castException) {
            Log.d("MembersFragment", "Activity does not implement interface");
        }
    }

    public interface reloadDetailsInterface {
        void reloadGroupDetails();
    }

    public void reloadRecycler() {
        membersList.clear();

        mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (inEvent) {
                    userIsOrganizer = dataSnapshot.child(MainActivity.userUid).child("isOrganizer").getValue(Boolean.class);
                    userIsAdmin = dataSnapshot.child(MainActivity.userUid).child("isAdmin").getValue(Boolean.class);
                }

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    final String memberId = childSnapshot.getKey();
                    final boolean memberIsAdmin = childSnapshot.child("isAdmin").getValue(Boolean.class);
                    final boolean memberIsOrganizer = childSnapshot.child("isOrganizer").getValue(Boolean.class);

                    mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            final UserInformation memberInfo = dataSnapshot.getValue(UserInformation.class);

                            mUsersRatingDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final UserRating memberRating = dataSnapshot.getValue(UserRating.class);

                                    mUsersRatedDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            boolean memberRatedBefore;
                                            String memberStatus;
                                            String userStatus;

                                            if (dataSnapshot.child(memberId).exists()) memberRatedBefore = true;
                                            else memberRatedBefore = false;

                                            if (userIsOrganizer) userStatus = "organizer";
                                            else if (userIsAdmin) userStatus = "admin";
                                            else userStatus = "member";

                                            if (memberIsOrganizer) memberStatus = "organizer";
                                            else if (memberIsAdmin) memberStatus = "admin";
                                            else memberStatus = "member";

                                            membersList.add(new UserInformation2(memberInfo, memberRating, memberId, inEvent, memberRatedBefore, memberStatus, userStatus));
                                            if (membersList.size() > 0) {
                                                Collections.sort(membersList, new Comparator<UserInformation2>() {
                                                    @Override
                                                    public int compare(UserInformation2 o1, UserInformation2 o2) {
                                                        if (o1.getMemberStatus().equals("admin") && o2.getMemberStatus().equals("member")) return -1;
                                                        else if (o1.getMemberStatus().equals("member") && o2.getMemberStatus().equals("admin")) return 1;
                                                        else if (o1.getMemberStatus().equals("organizer") && (o2.getMemberStatus().equals("admin") || o2.getMemberStatus().equals("member"))) return -1;
                                                        else if ((o2.getMemberStatus().equals("admin") || o2.getMemberStatus().equals("member") && o2.getMemberStatus().equals("organizer"))) return 1;
                                                        else return o1.getUserInformation().getName().compareTo(o2.getUserInformation().getName());
                                                    }
                                                });
                                            }
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

    public void removeMember(final String memberId, String memberName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle("Removing member")
                .setMessage("Are you sure you want to remove " + memberName + " from the group? Member removal is not reversible.\n\nDo note that after you remove this member, you and your group members will not be able to rate this user anymore.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserListsDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toasty.error(getActivity(), "This member is not in the group anymore", Toast.LENGTH_SHORT).show();
                                    reloadRecycler();
                                }
                                else {
                                    mUserListsDatabaseReference.child(memberId).removeValue();
                                    mJoinedListsDatabaseReference.child(memberId).child(groupId).removeValue();
                                    mGroupsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int numParticipants = dataSnapshot.child("numParticipants").getValue(Integer.class);
                                            mGroupsDatabaseReference.child("numParticipants").setValue(numParticipants-1);
                                            reloadInterface.reloadGroupDetails();
                                            reloadRecycler();

                                            // create notification object
                                            final String notifId = mRemoveNotifDatabaseReference.push().getKey();
                                            mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.child("updateNotifEnabled").getValue(Boolean.class)) mRemoveNotifDatabaseReference.child(notifId).child(memberId).setValue(true);
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
                        mUserListsDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toasty.error(getActivity(), "This member is not in the group anymore", Toast.LENGTH_SHORT).show();
                                    reloadRecycler();
                                }
                                else {
                                    mUsersRatingDatabaseReference.child(memberId).runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                            int memberNumRatings = mutableData.child("numRatings").getValue(Integer.class);
                                            float memberSumRating = mutableData.child("sumRating").getValue(Float.class);
                                            mutableData.child("numRatings").setValue(memberNumRatings+1);
                                            mutableData.child("sumRating").setValue(memberSumRating+ratingBar.getRating());
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                            mUsersRatedDatabaseReference.child(memberId).setValue(ratingBar.getRating());
                                            reloadRecycler();
                                        }
                                    });

                                }
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
                        mUserListsDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toasty.error(getActivity(), "This member is not in the group anymore", Toast.LENGTH_SHORT).show();
                                    reloadRecycler();
                                }
                                else {
                                    mUsersRatedDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            final float previousRating = dataSnapshot.getValue(Float.class);

                                            mUsersRatingDatabaseReference.child(memberId).runTransaction(new Transaction.Handler() {
                                                @NonNull
                                                @Override
                                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                                    float memberSumRating = mutableData.child("sumRating").getValue(Float.class);
                                                    mutableData.child("sumRating").setValue(memberSumRating-previousRating+ratingBar.getRating());
                                                    return Transaction.success(mutableData);
                                                }

                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                                    mUsersRatedDatabaseReference.child(memberId).setValue(ratingBar.getRating());
                                                    reloadRecycler();
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

    public void appointAdmin(final String memberId, String memberName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle("Appointing admin")
                .setMessage("Do you want to appoint " + memberName + " as a group admin?\n\nAdmins can remove members, appoint or dismiss other admins and edit activity info.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserListsDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toasty.error(getActivity(), "This member is not in the group anymore", Toast.LENGTH_SHORT).show();
                                    reloadRecycler();
                                }
                                else {
                                    mUserListsDatabaseReference.child(memberId).child("isAdmin").setValue(true);
                                    mUserListsDatabaseReference.child(memberId).child("isOrganizer").setValue(false);
                                    reloadRecycler();
                                }
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
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void dismissAdmin(final String memberId, String memberName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setCancelable(true)
                .setTitle("Dismissing admin")
                .setMessage("Do you want to dismiss " + memberName + " from his group admin position?\n\nAdmins can remove members, appoint or dismiss other admins and edit activity info.")
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUserListsDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Toasty.error(getActivity(), "This member is not in the group anymore", Toast.LENGTH_SHORT).show();
                                    reloadRecycler();
                                }
                                else {
                                    mUserListsDatabaseReference.child(memberId).child("isAdmin").setValue(false);
                                    mUserListsDatabaseReference.child(memberId).child("isOrganizer").setValue(false);
                                    reloadRecycler();
                                }
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
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
