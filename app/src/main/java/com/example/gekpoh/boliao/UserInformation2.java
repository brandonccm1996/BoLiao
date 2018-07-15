package com.example.gekpoh.boliao;

public class UserInformation2 {
    private UserInformation userInformation;
    private boolean enableRemove;   // enable/disable remove button
    private boolean memberIsAdmin;
    private String userId;

    public UserInformation2(UserInformation userInformation, boolean enableRemove, boolean memberIsAdmin, String userId) {
        this.userInformation = userInformation;
        this.enableRemove = enableRemove;
        this.memberIsAdmin = memberIsAdmin;
        this.userId = userId;
    }

    public UserInformation getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(UserInformation userInformation) { this.userInformation = userInformation; }

    public boolean getEnableRemove() { return enableRemove; }

    public void setEnableRemove(boolean enableRemove) { this.enableRemove = enableRemove; }

    public boolean getMemberIsAdmin() { return memberIsAdmin; }

    public void setMemberIsAdmin(boolean memberIsAdmin) { this.memberIsAdmin = memberIsAdmin; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
}