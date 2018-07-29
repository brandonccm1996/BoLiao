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

import es.dmoral.toasty.Toasty;

public class JoinedGroupFragment extends Fragment implements GroupRecyclerAdapter.GroupTouchCallBack {
    private static HashSet<String> joinedgroupIds = new HashSet<>();
    private static JoinedGroupFragment jgFragment;
    private boolean firstLoad = true;
    private HashSet<String> loadingList = new HashSet<>();
    private boolean doneLoading = true;
    private boolean signedIn = false;
    private Context mContext;
    private SearchInterface searchInterface;
    private RecyclerView groupView;
    private TextView searchActivityText;
    private GroupRecyclerAdapter adapter;
    private ImageButton searchButton;
    private DatabaseReference mGroupDatabaseReference, mJoinedListDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ValueEventListener mValueEventListener, mFirstLoadListener;
    private final ArrayList<Group> joinedgroups = new ArrayList<>();
    private final String TAG = "JoinedGroupFragment";
    private boolean viewState = false, viewCreated = false, displayEmptyLayout = true, displayLater = true;
    private static Group toBeUpdated;
    private static int toBeUpdatedPosition;
    private static String toBeUpdatedId;
    private boolean childListenerAttached = false;
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
        try {
            searchInterface = (SearchInterface) context;
        } catch (ClassCastException e) {
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
        if (adapter != null) groupView.setAdapter(adapter);
        viewCreated = true;
        if(displayLater){
            if(displayEmptyLayout){
                displayEmptyLayout();
            }else{
                displayNonEmptyLayout();
            }
        }
    }

