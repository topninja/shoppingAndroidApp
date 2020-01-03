package com.entage.nrd.entage.Models;

public class SubscribeTopic {

    private String user_id;
    private String topic;
    private boolean status;

    public SubscribeTopic() {

    }

    public SubscribeTopic(String user_id, String topic, boolean status) {
        this.user_id = user_id;
        this.topic = topic;
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
