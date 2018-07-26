package com.example.gekpoh.boliao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment{
    private static final int MAX_MESSAGE_LENGTH = 140;
    public static final int RC_PHOTO_PICKER = 1;
    private final String TAG = "ChatFragment";
    private boolean moveToEndAllowed = true;
    private RecyclerView chatRecyclerView;
    private LinearLayoutManager layoutManager;
    private ArrayList<ChatMessage> chatMessageList;
    private ChatRecyclerAdapter adapter;
    private EditText editText;
    private Button sendButton;
    private ImageButton photoPickerButton;
    private DatabaseReference mDatabaseReference;
    private StorageReference mChatPhotoStorageReference;
    private ChildEventListener mChildEventListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        editText = getView().findViewById(R.id.messageEditText);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_MESSAGE_LENGTH)});
        sendButton = getView().findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "SENDING MESSAGE");
                String key = mDatabaseReference.push().getKey();
                // Clear input box
                Map<String, Object> map = new HashMap<>();
                map.put("uid", MainActivity.userUid);
                map.put("text", editText.getText().toString());
                map.put("timeStamp", ServerValue.TIMESTAMP);
                mDatabaseReference.child(key).setValue(map);
                editText.setText("");
                moveToEndAllowed = true;
            }
        });
        photoPickerButton = getView().findViewById(R.id.photoPickerButton);
        photoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
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

        chatMessageList = new ArrayList<>();

        chatRecyclerView = getView().findViewById(R.id.chatRecyclerView);
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatRecyclerAdapter(chatMessageList);
        chatRecyclerView.setAdapter(adapter);
        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int currentPosition = layoutManager.findLastVisibleItemPosition();
                if(chatMessageList.size() - currentPosition <= 10){
                    moveToEndAllowed = true;
                }else{
                    moveToEndAllowed = false;
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        String chatKey = getArguments().getString(getString(R.string.groupIdKey));
        mChatPhotoStorageReference = FirebaseStorage.getInstance().getReference().child("chats").child(chatKey);
        mDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("chats").child(chatKey);
        mDatabaseReference.keepSynced(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            final StorageReference photoRef = mChatPhotoStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String key = mDatabaseReference.push().getKey();
                        if(key != null) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("uid", MainActivity.userUid);
                            map.put("text", editText.getText().toString());
                            map.put("photoUrl", downloadUri.toString());
                            map.put("timeStamp", ServerValue.TIMESTAMP);
                            mDatabaseReference.child(key).setValue(map);
                            editText.setText("");
                            Log.v(TAG,"Sending picture");
                            moveToEndAllowed = true;
                        }else{
                            Log.e(TAG,"failed to send message");
                        }
                    } else {
                        // Handle failures
                        Toasty.error(getActivity(), "Unable to send image", Toast.LENGTH_SHORT).show();
                        editText.setText("");
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        if (mDatabaseReference != null && mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

        super.onPause();
    }
    @Override
    public void onResume() {
        if(mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.v(TAG, "child added");
                    ChatMessage message = dataSnapshot.getValue(ChatMessage.class);
                    chatMessageList.add(message);
                    adapter.notifyItemInserted(chatMessageList.size());
                    if(moveToEndAllowed){
                        chatRecyclerView.scrollToPosition(chatMessageList.size() - 1);
                    }
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
        super.onResume();
    }
}
