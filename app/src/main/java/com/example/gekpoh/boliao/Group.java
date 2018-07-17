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
    public static final SimpleDateFormat groupDateFormatter = new SimpleDateFormat("E, dd/MM/yyyy hh:mma");
    public static final SimpleDateFormat groupDateFormatter2 = new SimpleDateFormat("dd/MM/yyyy hh:mma");
    public static final SimpleDateFormat groupDateFormatter3 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String TAG = "GROUP";
    private String chatId;//mGroupId is to indicate the reference to the database for this group. groupid is also used as chatid
    private String names, location, placeId, photoUrl, description;
    private long startDateTime, endDateTime;//timestamp
    private int maxParticipants, numParticipants;
    private String organizerId;

    public Group(){

    }
    private Group(Parcel in) {
        names = in.readString();
        location = in.readString();
        placeId = in.readString();
        chatId = in.readString();
        photoUrl = in.readString();
        description = in.readString();
        startDateTime = in.readLong();
        endDateTime = in.readLong();
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
    public Long getStartDateTime(){
        return startDateTime;
    }
    public Long getEndDateTime(){ return endDateTime; }
    public int getMaxParticipants(){ return maxParticipants; }
    public int getNumParticipants(){ return numParticipants; }
    public String getChatId(){
        return chatId;
    }
    public String getOrganizerId() { return organizerId; }
    public String getStartDateTimeString() {
        Date date = new Date(startDateTime);
        return groupDateFormatter.format(date);
    }
    public String getEndDateTimeString() {
        Date date = new Date(endDateTime);
        return groupDateFormatter.format(date);
    }
    public String getStartDateTimeString2() {
        Date date = new Date(startDateTime);
        return groupDateFormatter2.format(date);
    }
    public String getEndDateTimeString2() {
        Date date = new Date(endDateTime);
        return groupDateFormatter2.format(date);
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
        dest.writeLong(startDateTime);//Converting it to long is better than writeSerializable
        dest.writeLong(endDateTime);
        dest.writeInt(numParticipants);
        dest.writeInt(maxParticipants);
        dest.writeString(organizerId);
    }

    //Sort by date
    @Override
    public int compareTo(@NonNull Group o) {
        return startDateTime<o.startDateTime?-1:startDateTime>o.startDateTime?1:0;
    }
}
