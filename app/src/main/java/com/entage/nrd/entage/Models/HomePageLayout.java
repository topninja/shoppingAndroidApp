package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class HomePageLayout {

    private String layout_type;
    private String title;
    private long index;
    private String flag;
    private boolean is_added;

    private ArrayList<String> item_ids;
    private ArrayList<ObjectLayoutHomePage> objects;

    private ArrayList<String> categories_path;

    private String database_title;


    public HomePageLayout() {

    }

    public HomePageLayout(String layout_type) {
        this.layout_type = layout_type;
    }

    public HomePageLayout(String database_title, String layout_type, long index, String title, String flag, ArrayList<String> item_ids) {
        this.database_title = database_title;
        this.layout_type = layout_type;
        this.index = index;
        this.title = title;
        this.flag = flag;
        this.item_ids = item_ids;
    }

    public ArrayList<ObjectLayoutHomePage> getObjects() {
        return objects;
    }

    public void setObjects(ArrayList<ObjectLayoutHomePage> objects) {
        this.objects = objects;
    }

    public String getDatabase_title() {
        return database_title;
    }

    public void setDatabase_title(String database_title) {
        this.database_title = database_title;
    }

    public String getLayout_type() {
        return layout_type;
    }

    public void setLayout_type(String layout_type) {
        this.layout_type = layout_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public boolean isIs_added() {
        return is_added;
    }

    public void setIs_added(boolean is_added) {
        this.is_added = is_added;
    }

    public ArrayList<String> getItem_ids() {
        return item_ids;
    }

    public void setItem_ids(ArrayList<String> item_ids) {
        this.item_ids = item_ids;
    }

    public long getIndex() {
        return index;
    }

    public ArrayList<String> getCategories_path() {
        return categories_path;
    }

    public void setCategories_path(ArrayList<String> categories_path) {
        this.categories_path = categories_path;
    }

    public void setIndex(long index) {
        this.index = index;
    }


    @Override
    public String toString() {
        return "HomePageLayout{" +
                "layout_type='" + layout_type + '\'' +
                ", title='" + title + '\'' +
                ", index=" + index +
                ", flag='" + flag + '\'' +
                ", is_added=" + is_added +
                ", item_ids=" + item_ids +
                ", objects=" + objects +
                ", categories_path=" + categories_path +
                ", database_title='" + database_title + '\'' +
                '}';
    }
}
