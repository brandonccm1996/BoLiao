package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

public class NotificationsSettingsActivity extends AppCompatActivity {
    Switch timeSwitch, updateSwitch;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_settings_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");
        timeSwitch = findViewById(R.id.timeSwitch);
        timeSwitch.setChecked(Settings.TIME_NOTIFICATIONS_ENABLED);
        timeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.TIME_NOTIFICATIONS_ENABLED = isChecked;
                SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_sharedprefs_dir),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(getResources().getString(R.string.time_settings_key),Settings.TIME_NOTIFICATIONS_ENABLED);
                editor.apply();
            }
        });
        updateSwitch = findViewById(R.id.updateSwitch);
        updateSwitch.setChecked(Settings.UPDATE_NOTIFICATIONS_ENABLED);
        updateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.UPDATE_NOTIFICATIONS_ENABLED = isChecked;
                SharedPreferences prefs = getSharedPreferences(getString(R.string.settings_sharedprefs_dir),Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(getResources().getString(R.string.update_settings_key),Settings.UPDATE_NOTIFICATIONS_ENABLED);
                editor.apply();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
