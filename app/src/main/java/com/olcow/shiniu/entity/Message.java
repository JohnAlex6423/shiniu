package com.olcow.shiniu.entity;

public class Message {

    public static final int RECIPIENT = 0;
    public static final int SEND = 1;

    private String content;
    private String time;
    private int fromId;
    private int sendOrRecipient;

    public Message(String content, String time, int fromId, int sendOrRecipient) {
        this.content = content;
        this.time = time;
        this.fromId = fromId;
        this.sendOrRecipient = sendOrRecipient;
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

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getSendOrRecipient() {
        return sendOrRecipient;
    }

    public void setSendOrRecipient(int sendOrRecipient) {
        this.sendOrRecipient = sendOrRecipient;
    }

}
