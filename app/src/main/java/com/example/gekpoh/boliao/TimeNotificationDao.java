package com.example.gekpoh.boliao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TimeNotificationDao {
    @Query("SELECT * FROM TimeNotification")
    List<TimeNotification> getAllNotification();

    @Insert
    void insert(TimeNotification o);

    @Delete
    void delete(TimeNotification o);

    @Query("DELETE FROM TimeNotification WHERE groupId = :id")
    void deleteById(String id);

    @Query("Select * FROM TimeNotification WHERE groupId = :id")
    TimeNotification getNotificationById(String id);

    @Query("DELETE FROM TimeNotification")
    public void nukeTable();
}
