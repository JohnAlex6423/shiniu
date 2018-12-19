package com.olcow.shiniu.entity;

public class Message {

    public static final int RECIPIENT = 0;
    public static final int SEND = 1;
    public static final int NOSHOWTIME = 1;
    public static final int ISSHOWTIME = 0;

    private String content;
    private String time;
    private int sendOrRecipient;
    private int showTime;

    public Message(String content, String time, int sendOrRecipient,int showTime) {
        this.content = content;
        this.time = time;
        this.sendOrRecipient = sendOrRecipient;
        this.showTime = showTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSendOrRecipient() {
        return sendOrRecipient;
    }

    public void setSendOrRecipient(int sendOrRecipient) {
        this.sendOrRecipient = sendOrRecipient;
    }

    public int getShowTime() {
        return showTime;
    }

    public void setShowTime(int showTime) {
        this.showTime = showTime;
    }
}
