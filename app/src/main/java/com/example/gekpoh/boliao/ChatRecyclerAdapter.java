package com.example.gekpoh.boliao;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    public static final SimpleDateFormat chatDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    @NonNull
    private ArrayList<ChatMessage> chatMessageList;
    public ChatRecyclerAdapter(ArrayList<ChatMessage> chatMessages,String chatKey){
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
        holder.messageContent.setText(chatMessage.getText());
        holder.messageTime.setText(chatDateFormatter.format(new Date(chatMessage.getTimeStamp())));
        holder.messageOwner.setText(GroupUsersInformation.getNamefromId(chatMessage.getUid()));

    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageOwner, messageContent, messageTime;
        public ChatMessageViewHolder(View itemView) {
            super(itemView);
            messageOwner = itemView.findViewById(R.id.nameTextView);
            messageContent = itemView.findViewById(R.id.messageTextView);
            messageTime = itemView.findViewById(R.id.dateTextView);
            //Include date + people count next time
        }
    }
}
