package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class UserAccountSettings {

    private String user_id;
    private String username;
    private String first_name;
    private String last_name;
    private LocationInformation location_information;
    private String following;
    private String followers;
    private String profile_photo;
    private String email;
    private String phone_number;
    private String sex;
    private boolean entage_page;
    private ArrayList<String> entage_pages_access;
    private String language;
    private String currency;


    public UserAccountSettings() {

    }

    public UserAccountSettings(String user_id, String username, String first_name, String last_name, LocationInformation location_information,
                               String following, String followers, String profile_photo, String email,
                               String phone_number, String sex, boolean entage_page, ArrayList<String> entage_pages_access,
                               String language, String currency) {
        this.user_id = user_id;
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.location_information = location_information;
        this.following = following;
        this.followers = followers;
        this.profile_photo = profile_photo;
        this.email = email;
        this.phone_number = phone_number;
        this.sex = sex;
        this.entage_page = entage_page;
        this.entage_pages_access = entage_pages_access;
        this.language = language;
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public LocationInformation getLocation_information() {
        return location_information;
    }

    public void setLocation_information(LocationInformation location_information) {
        this.location_information = location_information;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public boolean isEntage_page() {
        return entage_page;
    }

    public void setEntage_page(boolean entage_page) {
        this.entage_page = entage_page;
    }



    public ArrayList<String> getEntage_pages_access() {
        return entage_pages_access;
    }

    public void setEntage_pages_access(ArrayList<String> entage_pages_access) {
        this.entage_pages_access = entage_pages_access;
    }

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", country='" + location_information + '\'' +
                ", following='" + following + '\'' +
                ", followers='" + followers + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", email='" + email + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", sex='" + sex + '\'' +
                ", entage_page=" + entage_page +
                ", entage_pages_access=" + entage_pages_access +
                '}';
    }
}
