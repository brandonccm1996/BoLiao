package com.example.gekpoh.boliao;

import android.content.Intent;
import android.graphics.Paint;
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

public class SearchGroupFragment extends Fragment implements GroupRecyclerAdapter.GroupTouchCallBack {
    private RecyclerView groupView;
    private GroupRecyclerAdapter adapter;
    private static SearchGroupFragment sgFragment;
    private final ArrayList<Group> searchedgroups = new ArrayList<>();
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener;
    private final String TAG = "SEARCHGROUPFRAGMENT";
    private boolean signedIn = false;
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
        groupView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupView.setAdapter(adapter);
    }

    public void onSignIn() {
        if (signedIn) return;
        signedIn = true;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");
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
        Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
        intent.putExtra(getString(R.string.groupKey), searchedgroups.get(pos));
        startActivity(intent);
    }

    public void reloadList(){
        //Add other filters here
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
        reloadList();
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
}