    public void onSignIn() {
        if (signedIn) return;
        signedIn = true;
        if (adapter == null) {
            Comparator<Group> timeSortComparator = new Comparator<Group>() {
                @Override
                public int compare(Group o1, Group o2) {
                    return o1.getStartDateTime() > o2.getStartDateTime() ? 1 : o1.getStartDateTime() < o2.getStartDateTime() ? -1 : 0;
                }
            };
            adapter = new GroupRecyclerAdapter(this, timeSortComparator);
            //adapter = new GroupRecyclerAdapter(this, null);
            if (groupView != null) groupView.setAdapter(adapter);
        }
        mJoinedListDatabaseReference = FirebaseDatabase.getInstance().getReference().child("joinedlists").child(MainActivity.userUid);
        mJoinedListDatabaseReference.keepSynced(true);
        mGroupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");

        Log.d("JGFrag", "OnSignIn");

        mFirstLoadListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v(TAG, "First Load");
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if (((String) data.getValue()).equals("true")) {
                        loadingList.add(data.getKey());
                        joinedgroupIds.add(data.getKey());
                        DatabaseReference ref = mGroupDatabaseReference.child(data.getKey());
                        ref.keepSynced(true);
                        ref.addListenerForSingleValueEvent(mValueEventListener);
                    }
                }
                doneLoading = true;
                if(doneLoading && loadingList.isEmpty()) {
                    MainActivity.needToLoadTimeNotification = false;
                    if(!childListenerAttached){
                        mJoinedListDatabaseReference.addChildEventListener(mChildEventListener);
                        childListenerAttached = true;
                    }
                    if(!viewState && !joinedgroups.isEmpty()){
                        displayNonEmptyLayout();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if(loadingList.contains(dataSnapshot.getKey())){
                    loadingList.remove(dataSnapshot.getKey());
                }
                if (group == null){
                    joinedgroupIds.remove(dataSnapshot.getKey());
                }else{
                    joinedgroups.add(group);
                    adapter.addGroup(group);
                    if(MainActivity.needToLoadTimeNotification){
                        TimeNotificationScheduler.setNewReminder(getActivity(), group.getChatId(),group.getNames(),group.getStartDateTime(),TimeNotificationScheduler.DELAY_2HRS);
                    }
                }
                if(doneLoading && loadingList.isEmpty()) {
                    MainActivity.needToLoadTimeNotification = false;
                    if(!viewState && !joinedgroups.isEmpty()){
                        displayNonEmptyLayout();
                    }
                    if(!childListenerAttached){
                        mJoinedListDatabaseReference.addChildEventListener(mChildEventListener);
                        childListenerAttached = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(joinedgroupIds.contains(dataSnapshot.getKey()))return;
                if (((String) dataSnapshot.getValue()).equals("true")) {
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
                for (Group group : joinedgroups) {
                    if (group.getChatId().equals(id)) {
                        joinedgroups.remove(group);
                        adapter.removeGroup(group);
                        break;
                    }
                }
                if (adapter.isListEmpty()) {
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
        doneLoading = false;
        mJoinedListDatabaseReference.addListenerForSingleValueEvent(mFirstLoadListener);
    }

    public void onSignOut() {
        if (!signedIn) return;
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

    public static boolean alreadyJoinedGroup(String id) {
        return joinedgroupIds.contains(id);
    }

    public void removeFromList(String id) {
        for (Group group : joinedgroups) {
            if (group.getChatId().equals(id)) {
                joinedgroups.remove(group);
                adapter.removeGroup(group);
                //adapter.notifyDataSetChanged();
                if (adapter.isListEmpty()) displayEmptyLayout();
                break;
            }
        }
    }

    public void updateGroupDetails(String id, Group group, final int pos) {
        /*Log.v(TAG, "Updating group details in rv");
        if (pos == -1) return;
        if (group != null) {
            Group grp2 = adapter.getGroupAtPos(pos);
            if (grp2 != null && grp2.getChatId().equals(group.getChatId())) {
                for (int x = 0; x < joinedgroups.size(); x++) {
                    if (joinedgroups.get(x).getChatId() == group.getChatId()) {
                        adapter.updateGroup(pos, group);
                        joinedgroups.set(x, group);
                    }
                }
            }
        } else {
            Toasty.error(mContext, "For some reason, this activity has been deleted.", Toast.LENGTH_SHORT).show();
            removeFromList(id);
        }*/
        toBeUpdated = group;
        toBeUpdatedPosition = pos;
        toBeUpdatedId = id;
    }

    public void displayEmptyLayout() {
        if(viewCreated == false){
            displayLater = true;
            displayEmptyLayout = true;
            return;
        }
        displayLater = false;
        searchButton.setEnabled(true);
        searchButton.setVisibility(View.VISIBLE);
        searchActivityText.setVisibility(View.VISIBLE);
        groupView.setVisibility(View.INVISIBLE);
        viewState = false;
    }


    public void displayNonEmptyLayout() {
        if(viewCreated == false){
            displayLater = true;
            displayEmptyLayout = false;
            return;
        }
        displayLater = false;
        searchButton.setEnabled(false);
        searchButton.setVisibility(View.INVISIBLE);
        searchActivityText.setVisibility(View.INVISIBLE);
        groupView.setVisibility(View.VISIBLE);
        viewState = true;
    }

    public interface SearchInterface {
        void startSearch();
    }

    @Override
    public void onResume() {
        Log.v(TAG,"On resumed called");
        if (adapter == null || adapter.isListEmpty()) {
            displayEmptyLayout();
        } else {
            if(toBeUpdated != null){
                updateGroupDetailsOnResume(toBeUpdatedId,toBeUpdated,toBeUpdatedPosition);
            }
            displayNonEmptyLayout();
        }
        super.onResume();
    }

    public void updateGroupDetailsOnResume(String id, Group group, final int pos){
        Log.v(TAG, Integer.toString(pos));
        if (pos == -1) return;
        if (group != null) {
            Group grp2 = adapter.getGroupAtPos(pos);
            Log.v(TAG, grp2.getNames());
            if (grp2 != null && grp2.getChatId().equals(group.getChatId())) {
                for (int x = 0; x < joinedgroups.size(); x++) {
                    if (joinedgroups.get(x).getChatId() == group.getChatId()) {
                        Log.v(TAG, "adding to adapter");
                        adapter.updateGroup(pos, group);
                        joinedgroups.set(x, group);
                        break;
                    }
                }
            }
        } else {
            Toasty.error(mContext, "For some reason, this activity has been deleted.", Toast.LENGTH_SHORT).show();
            removeFromList(id);
        }
        toBeUpdated = null;
    }
}
