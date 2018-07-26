package com.example.gekpoh.boliao;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import es.dmoral.toasty.Toasty;

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
    public void onBindViewHolder(@NonNull final MembersViewHolder holder, int position) {
        final UserInformation2 userInformation2 = membersList.get(position);

        holder.textViewName.setText(userInformation2.getUserInformation().getName().length() > 10 ? userInformation2.getUserInformation().getName().substring(0, 9) + "..." : userInformation2.getUserInformation().getName());

        if (userInformation2.getUserRating().getNumRatings() == 0) holder.ratingBar.setRating(0);
        else holder.ratingBar.setRating(userInformation2.getUserRating().getSumRating() / userInformation2.getUserRating().getNumRatings());

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

        final String memberStatus = userInformation2.getMemberStatus();
        final String userStatus = userInformation2.getUserStatus();

        // member status text view
        if (memberStatus.equals("organizer")) {
            holder.textViewMemberStatus.setVisibility(View.VISIBLE);
            holder.textViewMemberStatus.setText("Organizer");
        }
        else if (memberStatus.equals("admin")) {
            holder.textViewMemberStatus.setVisibility(View.VISIBLE);
            holder.textViewMemberStatus.setText("Admin");
        }
        else holder.textViewMemberStatus.setVisibility(View.INVISIBLE);

        if (userInformation2.getMemberId().equals(MainActivity.userUid)) {
            holder.membersCardView.setCardBackgroundColor(mCtx.getResources().getColor(R.color.colorLightBlue));
        }
        else holder.membersCardView.setCardBackgroundColor(mCtx.getResources().getColor(R.color.white_background));

        // user actions
        holder.membersCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // non member looking at cards or user looking at his own card
                if (!userInformation2.getInEvent() || userInformation2.getMemberId().equals(MainActivity.userUid)) {    // non member looking at cards or user looking at his own card
                    AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                            .setCancelable(true)
                            .setItems(R.array.memberActionsOwnCardOrNonMember, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                        Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                    }
                                    else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                // member not rated before
                else if (!userInformation2.getMemberRatedBefore()) {
                    if (userStatus.equals("member") || (userStatus.equals("admin") && memberStatus.equals("organizer"))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                                .setCancelable(true)
                                .setItems(R.array.memberActionsMemberToOthersOrAdminToOrganizer, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                            Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                        else if (which == 1) membersFragment.rateMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else if ((userStatus.equals("admin") || userStatus.equals("organizer")) && memberStatus.equals("member")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                                .setCancelable(true)
                                .setItems(R.array.memberActionsAdminToMember, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                            Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                        else if (which == 1) membersFragment.rateMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 2) membersFragment.removeMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 3) membersFragment.appointAdmin(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else if ((userStatus.equals("admin") || userStatus.equals("organizer")) && memberStatus.equals("admin")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                                .setCancelable(true)
                                .setItems(R.array.memberActionsAdminToAdmin, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                            Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                        else if (which == 1) membersFragment.rateMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 2) membersFragment.removeMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 3) membersFragment.dismissAdmin(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }

                // member rated before
                else if (userInformation2.getMemberRatedBefore()) {
                    if (userStatus.equals("member") || (userStatus.equals("admin") && memberStatus.equals("organizer"))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                                .setCancelable(true)
                                .setItems(R.array.memberActionsMemberToOthersOrAdminToOrganizer2, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                            Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                        else if (which == 1) membersFragment.rateMember2(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else if ((userStatus.equals("admin") || userStatus.equals("organizer")) && memberStatus.equals("member")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                                .setCancelable(true)
                                .setItems(R.array.memberActionsAdminToMember2, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                            Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                        else if (which == 1) membersFragment.rateMember2(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 2) membersFragment.removeMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 3) membersFragment.appointAdmin(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else if ((userStatus.equals("admin") || userStatus.equals("organizer")) && memberStatus.equals("admin")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx)
                                .setCancelable(true)
                                .setItems(R.array.memberActionsAdminToAdmin2, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!FirebaseDatabaseUtils.connectedToDatabase()) {
                                            Toasty.error(mCtx, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                        else if (which == 0) putExtrasForIntent(holder, userInformation2);
                                        else if (which == 1) membersFragment.rateMember2(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 2) membersFragment.removeMember(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                        else if (which == 3) membersFragment.dismissAdmin(userInformation2.getMemberId(), userInformation2.getUserInformation().getName());
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });
    }

    private void putExtrasForIntent(MembersViewHolder holder, UserInformation2 userInformation2) {
        Intent intent = new Intent(mCtx, ViewProfileActivity.class);
        intent.putExtra("memberName", userInformation2.getUserInformation().getName());
        intent.putExtra("memberDesc", userInformation2.getUserInformation().getDescription());
        intent.putExtra("memberSumRating", userInformation2.getUserRating().getSumRating());
        intent.putExtra("memberNumRatings", userInformation2.getUserRating().getNumRatings());
        intent.putExtra("memberProPic", userInformation2.getUserInformation().getPhotoUrl());
        mCtx.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    public class MembersViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewName;
        private ImageView imageViewProPic;
        private RatingBar ratingBar;
        private TextView textViewMemberStatus;
        private CardView membersCardView;

        public MembersViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewProPic = itemView.findViewById(R.id.imageViewProPic);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            textViewMemberStatus = itemView.findViewById(R.id.textViewMemberStatus);
            membersCardView = itemView.findViewById(R.id.membersCardView);
        }
    }
}
