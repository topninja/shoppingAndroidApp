package com.entage.nrd.entage.Models;

public class Vote {

    private String user_id;

    public Vote(String user_id) {
        this.user_id = user_id;
    }

    public Vote() {

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Like{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
