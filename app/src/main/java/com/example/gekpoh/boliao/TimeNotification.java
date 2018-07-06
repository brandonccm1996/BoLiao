package com.example.gekpoh.boliao;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"groupId"}, unique = true)})
public class TimeNotification {
    @PrimaryKey
    public int id;
    public String groupId;
    public String activityName;
    public long activityTime;
    public long notificationDelay;
}
