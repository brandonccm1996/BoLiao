package com.example.gekpoh.boliao;

public class UserLists {
    private boolean isAdmin;
    private boolean isOrganizer;
    public UserLists() {

    }

    public UserLists(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean getIsAdmin() { return isAdmin; }
    public boolean getIsOrganizer() { return isOrganizer; }
}
