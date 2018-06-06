package com.example.gekpoh.boliao;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.GroupViewHolder> {
    private Context mContext;
    private ArrayList<Group> groupList;
    private final String TAG = "GROUPRECYCLERADAPTER";
    public GroupRecyclerAdapter(Context context, ArrayList<Group> groups){
        mContext = context;
        groupList = groups;
    }
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        GroupViewHolder groupViewHolder = new GroupViewHolder(itemView);
        return groupViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        final Group group = groupList.get(position);
        String groupName = group.getName();
        holder.mGroupName.setText(groupName.length() > 10? groupName.substring(0,9) + "...":groupName);
        holder.mGroupDate.setText(group.getStartDate());//Possible Improvement: Indicate how much time left until the activity, Happening now, Over
        String placeName = group.getPlace();
        holder.mGroupPlace.setText(placeName.length() > 10? placeName.substring(0,9) + "...":placeName);
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),String.format("%s clicked", group.getName()),Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(view.getContext(), GroupDetailsActivity.class);
                //Put extra data into intent in the form of parcelable
                //++CAN ADD TRANSITIONS TO NEXT ACTIVITY HERE
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        public TextView mGroupName, mGroupDate, mGroupPlace;
        public CardView mCardView;
        public ImageView mImageView;
        public GroupViewHolder(View itemView) {
            super(itemView);
            mGroupName = itemView.findViewById(R.id.cardNoteTitle);
            mGroupPlace = itemView.findViewById(R.id.cardNotePlace);
            mCardView = itemView.findViewById(R.id.cardViewNote);
            mImageView = itemView.findViewById(R.id.cardNoteIcon);
            mGroupDate = itemView.findViewById(R.id.cardNoteDate);
            //Include date + people count next time
        }
    }
}
