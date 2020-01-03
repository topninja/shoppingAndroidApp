package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationInformation implements Parcelable {


    private String user_id;
    private String city_name;
    private String city_id;

    private String state_name;
    private String state_id;

    private String country_code;
    private String country_id;

    private String postal_code;


    public LocationInformation() {
    }

    public LocationInformation(String user_id, String city_name, String city_id, String state_name,
                               String state_id, String country_code, String country_id, String postal_code) {
        this.user_id = user_id;
        this.city_name = city_name;
        this.city_id = city_id;
        this.state_name = state_name;
        this.state_id = state_id;
        this.country_code = country_code;
        this.country_id = country_id;
        this.postal_code = postal_code;
    }

    protected LocationInformation(Parcel in) {
        user_id = in.readString();
        city_name = in.readString();
        city_id = in.readString();
        state_name = in.readString();
        state_id = in.readString();
        country_code = in.readString();
        country_id = in.readString();
        postal_code = in.readString();
    }

    public static final Creator<LocationInformation> CREATOR = new Creator<LocationInformation>() {
        @Override
        public LocationInformation createFromParcel(Parcel in) {
            return new LocationInformation(in);
        }

        @Override
        public LocationInformation[] newArray(int size) {
            return new LocationInformation[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public String getCity_id() {
        return city_id;
    }

    public void setCity_id(String city_id) {
        this.city_id = city_id;
    }

    public String getState_name() {
        return state_name;
    }

    public void setState_name(String state_name) {
        this.state_name = state_name;
    }

    public String getState_id() {
        return state_id;
    }

    public void setState_id(String state_id) {
        this.state_id = state_id;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getCountry_id() {
        return country_id;
    }

    public void setCountry_id(String country_id) {
        this.country_id = country_id;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    @Override
    public String toString() {
        return "LocationInformation{" +
                "user_id='" + user_id + '\'' +
                ", city_name='" + city_name + '\'' +
                ", city_id='" + city_id + '\'' +
                ", state_name='" + state_name + '\'' +
                ", state_id='" + state_id + '\'' +
                ", country_code='" + country_code + '\'' +
                ", country_id='" + country_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(city_name);
        dest.writeString(city_id);
        dest.writeString(state_name);
        dest.writeString(state_id);
        dest.writeString(country_code);
        dest.writeString(country_id);
        dest.writeString(postal_code);
    }
}
