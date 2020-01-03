package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class EntagePage implements Parcelable{

    private String name_entage_page;
    private String description;
    private String profile_photo;
    private String profile_bg_photo;
    private String entage_id;
    private Object entage_page_number;
    private ArrayList<String> users_ids;
    private long posts;
    private float rating;
    private ArrayList<String> categories_entage_page;
    private ArrayList<EntagePageDivision> entage_page_divisions;
    private String currency_entage_page;
    private boolean notifying_questions;
    private String email_entage_page;
    private String phone_entage_page;
    private String extra_data;


    public EntagePage() {

    }

    public EntagePage(String name_entage_page, String description,
                      String profile_photo, String profile_bg_photo, String entage_id, Object entage_page_number,
                      ArrayList<String> users_ids, long posts, float rating, ArrayList<String> categories_entage_page,
                      ArrayList<EntagePageDivision> entage_page_divisions, String currency_entage_page, boolean notifying_questions,
                      String email_entage_page, String phone_entage_page, String extra_data) {
        this.name_entage_page = name_entage_page;
        this.description = description;
        this.profile_photo = profile_photo;
        this.profile_bg_photo = profile_bg_photo;
        this.entage_id = entage_id;
        this.entage_page_number = entage_page_number;
        this.users_ids = users_ids;
        this.posts = posts;
        this.rating = rating;
        this.categories_entage_page = categories_entage_page;
        this.entage_page_divisions = entage_page_divisions;
        this.currency_entage_page = currency_entage_page;
        this.notifying_questions = notifying_questions;
        this.email_entage_page = email_entage_page;
        this.phone_entage_page = phone_entage_page;
        this.extra_data = extra_data;
    }

    public EntagePage(String name_entage_page, String profile_photo, String entage_id) {
        this.name_entage_page = name_entage_page;
        this.profile_photo = profile_photo;
        this.entage_id = entage_id;
    }


    protected EntagePage(Parcel in) {
        name_entage_page = in.readString();
        description = in.readString();
        profile_photo = in.readString();
        profile_bg_photo = in.readString();
        entage_id = in.readString();
        users_ids = in.createStringArrayList();
        posts = in.readLong();
        rating = in.readFloat();
        categories_entage_page = in.createStringArrayList();
        entage_page_divisions = in.createTypedArrayList(EntagePageDivision.CREATOR);
        currency_entage_page = in.readString();
        notifying_questions = in.readByte() != 0;
        email_entage_page = in.readString();
        phone_entage_page = in.readString();
        extra_data = in.readString();
        entage_page_number = in.readLong();
    }

    public static final Creator<EntagePage> CREATOR = new Creator<EntagePage>() {
        @Override
        public EntagePage createFromParcel(Parcel in) {
            return new EntagePage(in);
        }

        @Override
        public EntagePage[] newArray(int size) {
            return new EntagePage[size];
        }
    };

    public Object getEntage_page_number() {
        return entage_page_number;
    }

    public void setEntage_page_number(Object entage_page_number) {
        this.entage_page_number = entage_page_number;
    }

    public String getName_entage_page() {
        return name_entage_page;
    }

    public void setName_entage_page(String name_entage_page) {
        this.name_entage_page = name_entage_page;
    }

    public String getEmail_entage_page() {
        return email_entage_page;
    }

    public void setEmail_entage_page(String email_entage_page) {
        this.email_entage_page = email_entage_page;
    }

    public String getPhone_entage_page() {
        return phone_entage_page;
    }

    public void setPhone_entage_page(String phone_entage_page) {
        this.phone_entage_page = phone_entage_page;
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

    public String getProfile_bg_photo() {
        return profile_bg_photo;
    }

    public void setProfile_bg_photo(String profile_bg_photo) {
        this.profile_bg_photo = profile_bg_photo;
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

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public ArrayList<String> getCategories_entage_page() {
        return categories_entage_page;
    }

    public void setCategories_entage_page(ArrayList<String> categories_entage_page) {
        this.categories_entage_page = categories_entage_page;
    }

    public ArrayList<EntagePageDivision> getEntage_page_divisions() {
        return entage_page_divisions;
    }

    public void setEntage_page_divisions(ArrayList<EntagePageDivision> entage_page_divisions) {
        this.entage_page_divisions = entage_page_divisions;
    }

    public String getCurrency_entage_page() {
        return currency_entage_page;
    }

    public void setCurrency_entage_page(String currency_entage_page) {
        this.currency_entage_page = currency_entage_page;
    }

    public boolean isNotifying_questions() {
        return notifying_questions;
    }

    public void setNotifying_questions(boolean notifying_questions) {
        this.notifying_questions = notifying_questions;
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }

    @Override
    public String toString() {
        return "EntagePage{" +
                "name_entage_page='" + name_entage_page + '\'' +
                ", description='" + description + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", profile_bg_photo='" + profile_bg_photo + '\'' +
                ", entage_id='" + entage_id + '\'' +
                ", entage_page_number=" + entage_page_number +
                ", users_ids=" + users_ids +
                ", posts=" + posts +
                ", rating=" + rating +
                ", categories_entage_page=" + categories_entage_page +
                ", entage_page_divisions=" + entage_page_divisions +
                ", currency_entage_page='" + currency_entage_page + '\'' +
                ", notifying_questions=" + notifying_questions +
                ", email_entage_page='" + email_entage_page + '\'' +
                ", phone_entage_page='" + phone_entage_page + '\'' +
                ", extra_data='" + extra_data + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name_entage_page);
        dest.writeString(description);
        dest.writeString(profile_photo);
        dest.writeString(profile_bg_photo);
        dest.writeString(entage_id);
        dest.writeStringList(users_ids);
        dest.writeLong(posts);
        dest.writeFloat(rating);
        dest.writeStringList(categories_entage_page);
        dest.writeTypedList(entage_page_divisions);
        dest.writeString(currency_entage_page);
        dest.writeByte((byte) (notifying_questions ? 1 : 0));
        dest.writeString(email_entage_page);
        dest.writeString(phone_entage_page);
        dest.writeString(extra_data);
        //dest.writeLong((Long) entage_page_number);
    }
}