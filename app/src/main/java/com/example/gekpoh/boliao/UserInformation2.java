package com.example.gekpoh.boliao;

// UserInformation with isAdmin true/false and userId
public class UserInformation2 {
    private UserInformation userInformation;
    private boolean isAdmin;
    private String userId;

    public UserInformation2(UserInformation userInformation, boolean isAdmin, String userId) {
        this.userInformation = userInformation;
        this.isAdmin = isAdmin;
        this.userId = userId;
    }

    public UserInformation getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(UserInformation userInformation) { this.userInformation = userInformation; }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
}