package com.example.gekpoh.boliao;

public class ChatMessage {
    private String text;
    private String uid;
    private String photoUrl;
    private long timeStamp;

    public ChatMessage(){

    }
    public ChatMessage(String text, String uid, String photoUrl, long timeStamp) {
        this.text = text;
        this.uid = uid;
        this.timeStamp = timeStamp;
        this.photoUrl = photoUrl;
    }
    public String getText() {
        return text;
    }

    public String getUid() {return uid;}

    public String getPhotoUrl() {
        return photoUrl;
    }

    public long getTimeStamp(){return timeStamp;}
}
