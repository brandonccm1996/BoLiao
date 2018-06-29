package com.example.gekpoh.boliao;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class EditEventInfoActivity extends AppCompatActivity {
    private Group mGroup;
    private static final int RC_PHOTO_PICKER = 1;
    private EditText editname, editplace, editstart, editend, editdescription, editsize;
    private Button cancelbutton, okbutton;
    private ImageView profilePicView;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private Date startDate, endDate;
    private String dateTimeString;
    private boolean hasPhoto = false, changedPhotos = false;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event_info_layout);
        mGroup = getIntent().getParcelableExtra(getString(R.string.groupKey));
        if (mGroup == null) {
            setResult(RESULT_CANCELED);
            finish();
        }
        mDatabaseReference = FirebaseDatabaseUtils.getDatabase().getReference().child("groups").child(mGroup.getChatId());
        mStorageReference = FirebaseStorage.getInstance().getReference().child("groupprofilepics");
        editname = findViewById(R.id.editEventNameView);
        editname.setText(mGroup.getNames());
        editplace = findViewById(R.id.editEventPlaceView);
        editplace.setText(mGroup.getLocation());
        editsize = findViewById(R.id.editEventSizeView);
        editsize.setText(Long.toString(mGroup.getMaxParticipants()));
        //need to check that the current size is not larger than the new max size
        startDate = new Date(mGroup.getStartDateTime());
        endDate = new Date(mGroup.getEndDateTime());
        editstart = findViewById(R.id.editEventStartDateView);
        editstart.setText(mGroup.getStartDateTimeString());
        editstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);
                DatePickerDialog mDatePickerDialog = new DatePickerDialog(EditEventInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateTimeString = dayOfMonth + "/" + (month + 1) + "/" + year;
                        final TimePickerDialog mTimePickerDialog = new TimePickerDialog(EditEventInfoActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                dateTimeString = dateTimeString + " " + String.format("%02d:%02d", hourOfDay, minute);
                                editstart.setText(dateTimeString);
                                try {
                                    startDate = Group.groupDateFormatter.parse(dateTimeString);
                                } catch (ParseException e) {

                                }
                            }
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
                        mTimePickerDialog.show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                mDatePickerDialog.show();
            }
        });
        editend = findViewById(R.id.editEventEndDateView);
        editend.setText(mGroup.getEndDateTimeString());
        editend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                DatePickerDialog mDatePickerDialog = new DatePickerDialog(EditEventInfoActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dateTimeString = dayOfMonth + "/" + (month + 1) + "/" + year;
                        final TimePickerDialog mTimePickerDialog = new TimePickerDialog(EditEventInfoActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                dateTimeString = dateTimeString + " " + String.format("%02d:%02d", hourOfDay, minute);
                                editend.setText(dateTimeString);
                                try {
                                    endDate = Group.groupDateFormatter.parse(dateTimeString);
                                } catch (ParseException e) {

                                }
                            }
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
                        mTimePickerDialog.show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                mDatePickerDialog.show();
            }
        });
        editdescription = findViewById(R.id.editEventDescriptionView);
        editdescription.setText(mGroup.getDescription());
        profilePicView = findViewById(R.id.editGroupPicView);
        if (mGroup.getPhotoUrl() != null) {
            hasPhoto = true;
            Glide.with(this)
                    .load(mGroup.getPhotoUrl())
                    .into(profilePicView);
        }
        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(EditEventInfoActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_editprofilepic, popupMenu.getMenu());
                if (!hasPhoto) {
                    Menu menu = popupMenu.getMenu();
                    menu.findItem(R.id.remove_pic).setEnabled(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.remove_pic:
                                hasPhoto = false;
                                changedPhotos = true;
                                Toast.makeText(EditEventInfoActivity.this, "Activity pic removed", Toast.LENGTH_SHORT).show();
                                profilePicView.setImageResource(R.drawable.profilepic);
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
        cancelbutton = findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        okbutton = findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                if(changedPhotos) {
                    if (hasPhoto) {
                        final StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
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
                                    Uri photoUri = task.getResult();
                                    mDatabaseReference.child("photoUrl").setValue(photoUri.toString());
                                }
                            }
                        });
                    } else {
                        mDatabaseReference.child("photoUrl").setValue(null);
                    }
                }
                mDatabaseReference.child("names").setValue(editname.getText().toString());
                mDatabaseReference.child("description").setValue(editdescription.getText().toString());
                mDatabaseReference.child("location").setValue(editplace.getText().toString());
                mDatabaseReference.child("maxParticipants").setValue(Integer.parseInt(editsize.getText().toString()));
                mDatabaseReference.child("startDateTime").setValue(startDate.getTime());
                mDatabaseReference.child("endDateTime").setValue(endDate.getTime());
                //set place id?
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                selectedImageUri = data.getData();
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(profilePicView);
                hasPhoto = true;
                changedPhotos = true;
            }
        }
    }
}