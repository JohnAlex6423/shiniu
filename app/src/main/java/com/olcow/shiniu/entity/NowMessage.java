package com.olcow.shiniu.entity;

public class NowMessage {

    private String name;
    private String content;
    private String time;
    private String avatar;
    private int redBadge;

    public NowMessage() {
    }

    public NowMessage(String name, String content, String time, String avatar, int redBadge) {
        this.name = name;
        this.content = content;
        this.time = time;
        this.avatar = avatar;
        this.redBadge = redBadge;
    }

    public int getRedbadge() {
        return redBadge;
    }

    public void setRedbadge(int redbadge) {
        this.redBadge = redbadge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
