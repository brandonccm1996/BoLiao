package com.example.gekpoh.boliao;

public class UserInformation2 {
    private UserInformation userInformation;
    private String memberId;
    private boolean inEvent;
    private boolean memberRatedBefore;
    private String memberStatus;
    private String userStatus;

    public UserInformation2(UserInformation userInformation, String memberId, boolean inEvent, boolean memberRatedBefore, String memberStatus, String userStatus) {
        this.userInformation = userInformation;
        this.memberId = memberId;
        this.inEvent = inEvent;
        this.memberRatedBefore = memberRatedBefore;
        this.memberStatus = memberStatus;
        this.userStatus = userStatus;
    }

    public UserInformation getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(UserInformation userInformation) {
        this.userInformation = userInformation;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean getInEvent() {
        return inEvent;
    }

    public void setInEvent(boolean inEvent) {
        this.inEvent = inEvent;
    }

    public boolean getMemberRatedBefore() {
        return memberRatedBefore;
    }

    public void setMemberRatedBefore(boolean memberRatedBefore) {
        this.memberRatedBefore = memberRatedBefore;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}