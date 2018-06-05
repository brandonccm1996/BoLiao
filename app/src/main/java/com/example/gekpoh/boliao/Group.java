package com.example.gekpoh.boliao;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Group implements Parcelable{
    private String name, place;
    private Date date;
    private Bitmap groupIcon;

    public Group(String name, String place) {
        this.name = name;
        this.place = place;

    }
    private Group(Parcel in) {
        name = in.readString();
        place = in.readString();
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
        return name;
    }

    public String getPlace(){
        //Placeholder for now. Need to use Google Maps API next time
        return place;
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
        dest.writeString(name);
        dest.writeString(place);
        dest.writeValue(groupIcon);
    }
}
