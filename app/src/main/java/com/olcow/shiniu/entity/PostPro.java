package com.olcow.shiniu.entity;

public class PostPro {
    private int postid;
    private int uid;
    private String content;
    private String imgs;
    private long date;
    private String name;
    private String avatar;

    public PostPro(int postid, int uid, String content, String imgs, long date,String name, String avatar) {
        this.postid = postid;
        this.uid = uid;
        this.content = content;
        this.imgs = imgs;
        this.date = date;
        this.name = name;
        this.avatar = avatar;
    }

    public PostPro() {
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getPostid() {
        return postid;
    }

    public void setPostid(int postid) {
        this.postid = postid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgs() {
        return imgs;
    }

    public void setImgs(String imgs) {
        this.imgs = imgs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
