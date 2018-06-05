package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SearchGroupFragment extends Fragment {
    private RecyclerView groupView;
    private ArrayList<Group> groupList;
    private GroupRecyclerAdapter adapter;
    private static Fragment sgFragment;
    private final String TAG = "SEARCHGROUPFRAGMENT";

    public static Fragment getInstance(){
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
        groupList = getArguments().getParcelableArrayList(getResources().getString(R.string.searched_groups));
        adapter = new GroupRecyclerAdapter(getActivity(),groupList);
        groupView = getView().findViewById(R.id.groupList);
        groupView.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupView.setAdapter(adapter);
    }
}
