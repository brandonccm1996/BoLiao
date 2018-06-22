package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
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

public class SearchGroupFragment extends Fragment implements GroupRecyclerAdapter.GroupTouchCallBack {
    private RecyclerView groupView;
    private GroupRecyclerAdapter adapter;
    private static SearchGroupFragment sgFragment;
    private final ArrayList<Group> searchedgroups = new ArrayList<>();
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener;
    private final String TAG = "SEARCHGROUPFRAGMENT";
    private boolean signedIn = false;
    private long reloadTimer = 0;
    public static SearchGroupFragment getInstance(){
        if(sgFragment == null){
            sgFragment = new SearchGroupFragment();
        }
        return sgFragment;
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
        //groupList = getArguments().getParcelableArrayList(getResources().getString(R.string.searched_groups));
        adapter = new GroupRecyclerAdapter(this,searchedgroups);
        groupView = getView().findViewById(R.id.groupList);
        groupView.setLayoutManager(new reloadLayoutManager(getActivity()));
        groupView.setAdapter(adapter);
    }

    public void onSignIn() {
        if (signedIn) return;
        signedIn = true;
        mDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("groups");
        mDatabaseReference.keepSynced(true);
        //reloadList();
    }

    public void onSignOut() {
        if(!signedIn) return;
        signedIn = false;
        searchedgroups.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void touchGroup(int pos) {
        if(GroupDetailsActivity.isInstanceCreated()) return;
        Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
        intent.putExtra(getString(R.string.groupKey), searchedgroups.get(pos).getChatId());
        startActivity(intent);
    }

    public void reloadList(){
        //Add other filters here
        if(SystemClock.elapsedRealtime() - reloadTimer < 2000) return;//Can only reload once every 2 second
        reloadTimer = SystemClock.elapsedRealtime();
        searchedgroups.clear();
        if(mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if(JoinedGroupFragment.alreadyJoinedGroup(data.getKey()))continue;
                        Log.v(TAG, "NEW GROUP ADDED");
                        Group group = data.getValue(Group.class);
                        searchedgroups.add(group);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        mDatabaseReference.addListenerForSingleValueEvent(mValueEventListener);
    }

    //automatically reloads on every swipe
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //reloadList();
    }

    public void removeFromList(String id){
        for(Group group: searchedgroups){
            if(group.getChatId().equals(id)){
                searchedgroups.remove(group);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void updateGroupDetails(Group group, final int pos) {
        if(pos == -1)return;
        searchedgroups.set(pos,group);
    }

    private class reloadLayoutManager extends LinearLayoutManager{
        public reloadLayoutManager(Context context){
            super(context);
        }

        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            int scrollRange = super.scrollVerticallyBy(dy, recycler, state);
            int overscroll = dy - scrollRange;
            if (overscroll < -150) {//any value lesser than 0 is overscroll
                // top overscroll
                reloadList();
            }
            return scrollRange;
        }
    }
}
