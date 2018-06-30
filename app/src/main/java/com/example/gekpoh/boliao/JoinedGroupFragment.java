package com.example.gekpoh.boliao;


import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;

public class JoinedGroupFragment extends Fragment implements GroupRecyclerAdapter.GroupTouchCallBack{
    private static HashSet<String> joinedgroupIds = new HashSet<>();
    private static JoinedGroupFragment jgFragment;
    private boolean signedIn = false;
    private Context mContext;
    private RecyclerView groupView;
    private GroupRecyclerAdapter adapter;
    private DatabaseReference mGroupDatabaseReference, mJoinedListDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;
    private final ArrayList<Group> joinedgroups = new ArrayList<>();
    private final String TAG = "JoinedGroupFragment";

    public static JoinedGroupFragment getInstance() {
        if (jgFragment == null) {
            jgFragment = new JoinedGroupFragment();
        }
        return jgFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
        adapter = new GroupRecyclerAdapter(this, joinedgroups);
        groupView = getView().findViewById(R.id.groupList);
        groupView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupView.setAdapter(adapter);
    }

    public void onSignIn() {
        if (signedIn) return;
        signedIn = true;
        mJoinedListDatabaseReference = FirebaseDatabase.getInstance().getReference().child("joinedlists").child(MainActivity.userUid);
        mJoinedListDatabaseReference.keepSynced(true);
        mGroupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if(group == null)return;
                joinedgroups.add(group);
                adapter.notifyDataSetChanged();
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
                    joinedgroupIds.add(dataSnapshot.getKey());
                    DatabaseReference ref = mGroupDatabaseReference.child(dataSnapshot.getKey());
                    ref.keepSynced(true);
                    ref.addListenerForSingleValueEvent(mValueEventListener);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                for(Group group: joinedgroups){
                    if(group.getChatId().equals(id)){
                        joinedgroups.remove(group);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                joinedgroupIds.remove(id);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mJoinedListDatabaseReference.addChildEventListener(mChildEventListener);
    }

    public void onSignOut() {
        if(!signedIn) return;
        signedIn = false;
        joinedgroups.clear();
        adapter.notifyDataSetChanged();
        if (mJoinedListDatabaseReference != null && mChildEventListener != null) {
            mJoinedListDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        JoinedGroupFragment.joinedgroupIds.clear();
    }

    @Override
    public void touchGroup(int pos) {
        if(GroupDetailsActivity.isInstanceCreated()) return;
        Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
        intent.putExtra(getString(R.string.groupKey), joinedgroups.get(pos).getChatId());
        intent.putExtra(getString(R.string.InActivityKey),true);
        intent.putExtra(getString(R.string.TapPositionKey),pos);
        startActivity(intent);
    }
    public static boolean alreadyJoinedGroup(String id){
        return joinedgroupIds.contains(id);
    }

    public void updateGroupDetails(Group group, final int pos) {
        if(pos == -1)return;
        if(group != null) {
            joinedgroups.set(pos, group);
        }else{
            Toast.makeText(mContext,"For some reason, this activity has been deleted.", Toast.LENGTH_SHORT).show();
            joinedgroups.remove(pos);
        }
    }
}
