package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class ObjectLayoutHomePage {

    private String item_id;
    private String title;
    private String url;

    private String flag;
    private ArrayList<String> categories_path;


    public ObjectLayoutHomePage() {
    }

    public ObjectLayoutHomePage(String item_id, String title, String url, String flag, ArrayList<String> categories_path) {
        this.item_id = item_id;
        this.title = title;
        this.url = url;
        this.flag = flag;
        this.categories_path = categories_path;
    }


    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public ArrayList<String> getCategories_path() {
        return categories_path;
    }

    public void setCategories_path(ArrayList<String> categories_path) {
        this.categories_path = categories_path;
    }


    @Override
    public String toString() {
        return "ObjectLayoutHomePage{" +
                "item_id='" + item_id + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", flag='" + flag + '\'' +
                ", categories_path=" + categories_path +
                '}';
    }
}
