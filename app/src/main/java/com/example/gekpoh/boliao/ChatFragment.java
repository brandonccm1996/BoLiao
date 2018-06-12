package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChatFragment extends Fragment {
    private static final int MAX_MESSAGE_LENGTH = 140;
    private final String TAG = "ChatFragment";
    private RecyclerView chatRecyclerView;
    private ArrayList<ChatMessage> chatMessageList;
    private ChatRecyclerAdapter adapter;
    private EditText editText;
    private Button sendButton;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("chat").child(getArguments().getString(getString(R.string.groupIdKey)));
        editText = getView().findViewById(R.id.messageEditText);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_MESSAGE_LENGTH)});
        sendButton = getView().findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = mDatabaseReference.push().getKey();
                // Clear input box
                Map<String, Object> map = new HashMap<>();
                map.put("name", MainActivity.userUid);
                map.put("text", editText.getText());
                map.put("timestamp", ServerValue.TIMESTAMP);
                mDatabaseReference.child(key).setValue(map);
                editText.setText("");
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        chatRecyclerView = getView().findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ChatRecyclerAdapter(chatMessageList);
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                chatMessageList.add(dataSnapshot.getValue(ChatMessage.class));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }
}
