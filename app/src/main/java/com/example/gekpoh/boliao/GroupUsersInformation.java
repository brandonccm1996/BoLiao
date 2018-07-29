package com.example.gekpoh.boliao;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
//Loads relevant user information
public class GroupUsersInformation {
    private static HashMap<String,String> uidtoName = new HashMap<>();
    private static HashMap<String,String> uidtoUrl = new HashMap<>();
    private String chatId;
    private DatabaseReference userListsDatabaseReference, usersDatabaseReference;
    private ValueEventListener updateNameListener;
    public GroupUsersInformation(String chatId) {
        uidtoName.clear();
        this.chatId = chatId;
        FirebaseDatabase database = FirebaseDatabaseUtils.getDatabase();
        userListsDatabaseReference = database.getReference().child("userlists").child(chatId);
        //userListsDatabaseReference = database.getReference().child("users");
        usersDatabaseReference = database.getReference().child("users");
        updateNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uidtoName.put(dataSnapshot.getKey(),(String)dataSnapshot.child("name").getValue());
                uidtoUrl.put(dataSnapshot.getKey(),(String)dataSnapshot.child("photoUrl").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ChildEventListener listEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                usersDatabaseReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(updateNameListener);
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
        userListsDatabaseReference.addChildEventListener(listEventListener);
    }
    public static String getNamefromId(String uid){
        if(!uidtoName.containsKey(uid)){
            enforceUidExist(uid);
            return "#notFound:D";
        }else{
            return uidtoName.get(uid);
        }
    }
    public static String getPhotoUrlfromId(String uid){
        if(!uidtoUrl.containsKey(uid)){
            enforceUidExist(uid);
            return "";
        }else{
            return uidtoUrl.get(uid);
        }
    }

    public static void enforceUidExist(String uid){
        ValueEventListener requestNewNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uidtoName.put(dataSnapshot.getKey(),(String)dataSnapshot.child("name").getValue());
                uidtoUrl.put(dataSnapshot.getKey(),(String)dataSnapshot.child("photoUrl").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FirebaseDatabaseUtils.getDatabase().getReference().child("users").child(uid).addListenerForSingleValueEvent(requestNewNameListener);
    }
}
