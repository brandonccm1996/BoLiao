package com.example.gekpoh.boliao;

public class ChatMessage {
    private String text;
    private String name;
    private long timeStamp;
    public ChatMessage(String text, String name, String photoUrl, long timeStamp) {
        this.text = text;
        this.name = name;
        this.timeStamp = timeStamp;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeStamp(long timeStamp){this.timeStamp = timeStamp;}

    public long getTimeStamp(){return timeStamp;}
}
