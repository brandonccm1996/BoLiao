package com.example.gekpoh.boliao;

import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ChatMessageViewHolder>  {
    public static final SimpleDateFormat chatDateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mma");
    @NonNull
    private ArrayList<ChatMessage> chatMessageList;
    public ChatRecyclerAdapter(ArrayList<ChatMessage> chatMessages){
        chatMessageList = chatMessages;
    }
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_layout, parent, false);
        return new ChatMessageViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        final ChatMessage chatMessage = chatMessageList.get(position);
        String photoUrl = chatMessage.getPhotoUrl();
        if(photoUrl != null){
            final ChatMessageViewHolder holder2 = holder;
            holder.messageContent.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(holder.imageView.getContext())
                    .load(chatMessage.getPhotoUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder2.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.imageView);
        }else {
            holder.progressBar.setVisibility(View.GONE);
            holder.messageContent.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.messageContent.setText(chatMessage.getText());
        }
        holder.messageTime.setText(chatDateFormatter.format(new Date(chatMessage.getTimeStamp())));
        holder.messageOwner.setText(GroupUsersInformation.getNamefromId(chatMessage.getUid()));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageOwner, messageContent, messageTime;
        public ImageView imageView;
        public ProgressBar progressBar;
        public ChatMessageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
            messageOwner = itemView.findViewById(R.id.nameTextView);
            messageContent = itemView.findViewById(R.id.messageTextView);
            messageTime = itemView.findViewById(R.id.dateTextView);
            progressBar = itemView.findViewById(R.id.chatProgressBar);
            //Include date + people count next time
        }
    }
}
