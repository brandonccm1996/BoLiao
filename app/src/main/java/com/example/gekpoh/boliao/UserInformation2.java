package com.example.gekpoh.boliao;

public class UserInformation2 {
    private UserInformation userInformation;
    private boolean enableRemove;   // enable/disable remove button
    private boolean memberIsAdmin;
    private String userId;
    private boolean memberRatedBefore;
    private boolean userIsAdmin;
    private boolean inEvent;

    public UserInformation2(UserInformation userInformation, boolean enableRemove, boolean memberIsAdmin, boolean userIsAdmin, String userId, boolean memberRatedBefore, boolean inEvent) {
        this.userInformation = userInformation;
        this.enableRemove = enableRemove;
        this.memberIsAdmin = memberIsAdmin;
        this.userIsAdmin = userIsAdmin;
        this.userId = userId;
        this.memberRatedBefore = memberRatedBefore;
        this.inEvent = inEvent;
    }

    public UserInformation getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(UserInformation userInformation) { this.userInformation = userInformation; }

    public boolean getEnableRemove() { return enableRemove; }

    public void setEnableRemove(boolean enableRemove) { this.enableRemove = enableRemove; }

    public boolean getMemberIsAdmin() { return memberIsAdmin; }

    public void setMemberIsAdmin(boolean memberIsAdmin) { this.memberIsAdmin = memberIsAdmin; }

    public boolean getUserIsAdmin() { return userIsAdmin; }

    public void setUserIsAdmin(boolean userIsAdmin) { this.userIsAdmin = userIsAdmin; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public boolean getMemberRatedBefore() { return memberRatedBefore; }

    public void setMemberRatedBefore(boolean memberRatedBefore) { this.memberRatedBefore = memberRatedBefore; }

    public boolean getInEvent() { return inEvent; }

    public void setInEvent(boolean inEvent) { this.inEvent = inEvent; }
}