package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Notification implements Parcelable{

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
    private String img_url;
    private String notify_id;

    public Notification() {

    }

    public Notification(String entage_page_id, String item_id, String token_id, String topic,
                        String title, String body, String flag, String sender_id, String receiver_id,
                        String extra_data, String img_url, String notify_id) {
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
        this.img_url = img_url;
        this.notify_id = notify_id;
    }

    protected Notification(Parcel in) {
        entage_page_id = in.readString();
        item_id = in.readString();
        token_id = in.readString();
        topic = in.readString();
        title = in.readString();
        body = in.readString();
        flag = in.readString();
        sender_id = in.readString();
        receiver_id = in.readString();
        extra_data = in.readString();
        img_url = in.readString();
        notify_id = in.readString();
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

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

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getNotify_id() {
        return notify_id;
    }

    public void setNotify_id(String notify_id) {
        this.notify_id = notify_id;
    }

    @Override
    public String toString() {
        return "Notification{" +
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
                ", img_url='" + img_url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entage_page_id);
        dest.writeString(item_id);
        dest.writeString(token_id);
        dest.writeString(topic);
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(flag);
        dest.writeString(sender_id);
        dest.writeString(receiver_id);
        dest.writeString(extra_data);
        dest.writeString(img_url);
        dest.writeString(notify_id);
    }
}
