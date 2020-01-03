package com.entage.nrd.entage.Models;

public class Account {

    String user_id;
    String time_create;
    String type;
    int version_code;
    String version_name;


    public Account(String user_id, String time_create, String type, int version_code, String version_name) {
        this.user_id = user_id;
        this.time_create = time_create;
        this.type = type;
        this.version_code = version_code;
        this.version_name = version_name;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTime_create() {
        return time_create;
    }

    public void setTime_create(String time_create) {
        this.time_create = time_create;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Account{" +
                "user_id='" + user_id + '\'' +
                ", time_create='" + time_create + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
