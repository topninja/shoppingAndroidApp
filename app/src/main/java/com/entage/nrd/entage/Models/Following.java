package com.entage.nrd.entage.Models;

public class Following {

    private String user_id;
    private String entage_page_id;
    private String date_following;
    private boolean is_notifying;

    public Following() {

    }

    public Following(String user_id, String entage_page_id, String date_following, boolean is_notifying) {
        this.user_id = user_id;
        this.entage_page_id = entage_page_id;
        this.date_following = date_following;
        this.is_notifying = is_notifying;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEntage_page_id() {
        return entage_page_id;
    }

    public void setEntage_page_id(String entage_page_id) {
        this.entage_page_id = entage_page_id;
    }

    public String getDate_following() {
        return date_following;
    }

    public void setDate_following(String date_following) {
        this.date_following = date_following;
    }

    public boolean isIs_notifying() {
        return is_notifying;
    }

    public void setIs_notifying(boolean is_notifying) {
        this.is_notifying = is_notifying;
    }

    @Override
    public String toString() {
        return "Following{" +
                "user_id='" + user_id + '\'' +
                ", entage_page_id='" + entage_page_id + '\'' +
                ", date_following='" + date_following + '\'' +
                ", is_notifying=" + is_notifying +
                '}';
    }
}
