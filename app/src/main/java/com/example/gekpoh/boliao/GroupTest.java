package com.example.gekpoh.boliao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// FOR TESTING PURPOSES
public class GroupTest {
    public String name;
    public String location;
    public String description;
    public String photoUrl;
    public int numPeople;
    public SimpleDateFormat groupDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public Date sDate;
    public Date eDate;

    public GroupTest() {

    }

    public GroupTest(String name, String location, String description, String photoUrl, int numPeople, String sDate, String eDate) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.photoUrl = photoUrl;
        this.numPeople = numPeople;
        try {
            this.sDate = groupDateFormatter.parse(sDate);
            this.eDate = groupDateFormatter.parse(eDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        if (name != null) return name;
        else return "DEBUGGING DEBUGGING";
    }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public int getNumPeople() { return numPeople; }
    public void setNumPeople(int numPeople) { this.numPeople = numPeople; }
    public Date getsDate() { return sDate; }
    public void setsDate(Date sDate) { this.sDate = sDate; }
    public Date geteDate() { return eDate; }
    public void seteDate(Date eDate) { this.eDate = eDate; }
}

