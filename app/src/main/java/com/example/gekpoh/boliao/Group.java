package com.example.gekpoh.boliao;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Group implements Parcelable{
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private String mName, mPlace;
    private Date mStartDate, mEndDate;
    private Bitmap groupIcon;

    public Group(String name, String place, Date startdate, Date enddate) {
        mName = name;
        mPlace = place;
        mStartDate = startdate;
        mEndDate = enddate;
    }
    private Group(Parcel in) {
        mName = in.readString();
        mPlace = in.readString();
        mStartDate = new Date(in.readLong());
        mEndDate = new Date(in.readLong());
        groupIcon = in.readParcelable(Bitmap.class.getClassLoader());
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
        return mName;
    }

    public String getPlace(){
        //Placeholder for now. Need to use Google Maps API next time
        return mPlace;
    }

    public String getStartDate(){
        return formatter.format(mStartDate);
    }
    public String getEndDate(){
        return formatter.format(mEndDate);
    }
    public Bitmap getIcon() {
        return groupIcon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPlace);
        dest.writeLong(mStartDate.getTime());//Converting it to long is better than writeSerializable
        dest.writeLong(mEndDate.getTime());
        dest.writeValue(groupIcon);
    }
}
