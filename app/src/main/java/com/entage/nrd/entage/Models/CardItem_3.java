package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class CardItem_3 {

    private ArrayList<String> img_url;
    private boolean is_from_db;
    private String type_view_cards;

    public CardItem_3(ArrayList<String> img_url, boolean is_from_db, String type_view_cards) {
        this.img_url = img_url;
        this.is_from_db = is_from_db;
        this.type_view_cards = type_view_cards;
    }

    public CardItem_3() {

    }

    public String getType_view_cards() {
        return type_view_cards;
    }

    public void setType_view_cards(String type_view_cards) {
        this.type_view_cards = type_view_cards;
    }

    public ArrayList<String> getImg_url() {
        return img_url;
    }

    public void setImg_url(ArrayList<String> img_url) {
        this.img_url = img_url;
    }

    public boolean isIs_from_db() {
        return is_from_db;
    }

    public void setIs_from_db(boolean is_from_db) {
        this.is_from_db = is_from_db;
    }

}

