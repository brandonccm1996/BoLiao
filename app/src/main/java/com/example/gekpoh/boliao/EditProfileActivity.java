package com.example.gekpoh.boliao;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private static final int editNameRequest = 1;
    private static final int editDescriptionRequest = 2;

    private ImageView imageViewProPic;
    private TextView textViewName;
    private RatingBar ratingBar;
    private TextView textViewDescription;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;

    private UserInfo currentUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");

        imageViewProPic = findViewById(R.id.imageViewProPic);
        textViewName = findViewById(R.id.textViewName);
        ratingBar = findViewById(R.id.ratingBar);
        textViewDescription = findViewById(R.id.textViewDescription);

        imageViewProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        textViewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startEditNameActivityIntent = new Intent (EditProfileActivity.this, EditNameActivity.class);
                startEditNameActivityIntent.putExtra(Intent.EXTRA_TEXT, currentUserInfo.getName());
                startActivityForResult(startEditNameActivityIntent, editNameRequest);
            }
        });

        textViewDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startEditDescriptionActivityIntent = new Intent (EditProfileActivity.this, EditDescriptionActivity.class);
                startEditDescriptionActivityIntent.putExtra(Intent.EXTRA_TEXT, currentUserInfo.getDescription());
                startActivityForResult(startEditDescriptionActivityIntent, editDescriptionRequest);
            }
        });

        mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) { // find the correct userInfo in the database to display
                    if (uniqueKeySnapshot.getKey().equals(MainActivity.userEmail)) {
                        currentUserInfo = uniqueKeySnapshot.getValue(UserInfo.class);
                        textViewName.setText(currentUserInfo.getName());
                        ratingBar.setRating(currentUserInfo.getRating());
                        textViewDescription.setText(currentUserInfo.getDescription());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == editNameRequest) {
            if (resultCode == Activity.RESULT_OK) {
                String newName = data.getStringExtra(Intent.EXTRA_TEXT);
                mUsersDatabaseReference.child(MainActivity.userEmail).child("name").setValue(newName);
            }
        }
        else if (requestCode == editDescriptionRequest) {
            if (resultCode == Activity.RESULT_OK) {
                String newDescription = data.getStringExtra(Intent.EXTRA_TEXT);
                mUsersDatabaseReference.child(MainActivity.userEmail).child("description").setValue(newDescription);
            }
        }
    }
}
