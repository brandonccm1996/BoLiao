package com.example.gekpoh.boliao;

public class UserInformation {
    private String name;
    private String description;
    private String photoUrl;
    private float sumRating;
    private int numRatings;

    public UserInformation() {

    }

    public UserInformation(String username, String description, String photoUrl, float sumRating, int numRatings) {
        this.name = username;
        this.description = description;
        this.photoUrl = photoUrl;
        this.sumRating = sumRating;
        this.numRatings = numRatings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public float getSumRating() {
        return sumRating;
    }

    public void setSumRating(float sumRating) {
        this.sumRating = sumRating;
    }

    public int getNumRatings() { return numRatings; }

    public void setNumRatings(int numRatings) { this.numRatings = numRatings; }
}