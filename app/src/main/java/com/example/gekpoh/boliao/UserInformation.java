package com.example.gekpoh.boliao;

public class UserInformation {
    private String name;
    private String description;
    private String photoUrl;
    private float rating;

    public UserInformation() {

    }

    public UserInformation(String username, String description, String photoUrl, float rating) {
        this.name = username;
        this.description = description;
        this.photoUrl = photoUrl;
        this.rating = rating;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}