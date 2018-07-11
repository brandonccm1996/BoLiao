package com.example.gekpoh.boliao;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersViewHolder>{

    private Context mCtx;
    private ArrayList<UserInformation> membersList;

    public MembersAdapter(Context mCtx, ArrayList<UserInformation> membersList) {
        this.mCtx = mCtx;
        this.membersList = membersList;
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
        UserInformation userInformation = membersList.get(position);

        holder.textViewName.setText(userInformation.getName());
        if (userInformation.getNumRatings() == 0) holder.ratingBar.setRating(0);
        else holder.ratingBar.setRating(userInformation.getSumRating() / userInformation.getNumRatings());

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

        public MembersViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewProPic = itemView.findViewById(R.id.imageViewProPic);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
            buttonRate = itemView.findViewById(R.id.buttonRate);
        }
    }
}
