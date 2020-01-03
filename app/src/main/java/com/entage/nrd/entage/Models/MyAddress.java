package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class MyAddress implements  Parcelable{

    private String title;
    private String lat_lng;
    private String location;
    private String country;
    private LocationInformation city;
    private String address_home;
    private String phone_number;
    private String email;

    public MyAddress(){

    }

    public MyAddress(String title, String lat_lng, String location, String country,
                     LocationInformation city, String address_home, String phone_number, String email) {
        this.title = title;
        this.lat_lng = lat_lng;
        this.location = location;
        this.country = country;
        this.city = city;
        this.address_home = address_home;
        this.phone_number = phone_number;
        this.email = email;
    }

    protected MyAddress(Parcel in) {
        title = in.readString();
        lat_lng = in.readString();
        location = in.readString();
        country = in.readString();
        city = in.readParcelable(LocationInformation.class.getClassLoader());
        address_home = in.readString();
        phone_number = in.readString();
        email = in.readString();
    }

    public static final Creator<MyAddress> CREATOR = new Creator<MyAddress>() {
        @Override
        public MyAddress createFromParcel(Parcel in) {
            return new MyAddress(in);
        }

        @Override
        public MyAddress[] newArray(int size) {
            return new MyAddress[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocationInformation getCity() {
        return city;
    }

    public void setCity(LocationInformation city) {
        this.city = city;
    }

    public String getAddress_home() {
        return address_home;
    }

    public void setAddress_home(String address_home) {
        this.address_home = address_home;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLat_lng() {
        return lat_lng;
    }

    public void setLat_lng(String lat_lng) {
        this.lat_lng = lat_lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(lat_lng);
        dest.writeString(location);
        dest.writeString(country);
        dest.writeParcelable(city, flags);
        dest.writeString(address_home);
        dest.writeString(phone_number);
        dest.writeString(email);
    }



}
