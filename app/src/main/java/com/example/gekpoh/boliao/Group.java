package com.example.gekpoh.boliao;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Group implements Parcelable{
    public static final SimpleDateFormat groupDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private String groupid;//mGroupId is to indicate the reference to the database for this group
    private String name, placename, placeid, photourl, description;
    private long startdate, enddate;//timestamp
    private int maxsize;
    private ArrayList<String> uids;
    //New Activity Constructor
    private Group(String name, String placename, String placeid, String description ,Long startdate, Long enddate, int maxsize) {
        this.name = name;
        this.placename = placename;
        this.placeid = placeid;
        this.description = description;
        this.startdate = startdate;
        this.enddate = enddate;
        this.maxsize = maxsize;
    }
    private Group(Parcel in) {
        name = in.readString();
        placename = in.readString();
        placeid = in.readString();
        groupid = in.readString();
        photourl = in.readString();
        description = in.readString();
        startdate = in.readLong();
        enddate = in.readLong();
        maxsize = in.readInt();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    public String getName(){
        return name;
    }

    public String getPlaceId(){
        return placeid;
    }
    public String getPlaceName(){
        //Placeholder for now. Need to use Google Maps API next time
        return placename;
    }
    public String getPhotoUrl() {
        return photourl;
    }
    public String getStartDate(){
        return groupDateFormatter.format(new Date(startdate));
    }
    public String getEndDate(){
        return groupDateFormatter.format(new Date(enddate));
    }
    public String getGroupId(){
        return groupid;
    }
    public void setGroupId(String id){
        groupid = id;
    }
    //public method to obtain group from groupId
    public static void getGroupfromId(final String groupid, final ArrayList<Group> groupList){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Need to decide how to obtain the data from database, I dont think the code below works
                //Group group = dataSnapshot.child(groupid).getValue(Group.class);
                //group.setGroupId(groupid);
                //groupList.add(group);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    //public method to create a new Activity
    public static void createGroup(final ArrayList<Group> groupList){
        
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(placename);
        dest.writeString(placeid);
        dest.writeString(groupid);
        dest.writeString(description);
        dest.writeLong(startdate);//Converting it to long is better than writeSerializable
        dest.writeLong(enddate);
        dest.writeInt(maxsize);
    }
}
