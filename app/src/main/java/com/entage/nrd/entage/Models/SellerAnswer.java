package com.entage.nrd.entage.Models;

public class SellerAnswer {

    private String writer_id;
    private String user_name;

    private String item_id;

    private String question_id;
    private String answer;
    private String time_create;

    private boolean is_seen;

    public SellerAnswer() {

    }

    public SellerAnswer(String writer_id, String user_name, String item_id, String question_id, String answer, String time_create, boolean is_seen) {
        this.writer_id = writer_id;
        this.user_name = user_name;
        this.item_id = item_id;
        this.question_id = question_id;
        this.answer = answer;
        this.time_create = time_create;
        this.is_seen = is_seen;
    }

    public String getWriter_id() {
        return writer_id;
    }

    public void setWriter_id(String writer_id) {
        this.writer_id = writer_id;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTime_create() {
        return time_create;
    }

    public void setTime_create(String time_create) {
        this.time_create = time_create;
    }

    public boolean isIs_seen() {
        return is_seen;
    }

    public void setIs_seen(boolean is_seen) {
        this.is_seen = is_seen;
    }
}
