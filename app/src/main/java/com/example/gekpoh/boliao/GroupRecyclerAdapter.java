package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Comparator;

import es.dmoral.toasty.Toasty;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.GroupViewHolder> {
    private GroupTouchCallBack mCallBack;
    private SortedList<Group> groupList;
    private final String TAG = "GROUPRECYCLERADAPTER";
    public GroupRecyclerAdapter(GroupTouchCallBack callback, final Comparator<Group> comparator){
        mCallBack = callback;
        groupList = new SortedList<Group>(Group.class, new SortedList.Callback<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                if(comparator == null) return 0;
                return comparator.compare(o1,o2);
            }

            @Override
            public void onChanged(int position, int count) {
                Log.v(TAG, "onChanged");
                notifyItemRangeChanged(position, count);
                //notifyDataSetChanged();
            }

            @Override
            public boolean areContentsTheSame(Group oldItem, Group newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Group item1, Group item2) {
                return item1.getChatId().equals(item2.getChatId());
            }

            @Override
            public void onInserted(int position, int count) {
                Log.v(TAG, "onInserted");
                notifyItemRangeInserted(position,count);
                notifyItemRangeChanged(position, groupList.size() - position);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.v(TAG, "onreMoved");
                notifyItemRangeRemoved(position,count);
                notifyItemRangeChanged(position, groupList.size() - position);
                //notifyDataSetChanged();
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.v(TAG, "onMoved");
                //notifyDataSetChanged();
                notifyItemMoved(fromPosition, toPosition);
                Log.v(TAG, Integer.toString(fromPosition));
                Log.v(TAG, Integer.toString(toPosition));
                if(fromPosition > toPosition){
                    notifyItemRangeChanged(toPosition, fromPosition - toPosition + 1);
                }else{
                    notifyItemRangeChanged(fromPosition, toPosition - fromPosition + 1);
                }
            }
        });
    }
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new GroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group group = groupList.get(position);
        final int mPosition = position;
        String groupName = group.getNames();
        //holder.mGroupName.setText(groupName.length() > 15? groupName.substring(0,14) + "...":groupName);
        holder.mGroupName.setText(groupName);
        holder.mGroupDate.setText(group.getStartDateTimeString());//Possible Improvement: Indicate how much time left until the activity, Happening now, Over
        String placeName = group.getLocation();
        //holder.mGroupPlace.setText(placeName.length() > 15? placeName.substring(0,14) + "...":placeName);
        holder.mGroupPlace.setText(placeName);
        String participantsString = "Participants: " + group.getNumParticipants();
        holder.mGroupParticipants.setText(participantsString);
        if(group.getPhotoUrl() != null){
            Glide.with(holder.mImageView.getContext())
                    .load(group.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.mImageView);
        }else{
            Glide.with(holder.mImageView.getContext())
                    .load(R.drawable.profilepic)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.mImageView);
        }
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(),String.format("%s clicked", group.getName()),Toast.LENGTH_SHORT).show();
                boolean inEvent = mCallBack.touchGroup(mPosition);
                if (!inEvent && !FirebaseDatabaseUtils.connectedToDatabase()) {
                    Toasty.error(view.getContext(), "Unable to retrieve information of selected activity. Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(GroupDetailsActivity.isInstanceCreated()) return;
                Intent intent = new Intent(view.getContext(), GroupDetailsActivity.class);
                intent.putExtra(view.getContext().getResources().getString(R.string.groupKey), groupList.get(mPosition).getChatId());
                intent.putExtra(view.getContext().getResources().getString(R.string.InActivityKey),inEvent);
                intent.putExtra(view.getContext().getResources().getString(R.string.TapPositionKey),mPosition);
                view.getContext().startActivity(intent);
                //++CAN ADD TRANSITIONS TO NEXT ACTIVITY HERE
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder{
        private TextView mGroupName, mGroupDate, mGroupPlace, mGroupParticipants;
        private CardView mCardView;
        private ImageView mImageView;
        private GroupViewHolder(View itemView) {
            super(itemView);
            mGroupName = itemView.findViewById(R.id.cardNoteTitle);
            mGroupPlace = itemView.findViewById(R.id.cardNotePlace);
            mCardView = itemView.findViewById(R.id.cardViewNote);
            mImageView = itemView.findViewById(R.id.cardNoteIcon);
            mGroupDate = itemView.findViewById(R.id.cardNoteDate);
            mGroupParticipants = itemView.findViewById(R.id.cardNoteParticipants);
            //Include date + people count next time
        }
    }

    public interface GroupTouchCallBack{
        public boolean touchGroup(int pos);
    }

    public void addGroup(Group group){
        groupList.add(group);
    }

    public void removeGroup(Group group){
        groupList.remove(group);
    }

    public void removeGroupAtPos(int pos){
        groupList.removeItemAt(pos);
    }

    public void clearList(){
        groupList.clear();
    }

    public Group getGroupAtPos(int pos){
        Group group = null;
        try{
            group = groupList.get(pos);
        }catch(IndexOutOfBoundsException e){

        }
        return group;
    }
    public void updateGroup(int pos, Group group){
        groupList.updateItemAt(pos, group);
    }

    public boolean isListEmpty(){
        return groupList.size() == 0;
    }
}
