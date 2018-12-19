package com.olcow.shiniu.entity;

public class MessagePro {

    private String content;
    private long date;

    public MessagePro(String content, long date) {
        this.content = content;
        this.date = date;
    }

    public MessagePro() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
