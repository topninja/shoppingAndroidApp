package com.entage.nrd.entage.Models;

public class ChangeEmail {

    private String user_id;
    private String previous_email ;
    private String new_email;
    private String date;

    public ChangeEmail(String user_id, String previous_email, String new_email, String date) {
        this.user_id = user_id;
        this.previous_email = previous_email;
        this.new_email = new_email;
        this.date = date;
    }

    public ChangeEmail() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPrevious_email() {
        return previous_email;
    }

    public void setPrevious_email(String previous_email) {
        this.previous_email = previous_email;
    }

    public String getNew_email() {
        return new_email;
    }

    public void setNew_email(String new_email) {
        this.new_email = new_email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ChangeEmail{" +
                "user_id='" + user_id + '\'' +
                ", previous_email='" + previous_email + '\'' +
                ", new_email='" + new_email + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
