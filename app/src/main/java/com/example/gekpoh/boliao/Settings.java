package com.example.gekpoh.boliao;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {
    public static boolean TIME_NOTIFICATIONS_ENABLED = false;
    public static boolean UPDATE_NOTIFICATIONS_ENABLED = false;

    public static void loadSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.settings_sharedprefs_dir),Context.MODE_PRIVATE);
        TIME_NOTIFICATIONS_ENABLED = prefs.getBoolean(context.getResources().getString(R.string.time_settings_key), false);
        UPDATE_NOTIFICATIONS_ENABLED = prefs.getBoolean(context.getResources().getString(R.string.update_settings_key), false);
    }
}
