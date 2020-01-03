package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class EntagePageSettings implements Parcelable{


    private String entage_id;
    private String user_id;
    private String email_entage_page;
    private String phone_entage_page;
    private String categories_number;

    public EntagePageSettings(String entage_id, String user_id, String email_entage_page, String phone_entage_page,
                              String categories_number, String categorie_1, String categorie_2, String categorie_3, String categorie_4,
                              String categorie_5, String categorie_6) {
        this.entage_id = entage_id;
        this.user_id = user_id;
        this.email_entage_page = email_entage_page;
        this.phone_entage_page = phone_entage_page;
        this.categories_number = categories_number;
    }

    public EntagePageSettings() {

    }

    protected EntagePageSettings(Parcel in) {
        entage_id = in.readString();
        user_id = in.readString();
        email_entage_page = in.readString();
        phone_entage_page = in.readString();
        categories_number = in.readString();
    }

    public static final Creator<EntagePageSettings> CREATOR = new Creator<EntagePageSettings>() {
        @Override
        public EntagePageSettings createFromParcel(Parcel in) {
            return new EntagePageSettings(in);
        }

        @Override
        public EntagePageSettings[] newArray(int size) {
            return new EntagePageSettings[size];
        }
    };

    public String getEntage_id() {
        return entage_id;
    }

    public void setEntage_id(String entage_id) {
        this.entage_id = entage_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getCategories_number() {
        return categories_number;
    }

    public void setCategories_number(String categories_number) {
        this.categories_number = categories_number;
    }

    @Override
    public String toString() {
        return "EntagePageSettings{" +
                "entage_id='" + entage_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", email_entage_page='" + email_entage_page + '\'' +
                ", phone_entage_page='" + phone_entage_page + '\'' +
                ", categories_number='" + categories_number + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entage_id);
        dest.writeString(user_id);
        dest.writeString(email_entage_page);
        dest.writeString(phone_entage_page);
        dest.writeString(categories_number);
    }
}