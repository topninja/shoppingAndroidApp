package com.entage.nrd.entage.Models;

public class Message {

    private String message_id;
    private String user_id;
    private String message;
    private String extra_text_1;
    private String extra_text_2;
    private MyAddress address;
    private String date;
    private String language;
    private boolean is_deleted;
    private boolean is_sent;
    private boolean is_read;
    private boolean is_cancelled;

    public Message() {
    }

    public Message(String message_id, String user_id, String message, String extra_text_1, String extra_text_2,
                   String date, String language, boolean is_deleted, boolean is_sent, boolean is_read, boolean is_cancelled) {
        this.message_id = message_id;
        this.user_id = user_id;
        this.message = message;
        this.extra_text_1 = extra_text_1;
        this.extra_text_2 = extra_text_2;
        this.date = date;
        this.language = language;
        this.is_deleted = is_deleted;
        this.is_sent = is_sent;
        this.is_read = is_read;
        this.is_cancelled = is_cancelled;
    }

    public Message(String message_id, String user_id, String message, String extra_text_1, String extra_text_2, MyAddress address, String date,
                   String language, boolean is_deleted,
                   boolean is_sent, boolean is_read, boolean is_cancelled) {
        this.message_id = message_id;
        this.user_id = user_id;
        this.message = message;
        this.extra_text_1 = extra_text_1;
        this.extra_text_2 = extra_text_2;
        this.address = address;
        this.date = date;
        this.language = language;
        this.is_deleted = is_deleted;
        this.is_sent = is_sent;
        this.is_read = is_read;
        this.is_cancelled = is_cancelled;
    }

    public boolean isIs_cancelled() {
        return is_cancelled;
    }

    public void setIs_cancelled(boolean is_cancelled) {
        this.is_cancelled = is_cancelled;
    }

    public boolean isIs_sent() {
        return is_sent;
    }

    public void setIs_sent(boolean is_sent) {
        this.is_sent = is_sent;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public String getExtra_text_1() {
        return extra_text_1;
    }

    public void setExtra_text_1(String extra_text_1) {
        this.extra_text_1 = extra_text_1;
    }

    public String getExtra_text_2() {
        return extra_text_2;
    }

    public void setExtra_text_2(String extra_text_2) {
        this.extra_text_2 = extra_text_2;
    }

    public MyAddress getAddress() {
        return address;
    }

    public void setAddress(MyAddress address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message_id='" + message_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", message='" + message + '\'' +
                ", extra_text_1='" + extra_text_1 + '\'' +
                ", extra_text_2='" + extra_text_2 + '\'' +
                ", date='" + date + '\'' +
                ", language='" + language + '\'' +
                ", is_deleted=" + is_deleted +
                ", is_sent=" + is_sent +
                ", is_read=" + is_read +
                ", is_cancelled=" + is_cancelled +
                '}';
    }
}
