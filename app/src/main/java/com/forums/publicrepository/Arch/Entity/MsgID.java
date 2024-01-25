package com.forums.publicrepository.Arch.Entity;

public class MsgID {
    private String id, mID;

    public MsgID(String id, String mID) {
        this.id = id;
        this.mID = mID;
    }

    public MsgID() {}

    public String getId() {
        return id;
    }

    public String getmID() {
        return mID;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setmID(String mID) {
        this.mID = mID;
    }
}
