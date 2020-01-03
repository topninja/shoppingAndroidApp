package com.entage.nrd.entage.Models;

public class GarbageUid {

    String uid;
    String date;
    String email_phone;

    public GarbageUid(String uid, String date, String email_phone) {
        this.uid = uid;
        this.date = date;
        this.email_phone = email_phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail_phone() {
        return email_phone;
    }

    public void setEmail_phone(String email_phone) {
        this.email_phone = email_phone;
    }

    @Override
    public String toString() {
        return "GarbageUid{" +
                "uid='" + uid + '\'' +
                ", date='" + date + '\'' +
                ", email_phone='" + email_phone + '\'' +
                '}';
    }
}
