package com.example.gekpoh.boliao;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseUtils {
    private static FirebaseDatabase database;
    public static FirebaseDatabase getDatabase(){
        if(database == null){
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }
}
