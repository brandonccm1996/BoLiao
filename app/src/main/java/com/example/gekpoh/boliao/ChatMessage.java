package com.example.gekpoh.boliao;

public class ChatMessage {
    private String text;
    private String uid;
    private long timeStamp;

    public ChatMessage(){

    }
    public ChatMessage(String text, String uid, long timeStamp) {
        this.text = text;
        this.uid = uid;
        this.timeStamp = timeStamp;
    }
    public String getText() {
        return text;
    }

    public String getUid() {return uid;}

    public long getTimeStamp(){return timeStamp;}
}
