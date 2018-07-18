package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder>{

    private Context mCtx;
    private ArrayList<UserInformation2> membersList;
    private MembersFragment membersFragment;

    public MembersAdapter(Context mCtx, ArrayList<UserInformation2> membersList, MembersFragment membersFragment) {
        this.mCtx = mCtx;
        this.membersList = membersList;
        this.membersFragment = membersFragment;
    }

    @NonNull
    @Override
    public MembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.members_card_layout, parent, false);
        return new MembersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MembersViewHolder holder, int position) {
        final UserInformation2 userInformation2 = membersList.get(position);

        holder.textViewName.setText(userInformation2.getUserInformation().getName());

        if (userInformation2.getUserInformation().getNumRatings() == 0) holder.ratingBar.setRating(0);
        else holder.ratingBar.setRating(userInformation2.getUserInformation().getSumRating() / userInformation2.getUserInformation().getNumRatings());

        if (userInformation2.getUserInformation().getPhotoUrl().equals("")) {
            Glide.with(holder.imageViewProPic.getContext())
                    .load(R.drawable.profilepic)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.imageViewProPic);
        }
        else Glide.with(holder.imageViewProPic.getContext())
                    .load(userInformation2.getUserInformation().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.imageViewProPic);

        if (userInformation2.getMemberIsAdmin()) holder.textViewAdmin.setVisibility(View.VISIBLE);
        else holder.textViewAdmin.setVisibility(View.INVISIBLE);

        if (!userInformation2.getInEvent()) {
            holder.buttonRemove.setVisibility(View.INVISIBLE);
            holder.buttonRate.setVisibility(View.INVISIBLE);
        }
        else {
            if (userInformation2.getEnableRemove()) holder.buttonRemove.setVisibility(View.VISIBLE);
            else holder.buttonRemove.setVisibility(View.INVISIBLE);

            if (userInformation2.getUserId().equals(MainActivity.userUid))
                holder.buttonRate.setVisibility(View.INVISIBLE);
            else holder.buttonRate.setVisibility(View.VISIBLE);

            if (userInformation2.getMemberRatedBefore()) {
                holder.buttonRate.getBackground().setColorFilter(ContextCompat.getColor(mCtx, R.color.colorLime), PorterDuff.Mode.MULTIPLY);
                holder.buttonRate.setText("Rated");
                holder.buttonRate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                            Toast.makeText(mCtx, "Please make sure that you have an internet connection", Toast.LENGTH_LONG).show();
                        }
                        else membersFragment.rateMember2(userInformation2.getUserId(), userInformation2.getUserInformation().getName());
                    }
                });
            } else {
                holder.buttonRate.getBackground().clearColorFilter();
                holder.buttonRate.setText("Rate");
                holder.buttonRate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                            Toast.makeText(mCtx, "Please make sure that you have an internet connection", Toast.LENGTH_LONG).show();
                        }
                        else membersFragment.rateMember(userInformation2.getUserId(), userInformation2.getUserInformation().getName());
                    }
                });
            }

            holder.buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                        Toast.makeText(mCtx, "Please make sure that you have an internet connection", Toast.LENGTH_LONG).show();
                    }
                    else membersFragment.removeMember(userInformation2.getUserId(), userInformation2.getUserInformation().getName());
                }
            });
        }

        holder.membersCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCtx, ViewProfileActivity.class);
                intent.putExtra("memberName", userInformation2.getUserInformation().getName());
                intent.putExtra("memberDesc", userInformation2.getUserInformation().getDescription());
                intent.putExtra("memberSumRating", userInformation2.getUserInformation().getSumRating());
                intent.putExtra("memberNumRatings", userInformation2.getUserInformation().getNumRatings());
                intent.putExtra("memberProPic", userInformation2.getUserInformation().getPhotoUrl());
                mCtx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    public class MembersViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewName;
        private ImageView imageViewProPic;
        private RatingBar ratingBar;
        private Button buttonRemove;
        private Button buttonRate;
        private TextView textViewAdmin;
        private CardView membersCardView;

        public MembersViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewProPic = itemView.findViewById(R.id.imageViewProPic);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            textViewAdmin = itemView.findViewById(R.id.textViewAdmin);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
            buttonRate = itemView.findViewById(R.id.buttonRate);
            membersCardView = itemView.findViewById(R.id.membersCardView);
        }
    }
}
