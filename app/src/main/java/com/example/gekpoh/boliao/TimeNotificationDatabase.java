package com.example.gekpoh.boliao;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {TimeNotification.class}, version = 1, exportSchema = false)
public abstract class TimeNotificationDatabase extends RoomDatabase{

    private static TimeNotificationDatabase INSTANCE;

    public abstract TimeNotificationDao timeNotificationDao();

    public static TimeNotificationDatabase getTimeNotificationDatabase(Context context){
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    TimeNotificationDatabase.class, "time-notifications-database").build();
        }
        return INSTANCE;
    }

    public static void destroyInstance(){
        INSTANCE = null;
    }
}
