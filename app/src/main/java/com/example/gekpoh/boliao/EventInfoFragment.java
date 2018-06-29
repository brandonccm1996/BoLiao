package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;

public class EventInfoFragment extends Fragment {
    private final String TAG = "EVENTINFOfragment";
    private Bundle args;
    private eventInfoCallBack mCallBack;
    private TextView nameView, startView, endView, placeView, descriptionView, sizeView;
    private ImageView picView;
    private ImageButton editButton;
    private long mLastClickTime = 0;
    private boolean editing = false;
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
        picView = getView().findViewById(R.id.groupPicView);
        editButton = getView().findViewById(R.id.editDetailsButton);
        if(!mCallBack.isUserOrganizer()){
            editButton.setVisibility(View.INVISIBLE);
            editButton.setEnabled(false);
        }
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallBack.clickEditActivity();
            }
        });
        String photoUrl = args.getString(getString(R.string.groupPhotoUrlKey));
        setImage(photoUrl);
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
        nameView = getView().findViewById(R.id.eventNameView);
        setName(args.getString(getString(R.string.groupNameKey),""));
        sizeView = getView().findViewById(R.id.eventSizeView);
        String sizeViewText = Long.toString(args.getInt(getString(R.string.groupCurrentSizeKey))) +'/' + Long.toString(args.getInt(getString(R.string.groupMaxSizeKey)));
        sizeView.setText(sizeViewText);
        startView = getView().findViewById(R.id.eventStartDateView);
        //Date startDate = new Date(args.getLong(getString(R.string.groupStartKey)));
        //Date endDate = new Date(args.getLong(getString(R.string.groupEndKey)));
        setStartTime(args.getString(getString(R.string.groupStartKey)));
        endView = getView().findViewById(R.id.eventEndDateView);
        setEndTime(args.getString(getString(R.string.groupEndKey)));
        placeView = getView().findViewById(R.id.eventPlaceView);
        setPlace(args.getString(getString(R.string.groupPlaceKey)));
        descriptionView = getView().findViewById(R.id.eventDescriptionView);
        setDescription(args.getString(getString(R.string.groupDescriptionKey)));
    }

    public interface eventInfoCallBack{
        void onJoinLeaveClick();
        boolean isUserOrganizer();
        void clickEditActivity();
    }

    public void updateNumParticipants(long x){
        Bundle args = getArguments();
        String sizeViewText = Long.toString(x) +'/' + Long.toString(args.getInt(getString(R.string.groupMaxSizeKey)));
        sizeView.setText(sizeViewText);
    }

    public void setName(String name){
        nameView.setText(name);
    }


    public void setSize(int maxSize){
        String sizeViewText = Long.toString(args.getInt(getString(R.string.groupCurrentSizeKey))) +'/' + Long.toString(maxSize);
        sizeView.setText(sizeViewText);
    }


    public void setPlace(String place){
        placeView.setText(place);
    }


    public void setStartTime(String time){
        startView.setText(time);
    }


    public void setEndTime(String time){
        endView.setText(time);
    }

    public void setDescription(String description){
        descriptionView.setText(description);
    }

    public void setImage(String photoUrl){
        if (photoUrl == null) {
            picView.setImageResource(R.drawable.profilepic);
        }
        else {
            Glide.with(picView.getContext())
                    .load(photoUrl)
                    .into(picView);
        }
    }
}
