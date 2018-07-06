package com.example.gekpoh.boliao;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.sql.Time;

import static android.content.Context.ALARM_SERVICE;

public class TimeNotificationScheduler {
    private static final String TAG = "TimeNotificationSched";
    public static final int DELAY_2HRS = 7200000;

    public static void setExistingReminder(Context context, TimeNotification notification) {
        Log.v(TAG, "Creating New Reminders");

        long activatetime = notification.activityTime - notification.notificationDelay;
        if(activatetime < System.currentTimeMillis()) return;
        // Enable a receiver
        ComponentName receiver = new ComponentName(context, TimeNotificationReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(context, TimeNotificationReceiver.class);

        intent.putExtra(context.getResources().getString(R.string.EventNotificationIdKey), notification.groupId);
        intent.putExtra(context.getResources().getString(R.string.EventNotificationNameKey), notification.activityName);
        intent.putExtra(context.getResources().getString(R.string.EventNotificationTimeKey), notification.activityTime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.RTC, activatetime, pendingIntent);
    }

    public static void setNewReminder(Context context, String groupId, String activityName, long activityTimeStamp, long delayTimeStamp) {
        //Create new reminder then insert into database
        if(System.currentTimeMillis() > activityTimeStamp + delayTimeStamp) return;
        Log.v(TAG, "Creating new TimeNotification object");
        TimeNotification notification = new TimeNotification();
        notification.id = (int) System.currentTimeMillis();
        notification.groupId = groupId;
        notification.activityName = activityName;
        notification.activityTime = activityTimeStamp;
        notification.notificationDelay = delayTimeStamp == -1? DELAY_2HRS :delayTimeStamp;
        new InsertNewReminderTask(context, notification).execute();

        setExistingReminder(context, notification);
    }

    public static void cancelReminder(Context context, String groupId) {
        new CancelReminderTask(context, groupId).execute();
    }

    public static void showNotification(Context context, Class<?> cls, String groupId, String title, String content, String channelid) {
        new RemoveReminderTask(context, groupId).execute();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Time Notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Events Time Notification");
            notificationManager.createNotificationChannel(channel);
        }
        final int _id = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, cls);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        /*TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                _id,PendingIntent.FLAG_UPDATE_CURRENT);
         */
        PendingIntent pendingIntent = PendingIntent.getActivity(context,1,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelid);
        Notification notification = builder.setContentTitle(title)
                .setContentText(content).setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(_id, notification);

        Log.v(TAG, "Time Notification Showing");
    }



    private static class InsertNewReminderTask extends AsyncTask<Void, Void, Void>{
        private Context mContext;
        private TimeNotification notification;
        private InsertNewReminderTask(Context context, TimeNotification notification){
            mContext = context;
            this.notification = notification;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContext);
            db.timeNotificationDao().insert(notification);
            Log.v(TAG, "Inserting Time Notification into database");
            return null;
        }
    }
    //This class is used to remove the reminder from the database
    private static class RemoveReminderTask extends AsyncTask<Void, Void, Void>{
        private Context mContext;
        private String groupId;
        private RemoveReminderTask(Context context, String groupId){
            mContext = context;
            this.groupId = groupId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContext);
            db.timeNotificationDao().deleteById(groupId);
            Log.v(TAG, "removing notification from database");
            return null;
        }
    }

    //This class is used to cancel the reminder in both the database and the alarm manager. e.g leaving a group
    private static class CancelReminderTask extends AsyncTask<Void, Void, TimeNotification>{
        private Context mContext;
        private String groupId;
        private CancelReminderTask(Context context, String groupId){
            mContext = context;
            this.groupId = groupId;
        }

        @Override
        protected TimeNotification doInBackground(Void... voids) {
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContext);
            TimeNotification noti = db.timeNotificationDao().getNotificationById(groupId);
            db.timeNotificationDao().deleteById(groupId);
            return noti;
        }

        @Override
        protected void onPostExecute(TimeNotification notification) {
            if(notification == null) return;
            ComponentName receiver = new ComponentName(mContext, TimeNotificationReceiver.class);
            PackageManager pm = mContext.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Intent intent = new Intent(mContext, TimeNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            am.cancel(pendingIntent);
            Log.v(TAG, "Post cancelling notification for:" + notification.activityName);
            super.onPostExecute(notification);
        }
    }
}
