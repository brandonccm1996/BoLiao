package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

public class EventInfoFragment extends Fragment {
    private final String TAG = "EVENTINFOfragment";
    private eventInfoCallBack mCallBack;
    private TextView sizeView;
    private long mLastClickTime = 0;
    private boolean isAdmin;
    private String groupId;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserListsDatabaseReference;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallBack = (eventInfoCallBack)context;
        }catch(ClassCastException e){
            Log.e(TAG,"Need to implement call back");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_info_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Bundle args = getArguments();
        groupId = args.getString("groupId");

        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        mUserListsDatabaseReference = mFirebaseDatabase.getReference().child("userlists").child(groupId);
        mUserListsDatabaseReference.keepSynced(true);
        ImageView picView = getView().findViewById(R.id.groupPicView);
        String photoUrl = args.getString(getString(R.string.groupPhotoUrlKey));
        if (photoUrl == null) {
            picView.setImageResource(R.drawable.profilepic);
        }
        else {
            Glide.with(picView.getContext())
                    .load(photoUrl)
                    .into(picView);
        }
        Button button = getView().findViewById(R.id.joinleaveButton);
        if(args.getBoolean(getString(R.string.InActivityKey))){
            button.setText(getString(R.string.LeaveButtonLabel));
        }else{
            if(args.getInt(getString(R.string.groupCurrentSizeKey)) == args.getInt(getString(R.string.groupMaxSizeKey))){
                //Maxed out.
                button.setEnabled(false);
            }
            button.setText(getString(R.string.JoinButtonLabel));
        }
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //Following code prevent double clicking
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mCallBack.onJoinLeaveClick();
            }
        });

        final Button buttonEdit = getView().findViewById(R.id.btnEditInfo);
        mUserListsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserLists userList = dataSnapshot.child(MainActivity.userUid).getValue(UserLists.class);
                if(userList != null) {
                    isAdmin = userList.getIsAdmin();
                }else{
                    //if user is not in the group yet, we will not find its info
                    isAdmin = false;
                }
                if (isAdmin) buttonEdit.setVisibility(View.VISIBLE);
                else buttonEdit.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toast.makeText(getActivity(), "Please make sure that you have an internet connection", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent startEditActivityIntent = new Intent(getActivity(), EditEventActivity.class);
                    startEditActivityIntent.putExtra("intentBundle", args);
                    startActivity(startEditActivityIntent);
                }
            }
        });

        TextView nameView, startView, endView, placeView, descriptionView;
        nameView = getView().findViewById(R.id.eventNameView);
        nameView.setText(args.getString(getString(R.string.groupNameKey),""));
        sizeView = getView().findViewById(R.id.eventSizeView);
        String sizeViewText = Long.toString(args.getInt(getString(R.string.groupCurrentSizeKey))) +'/' + Long.toString(args.getInt(getString(R.string.groupMaxSizeKey)));
        sizeView.setText(sizeViewText);
        startView = getView().findViewById(R.id.eventStartDateView);
        //Date startDate = new Date(args.getLong(getString(R.string.groupStartKey)));
        //Date endDate = new Date(args.getLong(getString(R.string.groupEndKey)));
        startView.setText(args.getString(getString(R.string.groupStartKey)));
        endView = getView().findViewById(R.id.eventEndDateView);
        endView.setText(args.getString(getString(R.string.groupEndKey)));
        placeView = getView().findViewById(R.id.eventPlaceView);
        placeView.setText(args.getString(getString(R.string.groupPlaceKey)));
        descriptionView = getView().findViewById(R.id.eventDescriptionView);
        descriptionView.setText(args.getString(getString(R.string.groupDescriptionKey)));
    }

    public interface eventInfoCallBack{
        void onJoinLeaveClick();
    }

    public void updateNumParticipants(long x){
        Bundle args = getArguments();
        String sizeViewText = Long.toString(x) +'/' + Long.toString(args.getInt(getString(R.string.groupMaxSizeKey)));
        sizeView.setText(sizeViewText);
    }
}
