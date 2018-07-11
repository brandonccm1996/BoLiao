package com.example.gekpoh.boliao;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "channel_updatedelete", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Update or Delete Event Details");
            mNotificationManager.createNotificationChannel(channel);
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().get("title").equals("Start Time Change Detected")) {
                Log.d("FbMessagingService", "groupId: " + remoteMessage.getData().get("groupId"));
                Log.d("FbMessagingService", "newStartDateTime: " + remoteMessage.getData().get("newDateTime"));
                TimeNotificationScheduler.updateReminder(this,remoteMessage.getData().get("groupId"), null, Long.parseLong(remoteMessage.getData().get("newDateTime")), TimeNotificationScheduler.DELAY_2HRS);
            }
            else if (remoteMessage.getData().get("title").equals("Name Change Detected")) {
                Log.d("FbMessagingService", "groupId: " + remoteMessage.getData().get("groupId"));
                Log.d("FbMessagingService", "newName: " + remoteMessage.getData().get("newName"));
                TimeNotificationScheduler.updateReminder(this,remoteMessage.getData().get("groupId"), remoteMessage.getData().get("newName"), -1, TimeNotificationScheduler.DELAY_2HRS);
            }

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
            } else {
                // Handle message within 10 seconds
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String notificationTitle = remoteMessage.getNotification().getTitle();
            String notificationMessage = remoteMessage.getNotification().getBody();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)   // to change
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationMessage)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}
