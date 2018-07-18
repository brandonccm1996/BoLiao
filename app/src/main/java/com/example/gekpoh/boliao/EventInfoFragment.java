package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EventInfoFragment extends Fragment {
    private final String TAG = "EVENTINFOfragment";
    private eventInfoCallBack mCallBack;
    private TextView sizeView,nameView, startView, endView, placeView, descriptionView;
    private ImageView picView;
    private long mLastClickTime = 0;
    private boolean isAdmin;
    private String groupId;
    private String photoUrl;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserListsDatabaseReference;
    private DatabaseReference mGroupsDatabaseReference;
    private DatabaseReference mGeofireDatabaseReference;
    private DatabaseReference mJoinedListsDatabaseReference;
    private DatabaseReference mChatsDatabaseReference;
    private DatabaseReference mDeleteEventNotifDatabaseReference;

    private FirebaseStorage mFirebaseStorage;

    private Button buttonEdit;
    private Button buttonDelete;

    private ArrayList<String> members;

    private Bundle args;

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
        args = getArguments();
        groupId = args.getString("groupId");
        Log.d("EventInfoFrag", "reloading" + args.getString("eventplace"));

        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        mUserListsDatabaseReference = mFirebaseDatabase.getReference().child("userlists").child(groupId);
        mUserListsDatabaseReference.keepSynced(true);
        mGroupsDatabaseReference = mFirebaseDatabase.getReference().child("groups").child(groupId);
        mGeofireDatabaseReference = mFirebaseDatabase.getReference().child("geoFireObjects").child(groupId);
        mChatsDatabaseReference = mFirebaseDatabase.getReference().child("chats").child(groupId);
        mJoinedListsDatabaseReference = mFirebaseDatabase.getReference().child("joinedlists");
        mDeleteEventNotifDatabaseReference = mFirebaseDatabase.getReference().child("deleteEventNotif").child(groupId).child(args.getString("eventname"));
        mFirebaseStorage = FirebaseStorage.getInstance();

        buttonEdit = getView().findViewById(R.id.btnEditInfo);
        buttonDelete = getView().findViewById(R.id.btnDelete);
        buttonEdit.setVisibility(View.INVISIBLE);
        buttonDelete.setVisibility(View.INVISIBLE);

        picView = getView().findViewById(R.id.groupPicView);
        photoUrl = args.getString(getString(R.string.groupPhotoUrlKey));
        if (photoUrl == null) {
            Glide.with(picView.getContext())
                    .load(R.drawable.profilepic)
                    .apply(RequestOptions.circleCropTransform())
                    .into(picView);
        }
        else {
            Glide.with(picView.getContext())
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
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

        mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserLists userList = dataSnapshot.child(MainActivity.userUid).getValue(UserLists.class);
                if(userList != null) {
                    isAdmin = userList.getIsAdmin();
                }
                else{
                    //if user is not in the group yet, we will not find its info
                    isAdmin = false;
                }

                if (isAdmin) {
                    buttonEdit.setVisibility(View.VISIBLE);
                    buttonDelete.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent startEditActivityIntent = new Intent(getActivity(), EditEventActivity.class);
                    startEditActivityIntent.putExtra("intentBundle", args);
                    startActivity(startEditActivityIntent);
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    members = new ArrayList<>();
                    mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                members.add(childSnapshot.getKey());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    final EditText inputToCheck = new EditText(getActivity());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    inputToCheck.setLayoutParams(lp);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setCancelable(true)
                            .setTitle("Deleting activity")
                            .setMessage("Are you sure you want to delete this activity? Activity deletion is not reversible.\nTo confirm, type in: 'delete'")
                            .setView(inputToCheck)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                final String notifId = mDeleteEventNotifDatabaseReference.push().getKey();

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // notification
                                    Map userList = new HashMap();
                                    for (String memberId : members) {
                                        if (!memberId.equals(MainActivity.userUid)) userList.put(memberId, true); // don't send notification to the person deleting event
                                        mJoinedListsDatabaseReference.child(memberId).child(groupId).removeValue();
                                        Log.d("EventInfoFrag", memberId);
                                    }
                                    mDeleteEventNotifDatabaseReference.child(notifId).setValue(userList);

                                    // delete chat photos
                                    mChatsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                                if (childSnapshot.hasChild("photoUrl")) {
                                                    StorageReference photoDeleteRef = mFirebaseStorage.getReferenceFromUrl(childSnapshot.child("photoUrl").getValue().toString());
                                                    photoDeleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("EventInfoFrag", "Deletion working");
                                                        }
                                                    });
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    // delete group photo
                                    mGroupsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("photoUrl")) {
                                                StorageReference photoDeleteRef2 = mFirebaseStorage.getReferenceFromUrl(dataSnapshot.child("photoUrl").getValue().toString());
                                                photoDeleteRef2.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("EventInfoFrag2", "Deletion working 2");
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    mChatsDatabaseReference.removeValue();
                                    mGeofireDatabaseReference.removeValue();
                                    mUserListsDatabaseReference.removeValue();
                                    mGroupsDatabaseReference.removeValue();
                                    Toast.makeText(getActivity(), "Activity deleted", Toast.LENGTH_LONG).show();
                                    getActivity().onBackPressed();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                    final AlertDialog dialog = builder.create();
                    dialog.show();
                    ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    inputToCheck.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.toString().equals("delete")) ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            else ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                    });
                }
            }
        });


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
    public void setEndDate(String endTime, String endTime2){
        endView.setText(endTime);
        args.putString(getString(R.string.groupEndKey), endTime);
        args.putString("eventend2", endTime2);   // using dd/MM/yyyy hh:mma
    }
    public void setStartDate(String startTime, String startTime2){
        startView.setText(startTime);
        args.putString(getString(R.string.groupStartKey), startTime);
        args.putString("eventstart2", startTime2);   // using dd/MM/yyyy hh:mma
    }
    public void setActivityName(String name){
        nameView.setText(name);
        args.putString(getString(R.string.groupNameKey), name);
    }
    public void setParticipantsText(int numParticipants, int maxParticipants){
        String sizeViewText = Long.toString(numParticipants) +'/' + Long.toString(maxParticipants);
        sizeView.setText(sizeViewText);
        args.putInt(getString(R.string.groupCurrentSizeKey), numParticipants);
        args.putInt(getString(R.string.groupMaxSizeKey), maxParticipants);
    }
    public void setPlaceName(String placeName){
        placeView.setText(placeName);
        args.putString(getString(R.string.groupPlaceKey), placeName);
    }
    public void setDescription(String description){
        descriptionView.setText(description);
        args.putString(getString(R.string.groupDescriptionKey), description);
    }
    public void setPhoto(String photoUrl){
        if (photoUrl == null) {
            Glide.with(picView.getContext())
                    .load(R.drawable.profilepic)
                    .apply(RequestOptions.circleCropTransform())
                    .into(picView);
        }
        else {
            Glide.with(picView.getContext())
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(picView);
        }
        args.putString(getString(R.string.groupPhotoUrlKey), photoUrl);
    }

    public void setPlaceId(String placeId){
        args.putString("placeId", placeId);
    }
}
