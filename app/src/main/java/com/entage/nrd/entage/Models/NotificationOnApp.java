package com.entage.nrd.entage.Models;



public class NotificationOnApp {

    private String entage_page_id;
    private String item_id;

    private String token_id;
    private String topic;

    private String title;
    private String body;
    private String flag;
    private String sender_id;
    private String receiver_id;

    private String extra_data;
    private String time;
    private int priority;
    private boolean is_read;




    public NotificationOnApp() {

    }

    public NotificationOnApp(String entage_page_id, String item_id, String token_id, String topic, String title, String body,
                             String flag, String sender_id, String receiver_id, String extra_data, String time, int priority,
                             boolean is_read) {
        this.entage_page_id = entage_page_id;
        this.item_id = item_id;
        this.token_id = token_id;
        this.topic = topic;
        this.title = title;
        this.body = body;
        this.flag = flag;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.extra_data = extra_data;
        this.time = time;
        this.priority = priority;
        this.is_read = is_read;
    }

    public String getEntage_page_id() {
        return entage_page_id;
    }

    public void setEntage_page_id(String entage_page_id) {
        this.entage_page_id = entage_page_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isIs_read() {
        return is_read;
    }

    public void setIs_read(boolean is_read) {
        this.is_read = is_read;
    }

    @Override
    public String toString() {
        return "NotificationOnApp{" +
                "entage_page_id='" + entage_page_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", token_id='" + token_id + '\'' +
                ", topic='" + topic + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", flag='" + flag + '\'' +
                ", sender_id='" + sender_id + '\'' +
                ", receiver_id='" + receiver_id + '\'' +
                ", extra_data='" + extra_data + '\'' +
                ", time='" + time + '\'' +
                ", priority=" + priority +
                ", is_read=" + is_read +
                '}';
    }
}
