package com.example.gekpoh.boliao;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.firebase.database.DatabaseReference;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class EditEventInfoActivity extends AppCompatActivity {
    private Group mGroup;
    private EditText editname, editplace, editstart, editend, editdescription, editsize;
    private Button cancelbutton, okbutton;
    private DatabaseReference mDatabaseReference;
    private Date startDate, endDate;
    private String dateTimeString;

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
}
