package com.forums.publicrepository.Arch.Entity;

public class User {
    private String uid, policy;

    public User(String uid, String policy) {
        this.uid = uid;
        this.policy = policy; //Accepted the policy or not.
    }
    public User(){}
    public String getUid() {
        return uid;
    }
    public String getPolicy() {
        return policy;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setPolicy(String policy) {
        this.policy = policy;
    }
}