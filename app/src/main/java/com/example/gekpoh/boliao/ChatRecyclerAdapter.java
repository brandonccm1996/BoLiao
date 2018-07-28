package com.example.gekpoh.boliao;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatRecyclerAdapter extends RecyclerView.Adapter {
    private static final int ME = 1, OTHERS = 2;
    public static final SimpleDateFormat chatDateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mma");
    public static final SimpleDateFormat chatDateFormatter2 = new SimpleDateFormat("hh:mma");
    public static final SimpleDateFormat chatDateFormatter3 = new SimpleDateFormat("dd/MM/yyyy");
    @NonNull
    private ArrayList<ChatMessage> chatMessageList;
    private String organizerId;

    public ChatRecyclerAdapter(ArrayList<ChatMessage> chatMessages, String organizerId) {
        chatMessageList = chatMessages;
        this.organizerId = organizerId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessageList.get(position);
        if (message.getUid().equals(MainActivity.userUid)) {
            return ME;
        } else {
            return OTHERS;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ME) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_sent_layout, parent, false);
            return new SentMessageViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_received_layout, parent, false);
            return new ReceivedMessageViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ChatMessage chatMessage = chatMessageList.get(position);
        boolean sameUser = false;
        boolean sameDay = false;
        String dateString = chatDateFormatter3.format(new Date(chatMessage.getTimeStamp()));
        if (position != 0) {
            ChatMessage chatMessage2 = chatMessageList.get(position - 1);
            sameUser = chatMessage2.getUid().equals(chatMessage.getUid());
            sameDay = chatDateFormatter3.format(new Date(chatMessage2.getTimeStamp())).equals(dateString);

        }
        String photoUrl = chatMessage.getPhotoUrl();
        if (chatMessage.getUid().equals(MainActivity.userUid)) {
            final SentMessageViewHolder holderr = (SentMessageViewHolder) holder;
            if(!sameDay){
                holderr.dateText.setText(dateString);
                holderr.dateText.setVisibility(View.VISIBLE);
            }else{
                holderr.dateText.setVisibility(View.GONE);
            }
            holderr.messageContent.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holderr.messageContent.setText("");
            if (photoUrl != null) {
                Glide.with(holderr.messageContent.getContext())
                        .asBitmap()
                        .load(photoUrl)
                        .into(new SimpleTarget<Bitmap>(500, 500) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                holderr.messageContent.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(holderr.messageContent.getResources(), resource), null, null);
                            }
                        });
            }
            holderr.messageContent.setText(chatMessage.getText());
            holderr.messageTime.setText(chatDateFormatter2.format(new Date(chatMessage.getTimeStamp())));
        } else {
            final ReceivedMessageViewHolder holderr = (ReceivedMessageViewHolder) holder;
            if(!sameDay){
                holderr.dateText.setText(dateString);
                holderr.dateText.setVisibility(View.VISIBLE);
            }else{
                holderr.dateText.setVisibility(View.GONE);
            }
            holderr.messageContent.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holderr.messageContent.setText("");
            if (photoUrl != null) {
                Glide.with(holderr.messageContent.getContext())
                        .asBitmap()
                        .load(photoUrl)
                        .into(new SimpleTarget<Bitmap>(500, 500) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                holderr.messageContent.setCompoundDrawablesWithIntrinsicBounds(null, new BitmapDrawable(holderr.messageContent.getResources(), resource), null, null);
                            }
                        });
            }
            holderr.messageContent.setText(chatMessage.getText());
            holderr.messageTime.setText(chatDateFormatter2.format(new Date(chatMessage.getTimeStamp())));
            if (!sameUser){
                holderr.messageOwner.setVisibility(View.VISIBLE);
                holderr.messageProfilePic.setVisibility(View.VISIBLE);
                holderr.messageOwner.setText(GroupUsersInformation.getNamefromId(chatMessage.getUid()));
                Glide.with(holderr.messageProfilePic.getContext())
                        .load(GroupUsersInformation.getPhotoUrlfromId(chatMessage.getUid()).equals("")?R.drawable.profilepic:GroupUsersInformation.getPhotoUrlfromId(chatMessage.getUid()))
                        .apply(RequestOptions.circleCropTransform())
                        .into(holderr.messageProfilePic);
                if (chatMessage.getUid().equals(organizerId)) {
                    holderr.messageOwner.setTextColor(Color.parseColor("#ff9100"));
                }else{
                    holderr.messageOwner.setTextColor(Color.BLACK);
                }
            }else{
                holderr.messageOwner.setVisibility(View.GONE);
                holderr.messageProfilePic.setVisibility(View.INVISIBLE);
            }
        }

    }
    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }
    /*
    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
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
        }
    }*/

    public class SentMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageContent, messageTime;
        public EditText dateText;
        public SentMessageViewHolder(View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.messageTextView);
            messageTime = itemView.findViewById(R.id.dateTextView);
            dateText = itemView.findViewById(R.id.dateEditText);
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageOwner, messageContent, messageTime;
        public EditText dateText;
        public ImageView messageProfilePic;
        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageProfilePic = itemView.findViewById(R.id.profilePic);
            messageOwner = itemView.findViewById(R.id.nameTextView);
            messageContent = itemView.findViewById(R.id.messageTextView);
            messageTime = itemView.findViewById(R.id.dateTextView);
            dateText = itemView.findViewById(R.id.dateEditText);
        }
    }
}
