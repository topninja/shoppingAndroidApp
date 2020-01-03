package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class EntagePageShortData {

    private String name_entage_page;
    private String description;
    private String profile_photo;
    private String entage_id;
    private ArrayList<String> users_ids;

    public EntagePageShortData() {

    }

    public EntagePageShortData(String name_entage_page, String description,
                               String profile_photo, String entage_id, ArrayList<String> users_ids) {
        this.name_entage_page = name_entage_page;
        this.description = description;
        this.profile_photo = profile_photo;
        this.entage_id = entage_id;
        this.users_ids = users_ids;
    }

    public String getName_entage_page() {
        return name_entage_page;
    }

    public void setName_entage_page(String name_entage_page) {
        this.name_entage_page = name_entage_page;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getEntage_id() {
        return entage_id;
    }

    public void setEntage_id(String entage_id) {
        this.entage_id = entage_id;
    }

    public ArrayList<String> getUsers_ids() {
        return users_ids;
    }

    public void setUsers_ids(ArrayList<String> users_ids) {
        this.users_ids = users_ids;
    }

    @Override
    public String toString() {
        return "EntagePageShortData{" +
                "name_entage_page='" + name_entage_page + '\'' +
                ", description='" + description + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", entage_id='" + entage_id + '\'' +
                ", users_ids=" + users_ids +
                '}';
    }
}
