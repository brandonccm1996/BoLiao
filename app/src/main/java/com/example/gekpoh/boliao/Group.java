package com.example.gekpoh.boliao;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Group implements Parcelable,Comparable<Group>{
    private static final SimpleDateFormat groupDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String TAG = "GROUP";
    private String chatId;//mGroupId is to indicate the reference to the database for this group. groupid is also used as chatid
    private String names, location, placeId, photoUrl, description;
    private String startDateTime, endDateTime;//timestamp
    private int maxParticipants, numParticipants;
    private String organizerId;

    public Group(){

    }

    //Constructor for creating new Activity
    public Group(String groupid, String name, String placename, String placeid, String photourl, String description ,String startdate, String enddate, int currentsize, int maxsize, String organizerId) {
        this.chatId = groupid;
        this.names = name;
        this.location = placename;
        this.placeId = placeid;
        this.photoUrl = photourl;
        this.description = description;
        this.startDateTime = startdate;
        this.endDateTime = enddate;
        this.numParticipants = currentsize;
        this.maxParticipants = maxsize;
        this.organizerId = organizerId;
    }
    private Group(Parcel in) {
        names = in.readString();
        location = in.readString();
        placeId = in.readString();
        chatId = in.readString();
        photoUrl = in.readString();
        description = in.readString();
        startDateTime = in.readString();
        endDateTime = in.readString();
        numParticipants = in.readInt();
        maxParticipants = in.readInt();
        organizerId = in.readString();
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

    public String getNames(){
        return names;
    }
    public String getPlaceId(){
        return placeId;
    }
    public String getLocation(){ return location; }
    public String getDescription(){ return description; }
    public String getPhotoUrl() {
        return photoUrl;
    }
    public String getStartDateTime(){
        return startDateTime;
    }
    public String getEndDateTime(){ return endDateTime; }
    public int getMaxParticipants(){ return maxParticipants; }
    public int getNumParticipants(){ return numParticipants; }
    public String getChatId(){
        return chatId;
    }
    public String getOrganizerId() { return organizerId; }
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(names);
        dest.writeString(location);
        dest.writeString(placeId);
        dest.writeString(chatId);
        dest.writeString(photoUrl);
        dest.writeString(description);
        dest.writeString(startDateTime);//Converting it to long is better than writeSerializable
        dest.writeString(endDateTime);
        dest.writeInt(numParticipants);
        dest.writeInt(maxParticipants);
        dest.writeString(organizerId);
    }

    //Sort by date
    @Override
    public int compareTo(@NonNull Group o) {
        Long date1, date2;
        try {
            date1 = groupDateFormatter.parse(startDateTime).getTime();
            date2 = groupDateFormatter.parse(o.getStartDateTime()).getTime();
        }catch (ParseException e){
            Log.e(TAG,"Date parsing failed failed");
            return 0;
        }
        return date1<date2?-1:date1>date2?1:0;
    }
}
