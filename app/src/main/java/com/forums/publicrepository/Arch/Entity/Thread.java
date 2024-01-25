package com.forums.publicrepository.Arch.Entity;

import androidx.annotation.Nullable;

public class Thread {
    private String id, title, body, imgURL, msgLoc;
    private long creationTime;

    public Thread(String id, @Nullable String title, String body,
                  @Nullable String imgURL, String msgLoc, long creationTime) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.imgURL = imgURL;
        this.msgLoc = msgLoc;
        this.creationTime = creationTime;
    }
    public Thread(){}

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getImgURL() {
        return imgURL;
    }

    public String getMsgLoc() {
        return msgLoc;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public void setMsgLoc(String msgLoc) {
        this.msgLoc = msgLoc;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
