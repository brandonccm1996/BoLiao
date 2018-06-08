package com.example.gekpoh.boliao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatFragment extends Fragment {
    private static final int MAX_MESSAGE_LENGTH = 140;
    private String mReference;
    private final String TAG = "ChatFragment";
    private RecyclerView chatRecyclerView;
    private ArrayList<ChatMessage> chatMessageList;
    private ChatRecyclerAdapter adapter;
    private FirebaseDatabase databaseReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mReference = getArguments().getString(getResources().getString(R.string.groupReferenceKey));
        EditText editText = getView().findViewById(R.id.messageEditText);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_MESSAGE_LENGTH)});
        chatRecyclerView = getView().findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ChatRecyclerAdapter(chatMessageList);
    }
}
