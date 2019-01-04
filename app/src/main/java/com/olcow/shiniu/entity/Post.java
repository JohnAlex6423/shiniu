package com.olcow.shiniu.entity;

import java.util.List;

public class Post {

    private int postId;
    private int uid;
    private String content;
    private List<String> imgs;
    private String avatar;
    private String name;
    private long date;

    public Post() {
    }

    public Post(int postId, int uid, String content, List<String> imgs,long date,String name, String avatar) {
        this.postId = postId;
        this.uid = uid;
        this.content = content;
        this.imgs = imgs;
        this.date = date;
        this.name = name;
        this.avatar = avatar;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
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

    public List<String> getImgs() {
        return imgs;
    }

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
