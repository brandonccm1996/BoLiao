package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;

public class EventInfoFragment extends Fragment {
    private final String TAG = "EVENTINFOfragment";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_info_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        ImageView picView = getView().findViewById(R.id.groupPicView);
        String photoUrl = args.getString(getString(R.string.groupPhotoUrlKey));
        if (photoUrl.equals("")) {
            picView.setImageResource(R.drawable.profilepic);
        }
        else {
            Glide.with(picView.getContext())
                    .load(photoUrl)
                    .into(picView);
        }
        TextView nameView, startView, endView, placeView, descriptionView, sizeView;
        nameView = getView().findViewById(R.id.eventNameView);
        nameView.setText(args.getString(getString(R.string.groupNameKey),""));
        sizeView = getView().findViewById(R.id.eventSizeView);
        String sizeViewText = Integer.toString(args.getInt(getString(R.string.groupCurrentSizeKey))) +'/' + Integer.toString(args.getInt(getString(R.string.groupMaxSizeKey)));
        sizeView.setText(sizeViewText);
        startView = getView().findViewById(R.id.eventStartDateView);
        Date startDate = new Date(args.getLong(getString(R.string.groupStartKey)));
        Date endDate = new Date(args.getLong(getString(R.string.groupEndKey)));
        startView.setText(Group.groupDateFormatter.format(startDate));
        endView = getView().findViewById(R.id.eventEndDateView);
        endView.setText(Group.groupDateFormatter.format(endDate));
        placeView = getView().findViewById(R.id.eventPlaceView);
        placeView.setText(args.getString(getString(R.string.groupPlaceKey)));
        descriptionView = getView().findViewById(R.id.eventDescriptionView);
        descriptionView.setText(args.getString(getString(R.string.groupDescriptionKey)));
    }
}
