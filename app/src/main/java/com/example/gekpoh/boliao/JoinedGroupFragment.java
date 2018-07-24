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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class JoinedGroupFragment extends Fragment implements GroupRecyclerAdapter.GroupTouchCallBack{
    private static HashSet<String> joinedgroupIds = new HashSet<>();
    private static JoinedGroupFragment jgFragment;
    private boolean signedIn = false;
    private Context mContext;
    private SearchInterface searchInterface;
    private RecyclerView groupView;
    private TextView searchActivityText;
    private GroupRecyclerAdapter adapter;
    private ImageButton searchButton;
    private DatabaseReference mGroupDatabaseReference, mJoinedListDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener;
    private final ArrayList<Group> joinedgroups = new ArrayList<>();
    private final String TAG = "JoinedGroupFragment";
    private boolean viewState = false;

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
        try{
            searchInterface = (SearchInterface)context;
        }catch(ClassCastException e){
            Log.e(TAG, "Need to implement search interface");
        }
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
        groupView = getView().findViewById(R.id.groupList);
        searchActivityText = getView().findViewById(R.id.SearchActivitiesTextView);
        searchActivityText.setText("Press the Search button to join new activities");
        TextView noActivityFoundText = getView().findViewById(R.id.noActivitiesTextView);
        noActivityFoundText.setVisibility(View.INVISIBLE);
        searchButton = getView().findViewById(R.id.initialSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInterface.startSearch();
            }
        });
        groupView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if(adapter != null) groupView.setAdapter(adapter);
    }

    public void onSignIn() {
        if (signedIn) return;
        signedIn = true;
        if(adapter == null) {
            Comparator<Group> timeSortComparator = new Comparator<Group>() {
                @Override
                public int compare(Group o1, Group o2) {
                    return o1.getStartDateTime() > o2.getStartDateTime() ? 1 : o1.getStartDateTime() < o2.getStartDateTime() ? -1 : 0;
                }
            };
            adapter = new GroupRecyclerAdapter(this, timeSortComparator);
            if(groupView != null) groupView.setAdapter(adapter);
        }
        mJoinedListDatabaseReference = FirebaseDatabase.getInstance().getReference().child("joinedlists").child(MainActivity.userUid);
        mJoinedListDatabaseReference.keepSynced(true);
        mGroupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");

        Log.d("JGFrag", "OnSignIn");

        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if(group == null)return;
                joinedgroups.add(group);
                adapter.addGroup(group);
                if(!viewState){
                    displayNonEmptyLayout();
                }
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
                        adapter.removeGroup(group);
                        break;
                    }
                }
                if(adapter.isListEmpty()){
                    displayEmptyLayout();
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
        viewState = false;
        displayEmptyLayout();
        joinedgroups.clear();
        adapter.clearList();
        if (mJoinedListDatabaseReference != null && mChildEventListener != null) {
            mJoinedListDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
        JoinedGroupFragment.joinedgroupIds.clear();
    }

    @Override
    public boolean touchGroup(int pos) {
        /*
        if(GroupDetailsActivity.isInstanceCreated()) return;
        Intent intent = new Intent(getContext(), GroupDetailsActivity.class);
        intent.putExtra(getString(R.string.groupKey), joinedgroups.get(pos).getChatId());
        intent.putExtra(getString(R.string.InActivityKey),true);
        intent.putExtra(getString(R.string.TapPositionKey),pos);
        startActivity(intent);
        */
        return true;
    }
    public static boolean alreadyJoinedGroup(String id){
        return joinedgroupIds.contains(id);
    }

    public void updateGroupDetails(Group group, final int pos) {
        if(pos == -1)return;
        if(group != null) {
            joinedgroups.set(pos,group);
            adapter.updateGroup(pos, group);
        }else{
            Toast.makeText(mContext,"For some reason, this activity has been deleted.", Toast.LENGTH_SHORT).show();
            joinedgroups.remove(pos);
            adapter.removeGroupAtPos(pos);
        }
    }

    public void displayEmptyLayout(){
        viewState = false;
        searchButton.setEnabled(true);
        searchButton.setVisibility(View.VISIBLE);
        searchActivityText.setVisibility(View.VISIBLE);
        groupView.setVisibility(View.INVISIBLE);
    }


    public void displayNonEmptyLayout(){
        viewState = true;
        searchButton.setEnabled(false);
        searchButton.setVisibility(View.INVISIBLE);
        searchActivityText.setVisibility(View.INVISIBLE);
        groupView.setVisibility(View.VISIBLE);
    }

    public interface SearchInterface{
        void startSearch();
    }

    @Override
    public void onResume() {
        if(adapter == null || adapter.isListEmpty()){
            displayEmptyLayout();
        }else{
            displayNonEmptyLayout();
        }
        super.onResume();
    }
}
