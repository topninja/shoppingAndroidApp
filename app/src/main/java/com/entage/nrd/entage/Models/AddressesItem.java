package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressesItem implements Parcelable{


    private String user_id;
    private String item_id;
    private String name_address;
    private String name_country;
    private String id_country;
    private String city;
    private String address_1;
    private String address_2;
    private String email;
    private String phone_number_1;
    private String phone_number_2;

    public AddressesItem() {

    }

    public AddressesItem(String user_id, String item_id, String name_address, String name_country,
                         String id_country, String city, String address_1, String address_2, String email,
                         String phone_number_1, String phone_number_2) {
        this.user_id = user_id;
        this.item_id = item_id;
        this.name_address = name_address;
        this.name_country = name_country;
        this.id_country = id_country;
        this.city = city;
        this.address_1 = address_1;
        this.address_2 = address_2;
        this.email = email;
        this.phone_number_1 = phone_number_1;
        this.phone_number_2 = phone_number_2;
    }


    protected AddressesItem(Parcel in) {
        user_id = in.readString();
        item_id = in.readString();
        name_address = in.readString();
        name_country = in.readString();
        id_country = in.readString();
        city = in.readString();
        address_1 = in.readString();
        address_2 = in.readString();
        email = in.readString();
        phone_number_1 = in.readString();
        phone_number_2 = in.readString();
    }

    public static final Creator<AddressesItem> CREATOR = new Creator<AddressesItem>() {
        @Override
        public AddressesItem createFromParcel(Parcel in) {
            return new AddressesItem(in);
        }

        @Override
        public AddressesItem[] newArray(int size) {
            return new AddressesItem[size];
        }
    };

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName_address() {
        return name_address;
    }

    public void setName_address(String name_address) {
        this.name_address = name_address;
    }

    public String getName_country() {
        return name_country;
    }

    public void setName_country(String name_country) {
        this.name_country = name_country;
    }

    public String getId_country() {
        return id_country;
    }

    public void setId_country(String id_country) {
        this.id_country = id_country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress_1() {
        return address_1;
    }

    public void setAddress_1(String address_1) {
        this.address_1 = address_1;
    }

    public String getAddress_2() {
        return address_2;
    }

    public void setAddress_2(String address_2) {
        this.address_2 = address_2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number_1() {
        return phone_number_1;
    }

    public void setPhone_number_1(String phone_number_1) {
        this.phone_number_1 = phone_number_1;
    }

    public String getPhone_number_2() {
        return phone_number_2;
    }

    public void setPhone_number_2(String phone_number_2) {
        this.phone_number_2 = phone_number_2;
    }

    @Override
    public String toString() {
        return "AddressesItem{" +
                "item_id='" + item_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", name_address='" + name_address + '\'' +
                ", name_country='" + name_country + '\'' +
                ", id_country='" + id_country + '\'' +
                ", city='" + city + '\'' +
                ", address_1='" + address_1 + '\'' +
                ", address_2='" + address_2 + '\'' +
                ", email='" + email + '\'' +
                ", phone_number_1='" + phone_number_1 + '\'' +
                ", phone_number_2='" + phone_number_2 + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(item_id);
        dest.writeString(name_address);
        dest.writeString(name_country);
        dest.writeString(id_country);
        dest.writeString(city);
        dest.writeString(address_1);
        dest.writeString(address_2);
        dest.writeString(email);
        dest.writeString(phone_number_1);
        dest.writeString(phone_number_2);
    }
}
