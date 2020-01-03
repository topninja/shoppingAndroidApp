package com.entage.nrd.entage.Models;

public class CardItem_1 {

    private String img_url;
    private boolean is_from_db;

    public CardItem_1() {
    }

    public CardItem_1(String img_url, boolean is_from_db) {
        this.img_url = img_url;
        this.is_from_db = is_from_db;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public boolean isIs_from_db() {
        return is_from_db;
    }

    public void setIs_from_db(boolean is_from_db) {
        this.is_from_db = is_from_db;
    }
}

