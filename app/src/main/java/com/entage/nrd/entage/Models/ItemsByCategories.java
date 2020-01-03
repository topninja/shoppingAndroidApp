package com.entage.nrd.entage.Models;

public class ItemsByCategories {

    private String item_id;


    public ItemsByCategories() {

    }

    public ItemsByCategories(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    @Override
    public String toString() {
        return "ItemsByCategories{" +
                "item_id='" + item_id + '\'' +
                '}';
    }
}
