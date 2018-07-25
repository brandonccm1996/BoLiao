package com.example.gekpoh.boliao;

public class UserInformation {
    private String name;
    private String description;
    private String photoUrl;
    private String devicetoken;
    private boolean updateNotifEnabled;

    public UserInformation() {

    }

    public UserInformation(String username, String description, String photoUrl, String devicetoken, boolean updateNotifEnabled) {
        this.name = username;
        this.description = description;
        this.photoUrl = photoUrl;
        this.devicetoken = devicetoken;
        this.updateNotifEnabled = updateNotifEnabled;
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

    public String getDevicetoken() { return devicetoken; }

    public void setDevicetoken(String devicetoken) { this.devicetoken = devicetoken; }

    public boolean getUpdateNotifEnabled() { return updateNotifEnabled; }

    public void setUpdateNotifEnabled(boolean updateNotifEnabled) { this.updateNotifEnabled = updateNotifEnabled; }
}