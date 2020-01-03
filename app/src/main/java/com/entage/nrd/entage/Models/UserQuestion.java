package com.entage.nrd.entage.Models;

public class UserQuestion {

    private String user_id;
    private String question_id;
    private String item_id;


    public UserQuestion() {

    }

    public UserQuestion(String user_id, String question_id, String item_id) {
        this.user_id = user_id;
        this.question_id = question_id;
        this.item_id = item_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }
}
