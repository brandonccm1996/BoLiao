package com.example.gekpoh.boliao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


import java.util.Date;
import java.util.List;

public class TimeNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "TimeNotificationRec";
    private static final String TIME_ID = "TIME";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                Intent serviceIntent = new Intent(context, RebootSetUpService.class);
                context.startService(serviceIntent);
                return;
            }
        }
        String groupId = intent.getStringExtra(context.getResources().getString(R.string.EventNotificationIdKey));
        String name = intent.getStringExtra(context.getResources().getString(R.string.EventNotificationNameKey));
        long timestamp = intent.getLongExtra(context.getResources().getString(R.string.EventNotificationTimeKey), 0);
        String time = Group.groupDateFormatter.format(new Date(timestamp));
        TimeNotificationScheduler.showNotification(context, MainActivity.class, groupId,"You have an event to attend soon!", name + " is happening at " + time, TIME_ID);
    }

}
