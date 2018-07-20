package com.example.gekpoh.boliao;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProfileActivity extends AppCompatActivity {

    private static final int editNameRequest = 1;
    private static final int editDescriptionRequest = 2;
    private static final int RC_PHOTO_PICKER = 3;

    private ImageView imageViewProPic;
    private TextView textViewName;
    private RatingBar ratingBar;
    private TextView textViewNumRatings;
    private TextView textViewDescription;
    private ProgressBar progressBar;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mProfilePicStorageReference;

    private UserInformation currentUserInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.common_google_signin_btn_icon_dark);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mFirebaseDatabase = FirebaseDatabaseUtils.getDatabase();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mProfilePicStorageReference = mFirebaseStorage.getReference().child("userprofilepics");

        imageViewProPic = findViewById(R.id.imageViewProPic);
        textViewName = findViewById(R.id.textViewName);
        ratingBar = findViewById(R.id.ratingBar);
        textViewNumRatings = findViewById(R.id.textViewNumRatings);
        textViewDescription = findViewById(R.id.textViewDescription);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        imageViewProPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(EditProfileActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_editprofilepic, popupMenu.getMenu());

                if (currentUserInfo.getPhotoUrl().equals("")) {
                    Menu menu = popupMenu.getMenu();
                    menu.findItem(R.id.remove_pic).setEnabled(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove_pic:
                                deletePic();
                                mUsersDatabaseReference.child(MainActivity.userUid).child("photoUrl").setValue("");
                                reloadUserDetails();
                                Toast.makeText(EditProfileActivity.this, "Profile pic removed", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.update_pic:
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/jpeg");
                                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
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

        reloadUserDetails();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == editNameRequest) {
            if (resultCode == Activity.RESULT_OK) {
                String newName = data.getStringExtra(Intent.EXTRA_TEXT);
                mUsersDatabaseReference.child(MainActivity.userUid).child("name").setValue(newName);
                reloadUserDetails();
                Toast.makeText(EditProfileActivity.this, "Username updated", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == editDescriptionRequest) {
            if (resultCode == Activity.RESULT_OK) {
                String newDescription = data.getStringExtra(Intent.EXTRA_TEXT);
                mUsersDatabaseReference.child(MainActivity.userUid).child("description").setValue(newDescription);
                reloadUserDetails();
                Toast.makeText(EditProfileActivity.this, "Description updated", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                progressBar.setVisibility(View.VISIBLE);

                // Delete previous file in Firebase Storage
                if (!currentUserInfo.getPhotoUrl().equals("")) {
                    deletePic();
                }

                // Upload new file in Firebase Storage
                Uri selectedImageUri = data.getData();
                final StorageReference photoRef = mProfilePicStorageReference.child(selectedImageUri.getLastPathSegment());
                UploadTask uploadTask = photoRef.putFile(selectedImageUri);

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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
                            mUsersDatabaseReference.child(MainActivity.userUid).child("photoUrl").setValue(downloadUri.toString());
                            reloadUserDetails();
                            Toast.makeText(EditProfileActivity.this, "Profile pic updated", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void deletePic() {
        final StorageReference photoDeleteRef = mFirebaseStorage.getReferenceFromUrl(currentUserInfo.getPhotoUrl());

        photoDeleteRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    private void reloadUserDetails() {
        mUsersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                currentUserInfo = dataSnapshot.child(MainActivity.userUid).getValue(UserInformation.class);

                if (currentUserInfo.getName().equals("")) textViewName.setText("Enter username");
                else textViewName.setText(currentUserInfo.getName());

                if (currentUserInfo.getNumRatings() == 0) ratingBar.setRating(0);
                else ratingBar.setRating(currentUserInfo.getSumRating() / currentUserInfo.getNumRatings());

                textViewNumRatings.setText(" Number of ratings: " + currentUserInfo.getNumRatings());

                if (currentUserInfo.getDescription().equals("")) textViewDescription.setText("Enter a description of yourself");
                else textViewDescription.setText(currentUserInfo.getDescription());

                if (currentUserInfo.getPhotoUrl().equals("")) {
                    Glide.with(imageViewProPic.getContext())
                            .load(R.drawable.profilepic)
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageViewProPic);
                }
                else {
                    Glide.with(imageViewProPic.getContext())
                            .load(currentUserInfo.getPhotoUrl())
                            .apply(RequestOptions.circleCropTransform())
                            .into(imageViewProPic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
