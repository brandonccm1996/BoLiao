package com.example.gekpoh.boliao;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateNewEventFragment1 extends Fragment{

    private EditText editTextName;
    private EditText editTextSDate;
    private EditText editTextSTime;
    private EditText editTextEDate;
    private EditText editTextETime;
    private EditText editTextLocation;

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimePickerDialog;
    private Calendar calendar;
    private int year, month, dayOfMonth;

    private Date sDateTime;
    private Date eDateTime;
    private SimpleDateFormat groupDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_new_event_fragment_layout1, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        editTextName = getView().findViewById(R.id.editTextName);
        editTextSDate = getView().findViewById(R.id.editTextSDate);
        editTextSTime = getView().findViewById(R.id.editTextSTime);
        editTextEDate = getView().findViewById(R.id.editTextEDate);
        editTextETime = getView().findViewById(R.id.editTextETime);
        editTextLocation = getView().findViewById(R.id.editTextLocation);

        editTextSDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                mDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextSDate.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

                mDatePickerDialog.show();
            }
        });

        editTextEDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                mDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextEDate.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                }, year, month, dayOfMonth);

                mDatePickerDialog.show();
            }
        });

        editTextSTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay > 12) editTextSTime.setText(String.format("%02d:%02d", hourOfDay-12, minute) + "p.m.");
                        else if (hourOfDay == 0) editTextSTime.setText(String.format("%02d:%02d", hourOfDay+12, minute) + "a.m.");
                        else if (hourOfDay == 12) editTextSTime.setText(String.format("%02d:%02d", hourOfDay, minute) + "p.m.");
                        else editTextSTime.setText(String.format("%02d:%02d", hourOfDay, minute) + "a.m.");
                    }
                }, 0, 0, false);

                mTimePickerDialog.show();
            }
        });

        editTextETime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay > 12) editTextETime.setText(String.format("%02d:%02d", hourOfDay-12, minute) + "p.m.");
                        else if (hourOfDay == 0) editTextETime.setText(String.format("%02d:%02d", hourOfDay+12, minute) + "a.m.");
                        else if (hourOfDay == 12) editTextETime.setText(String.format("%02d:%02d", hourOfDay, minute) + "p.m.");
                        else editTextETime.setText(String.format("%02d:%02d", hourOfDay, minute) + "a.m.");
                    }
                }, 0, 0, false);

                mTimePickerDialog.show();
            }
        });
    }

    public String sendName() { return editTextName.getText().toString(); }
    public String sendLocation() { return editTextLocation.getText().toString(); }
    public String sendSDate() { return editTextSDate.getText().toString(); }
    public String sendSTime() { return editTextSTime.getText().toString(); }
    public String sendEDate() { return editTextEDate.getText().toString(); }
    public String sendETime() { return editTextETime.getText().toString(); }
}
