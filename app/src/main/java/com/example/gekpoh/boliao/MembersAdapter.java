package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

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
        UserInformation2 userInformation2 = membersList.get(position);

        holder.textViewName.setText(userInformation2.getUserInformation().getName());
        if (userInformation2.getUserInformation().getNumRatings() == 0) holder.ratingBar.setRating(0);
        else holder.ratingBar.setRating(userInformation2.getUserInformation().getSumRating() / userInformation2.getUserInformation().getNumRatings());
        if (userInformation2.getUserInformation().getPhotoUrl().equals("")) holder.imageViewProPic.setImageResource(R.drawable.profilepic);
        else Glide.with(holder.imageViewProPic.getContext())
                    .load(userInformation2.getUserInformation().getPhotoUrl())
                    .into(holder.imageViewProPic);

        if (userInformation2.getEnableRemove()) holder.buttonRemove.setVisibility(View.VISIBLE);
        else holder.buttonRemove.setVisibility(View.INVISIBLE);
        if (userInformation2.getMemberIsAdmin()) holder.textViewAdmin.setVisibility(View.VISIBLE);
        else holder.textViewAdmin.setVisibility(View.INVISIBLE);
        if (userInformation2.getUserId().equals(MainActivity.userUid)) holder.buttonRate.setVisibility(View.INVISIBLE);
        else holder.buttonRate.setVisibility(View.VISIBLE);

        holder.textViewDummy.setText(userInformation2.getUserId());
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
        private TextView textViewDummy;

        public MembersViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewProPic = itemView.findViewById(R.id.imageViewProPic);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            textViewAdmin = itemView.findViewById(R.id.textViewAdmin);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
            buttonRate = itemView.findViewById(R.id.buttonRate);
            textViewDummy = itemView.findViewById(R.id.textViewDummy);

            buttonRate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    membersFragment.rateMember(textViewDummy.getText().toString(), textViewName.getText().toString());
                    Log.d("MembersAdapter", textViewName.getText().toString());
                }
            });

            buttonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    membersFragment.removeMember(textViewDummy.getText().toString(), textViewName.getText().toString());
                    Log.d("MembersAdapter2", textViewDummy.getText().toString());
                    Log.d("MembersAdapter2", textViewName.getText().toString());
                }
            });
        }
    }
}
