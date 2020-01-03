package com.entage.nrd.entage.Models;

public class ChangeUsername {

    private String user_id;
    private String previous_username ;
    private String new_username;
    private String date;

    public ChangeUsername(String user_id, String previous_username, String new_username, String date) {
        this.user_id = user_id;
        this.previous_username = previous_username;
        this.new_username = new_username;
        this.date = date;
    }

    public ChangeUsername() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPrevious_username() {
        return previous_username;
    }

    public void setPrevious_username(String previous_username) {
        this.previous_username = previous_username;
    }

    public String getNew_username() {
        return new_username;
    }

    public void setNew_username(String new_username) {
        this.new_username = new_username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ChangeUsername{" +
                "user_id='" + user_id + '\'' +
                ", previous_username='" + previous_username + '\'' +
                ", new_username='" + new_username + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
