package com.example.gekpoh.boliao;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import static android.content.Context.ALARM_SERVICE;

public class TimeNotificationScheduler {
    private static final String TAG = "TimeNotificationSched";
    public static final int DELAY_2HRS = 7200000;

    //Set reminders for notifications that  already exist in the database
    public static void setExistingReminder(Context context, TimeNotification notification) {

        long activatetime = notification.activityTime - notification.notificationDelay;
        if (activatetime < System.currentTimeMillis()) {
            //Already missed the time for the notification, remove the notification from database
            //new RemoveReminderTask(context, notification.groupId).execute();
            return;
        }
        Log.v(TAG, "Creating New Reminders");
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

    //Set reminders for notifications that does not exist in the database
    public static void setNewReminder(Context context, String groupId, String activityName, long activityTimeStamp, long delayTimeStamp) {
        //Create new reminder then insert into database
        //if(System.currentTimeMillis() > activityTimeStamp - delayTimeStamp) return;
        Log.v(TAG, "Creating new TimeNotification object");
        TimeNotification notification = new TimeNotification();
        notification.id = (int) System.currentTimeMillis();
        notification.groupId = groupId;
        notification.activityName = activityName;
        notification.activityTime = activityTimeStamp;
        notification.notificationDelay = delayTimeStamp == -1 ? DELAY_2HRS : delayTimeStamp;
        new InsertNewReminderTask(context, notification).execute();

        setExistingReminder(context, notification);
    }

    public static void updateReminder(Context context, String groupId, String activityName, long activityTimeStamp, long delayTimeStamp) {
        //Create new reminder then insert into database
        //if (System.currentTimeMillis() > activityTimeStamp - delayTimeStamp) return;
        new UpdateReminderTask(context, groupId, activityName, activityTimeStamp, delayTimeStamp).execute();
    }

    //Remove notification from database + remove pending intent from alarm manager
    public static void cancelReminder(Context context, String groupId) {
        new CancelReminderTask(context, groupId).execute();
    }

    /*
    public static void disableAllReminder(Context context){
        new DisableAllRemindersTask(context).execute();
    }

    public static void enableAllReminder(Context context){
        new EnableAllRemindersTask(context).execute();
    }*/

    public static void showNotification(Context context, Class<?> cls, String groupId, String title, String content, String channelid) {
        //new RemoveReminderTask(context, groupId).execute();

        SharedPreferences prefs = context.getSharedPreferences(context.getResources().getString(R.string.settings_sharedprefs_dir), Context.MODE_PRIVATE);
        boolean timeNotificationsEnabled = prefs.getBoolean(context.getResources().getString(R.string.time_settings_key), false);
        if (!timeNotificationsEnabled) return;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Time Notification", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Events Time Notification");
            notificationManager.createNotificationChannel(channel);
        }
        final int _id = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, cls);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelid);
        Notification notification = builder.setContentTitle(title + "\n")
                .setContentText(content + "\n").setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(_id, notification);
        Log.v(TAG, "Time Notification Showing");
    }

    private static class InsertNewReminderTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private TimeNotification notification;

        private InsertNewReminderTask(Context context, TimeNotification notification) {
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
    private static class RemoveReminderTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private String groupId;

        private RemoveReminderTask(Context context, String groupId) {
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

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.v(TAG, "notification is removed from database");
            super.onPostExecute(aVoid);
        }
    }

    //This class is used to cancel the reminder in both the database and the alarm manager. e.g leaving a group
    private static class CancelReminderTask extends AsyncTask<Void, Void, TimeNotification> {
        private Context mContext;
        private String groupId;

        private CancelReminderTask(Context context, String groupId) {
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
            if (notification == null) return;
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

    private static class UpdateReminderTask extends AsyncTask<Void, Void, TimeNotification> {
        private Context mContext;
        private String groupId;
        String activityName;
        long timeStamp, timeDelay;

        private UpdateReminderTask(Context context, String groupId, String activityName, long timeStamp, long timeDelay) {
            mContext = context;
            this.groupId = groupId;
            this.activityName = activityName;
            this.timeStamp = timeStamp;
            this.timeDelay = timeDelay;
        }

        @Override
        protected TimeNotification doInBackground(Void... voids) {
            Log.v(TAG, "Executing update");
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContext);
            TimeNotification noti = db.timeNotificationDao().getNotificationById(groupId);
            db.timeNotificationDao().deleteById(groupId);
            return noti;
        }

        @Override
        protected void onPostExecute(TimeNotification notification) {
            if(notification == null){
                Log.v(TAG, "NOTIFICATION OBJECT NOT FOUND WHEN UPDATING REMINDER!!!");
                return;//NOTE!: notification should never be null
            }
            if (activityName == null) activityName = notification.activityName;
            if (timeStamp == -1) timeStamp = notification.activityTime;
            ComponentName receiver = new ComponentName(mContext, TimeNotificationReceiver.class);
            PackageManager pm = mContext.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Intent intent = new Intent(mContext, TimeNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
            am.cancel(pendingIntent);
            Log.v(TAG, "Update: alarm deleted" + notification.activityName);
            setNewReminder(mContext, groupId, activityName, timeStamp, timeDelay);
            super.onPostExecute(notification);
        }
    }
    /*
    //This class is used to disable all reminders without removing them from the database
    private static class DisableAllRemindersTask extends AsyncTask<Void, Void, List<TimeNotification>> {
        private Context mContext;

        private DisableAllRemindersTask(Context context) {
            mContext = context;
        }
        @Override
        protected List<TimeNotification> doInBackground(Void... voids) {
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContext);
            return db.timeNotificationDao().getAllNotification();
        }

        @Override
        protected void onPostExecute(List<TimeNotification> timeNotifications) {
            if(timeNotifications != null && timeNotifications.size()>0){
                for(TimeNotification notification: timeNotifications){
                    ComponentName receiver = new ComponentName(mContext, TimeNotificationReceiver.class);
                    PackageManager pm = mContext.getPackageManager();
                    pm.setComponentEnabledSetting(receiver,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    Intent intent = new Intent(mContext, TimeNotificationReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, notification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager am = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
                    am.cancel(pendingIntent);
                    Log.v(TAG, "Post disabling notification for:" + notification.activityName);
                }
            }
        }
    }

    //This class is used to enable all reminders that is already in the database
    private static class EnableAllRemindersTask extends AsyncTask<Void, Void, List<TimeNotification>> {
        private Context mContext;

        private EnableAllRemindersTask(Context context) {
            mContext = context;
        }
        @Override
        protected List<TimeNotification> doInBackground(Void... voids) {
            TimeNotificationDatabase db = TimeNotificationDatabase.getTimeNotificationDatabase(mContext);
            return db.timeNotificationDao().getAllNotification();
        }

        @Override
        protected void onPostExecute(List<TimeNotification> timeNotifications) {
            if(timeNotifications != null && timeNotifications.size()>0){
                for(TimeNotification notification: timeNotifications){
                    TimeNotificationScheduler.setExistingReminder(mContext, notification);
                }
            }
        }
    }
    */
}
