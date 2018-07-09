package com.example.gekpoh.boliao;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class RebootSetUpService extends Service {
    private static final String TAG = "RebootSetUpService";
    private Intent intent;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        new RebootSetupTask(this).execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private class RebootSetupTask extends AsyncTask<Void, Void, List<TimeNotification>> {

        private Context mContextRef;

        private RebootSetupTask(Context context) {
            mContextRef = context;
        }
        @Override
        protected List<TimeNotification> doInBackground(Void... voids) {
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContextRef);
            return db.timeNotificationDao().getAllNotification();
        }

        @Override
        protected void onPostExecute(List<TimeNotification> timeNotifications) {
            if(timeNotifications != null && timeNotifications.size()>0){
                for(TimeNotification notification: timeNotifications){
                    Log.v(TAG,"Reboot: Loading notifications from database");
                    TimeNotificationScheduler.setExistingReminder(mContextRef, notification);
                }
            }
            //super.onPostExecute(timeNotifications);
            stopService(intent);
        }
    }
}
