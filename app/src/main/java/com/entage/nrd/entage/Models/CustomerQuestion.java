package com.entage.nrd.entage.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomerQuestion {

    private String user_id;
    private String user_name;

    private String item_id;

    private String question_id;
    private String question;
    private String time_create;

    private boolean is_read;

    private HashMap<String, String> notify_ids;

    public CustomerQuestion() {

    }

    public CustomerQuestion(String user_id, String user_name, String item_id, String question_id, String question, String time_create, boolean is_read, HashMap<String, String> notify_ids) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.item_id = item_id;
        this.question_id = question_id;
        this.question = question;
        this.time_create = time_create;
        this.is_read = is_read;
        this.notify_ids = notify_ids;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getTime_create() {
        return time_create;
    }

    public void setTime_create(String time_create) {
        this.time_create = time_create;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public HashMap<String, String> getNotify_ids() {
        return notify_ids;
    }

    public void setNotify_ids(HashMap<String, String> notify_ids) {
        this.notify_ids = notify_ids;
    }

    @Override
    public String toString() {
        return "CustomerQuestion{" +
                "user_id='" + user_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", item_id='" + item_id + '\'' +
                ", question_id='" + question_id + '\'' +
                ", question='" + question + '\'' +
                ", time_create='" + time_create + '\'' +
                ", is_read=" + is_read +
                ", notify_ids=" + notify_ids +
                '}';
    }
}


