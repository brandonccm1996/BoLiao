package com.example.gekpoh.boliao;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.AutoTransition;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
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

import es.dmoral.toasty.Toasty;

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
    private DatabaseReference mUsersDatabaseReference;
    private DatabaseReference mDetectDeleteNotifDatabaseReference;

    private FirebaseStorage mFirebaseStorage;

    private Button buttonEdit;
    private Button buttonDelete;
    private Button joinLeaveButton;
    private ConstraintLayout mLayout;
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
        mDetectDeleteNotifDatabaseReference = mFirebaseDatabase.getReference().child("detectDelete").child(groupId);
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mFirebaseStorage = FirebaseStorage.getInstance();

        buttonEdit = getView().findViewById(R.id.btnEditInfo);
        buttonDelete = getView().findViewById(R.id.btnDelete);

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
        joinLeaveButton = getView().findViewById(R.id.joinleaveButton);
        if(args.getBoolean(getString(R.string.InActivityKey))){
            joinLeaveButton.setText(getString(R.string.LeaveButtonLabel));
        }else{
            if(args.getInt(getString(R.string.groupCurrentSizeKey)) == args.getInt(getString(R.string.groupMaxSizeKey))){
                //Maxed out.
                joinLeaveButton.setEnabled(false);
            }
            joinLeaveButton.setText(getString(R.string.JoinButtonLabel));
        }
        joinLeaveButton.setOnClickListener(new View.OnClickListener(){

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
        mLayout = getView().findViewById(R.id.eventInfoLayout);
        mUserListsDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserLists userList = dataSnapshot.child(MainActivity.userUid).getValue(UserLists.class);
                boolean isOrganizer = false;
                if(userList != null) {
                    isAdmin = userList.getIsAdmin();
                    isOrganizer = userList.getIsOrganizer();
                }
                else{
                    //if user is not in the group yet, we will not find its info
                    isAdmin = false;
                }

                if (isAdmin) {
                    buttonEdit.setVisibility(View.VISIBLE);
                    buttonEdit.setEnabled(true);
                }
                if(isOrganizer){
                    buttonDelete.setVisibility(View.VISIBLE);
                    buttonDelete.setEnabled(true);
                }else{
                    joinLeaveButton.setVisibility(View.VISIBLE);
                    joinLeaveButton.setEnabled(true);
                }
                animateButtonsAppear();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //buttonHolder.setTranslationY(buttonHolder.getHeight());

        GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.v(TAG, "gesture detected");
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mLayout);
                if(velocityY < 0){
                    //appear
                    constraintSet.clear(R.id.scrollView2, ConstraintSet.BOTTOM);
                    constraintSet.connect(R.id.scrollView2,ConstraintSet.BOTTOM,R.id.buttonsHolder,ConstraintSet.TOP);
                    constraintSet.clear(R.id.buttonsHolder, ConstraintSet.TOP);
                    //constraintSet.connect(R.id.buttonsHolder, ConstraintSet.TOP, R.id.scrollView2, ConstraintSet.BOTTOM);

                }else{
                    //dissapear
                    constraintSet.clear(R.id.scrollView2, ConstraintSet.BOTTOM);
                    constraintSet.connect(R.id.scrollView2,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM);
                    //constraintSet.clear(R.id.buttonsHolder, ConstraintSet.TOP);
                    constraintSet.connect(R.id.buttonsHolder, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                }
                ChangeBounds autoTransition = new ChangeBounds();
                autoTransition.setDuration(100);
                autoTransition.setInterpolator(new LinearInterpolator());
                TransitionManager.beginDelayedTransition(mLayout);
                constraintSet.applyTo(mLayout);
                return true;
            }
        };
        final GestureDetectorCompat mDetector = new GestureDetectorCompat(getActivity(),listener);
        mLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toasty.error(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
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
                    Toasty.error(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                } else {
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
                                    for (final String memberId : members) {
                                        mJoinedListsDatabaseReference.child(memberId).child(groupId).removeValue();
                                        mUsersDatabaseReference.child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (!memberId.equals(MainActivity.userUid) && dataSnapshot.child("updateNotifEnabled").getValue(Boolean.class)) mDeleteEventNotifDatabaseReference.child(notifId).child(memberId).setValue(true);
                                                mDetectDeleteNotifDatabaseReference.child(notifId).child(memberId).setValue(true);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }

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

                                            mChatsDatabaseReference.removeValue();
                                            mGeofireDatabaseReference.removeValue();
                                            mGroupsDatabaseReference.removeValue();
                                            mUserListsDatabaseReference.removeValue();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    Toasty.success(getActivity(), "Activity deleted", Toast.LENGTH_LONG).show();
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
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    inputToCheck.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.toString().equals("delete")) dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            else dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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

    public void animateButtonsAppear(){
        Log.v(TAG, "gesture detected");
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mLayout);
        //appear
        constraintSet.clear(R.id.scrollView2, ConstraintSet.BOTTOM);
        constraintSet.connect(R.id.scrollView2,ConstraintSet.BOTTOM,R.id.buttonsHolder,ConstraintSet.TOP);
        constraintSet.clear(R.id.buttonsHolder, ConstraintSet.TOP);
        //constraintSet.connect(R.id.buttonsHolder, ConstraintSet.TOP, R.id.scrollView2, ConstraintSet.BOTTOM);

        ChangeBounds autoTransition = new ChangeBounds();
        autoTransition.setDuration(200);
        autoTransition.setInterpolator(new LinearInterpolator());
        TransitionManager.beginDelayedTransition(mLayout);
        constraintSet.applyTo(mLayout);
    }
}
