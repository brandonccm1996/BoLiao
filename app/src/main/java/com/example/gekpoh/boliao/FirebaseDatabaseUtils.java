package com.example.gekpoh.boliao;

import android.provider.ContactsContract;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDatabaseUtils {
    private static FirebaseDatabase database;
    private static boolean connection = false;
    public static FirebaseDatabase getDatabase(){
        if(database == null){
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }
        return database;
    }

    public static void setUpConnectionListener(){
        DatabaseReference connectedRef = FirebaseDatabaseUtils.getDatabase().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected= false;
                try {
                    connected = snapshot.getValue(Boolean.class);
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                if (connected) {
                    connection = true;
                } else {
                    connection = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    public static boolean connectedToDatabase(){
        return connection;
    }

    public static GeoFire getGeoFireInstance(){
        FirebaseDatabase database = getDatabase();
        DatabaseReference geofireref = database.getReference().child("geoFireObjects");
        return new GeoFire(geofireref);
    }
}
