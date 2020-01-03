package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class ReceivingLocation  implements  Parcelable{

    private boolean by_google_map;
    private String title;
    private String lat_lng;
    private String location;
    private AreaShippingAvailable country;
    private AreaShippingAvailable city;
    private String address;
    private String phone_number_1;
    private String phone_number_2;

    private boolean payment_bs;
    private boolean payment_wr;

    public ReceivingLocation() {
    }

    public ReceivingLocation(boolean by_google_map, String title, String lat_lng, String location, AreaShippingAvailable country,
                             AreaShippingAvailable city, String address, String phone_number_1,
                             String phone_number_2, boolean payment_bs, boolean payment_wr) {
        this.by_google_map = by_google_map;
        this.title = title;
        this.lat_lng = lat_lng;
        this.location = location;
        this.country = country;
        this.city = city;
        this.address = address;
        this.phone_number_1 = phone_number_1;
        this.phone_number_2 = phone_number_2;
        this.payment_bs = payment_bs;
        this.payment_wr = payment_wr;
    }

    protected ReceivingLocation(Parcel in) {
        by_google_map = in.readByte() != 0;
        title = in.readString();
        lat_lng = in.readString();
        location = in.readString();
        country = in.readParcelable(AreaShippingAvailable.class.getClassLoader());
        city = in.readParcelable(AreaShippingAvailable.class.getClassLoader());
        address = in.readString();
        phone_number_1 = in.readString();
        phone_number_2 = in.readString();
        payment_bs = in.readByte() != 0;
        payment_wr = in.readByte() != 0;
    }

    public static final Creator<ReceivingLocation> CREATOR = new Creator<ReceivingLocation>() {
        @Override
        public ReceivingLocation createFromParcel(Parcel in) {
            return new ReceivingLocation(in);
        }

        @Override
        public ReceivingLocation[] newArray(int size) {
            return new ReceivingLocation[size];
        }
    };

    public boolean isBy_google_map() {
        return by_google_map;
    }

    public void setBy_google_map(boolean by_google_map) {
        this.by_google_map = by_google_map;
    }

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

    public AreaShippingAvailable getCountry() {
        return country;
    }

    public void setCountry(AreaShippingAvailable country) {
        this.country = country;
    }

    public AreaShippingAvailable getCity() {
        return city;
    }

    public void setCity(AreaShippingAvailable city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public boolean isPayment_bs() {
        return payment_bs;
    }

    public void setPayment_bs(boolean payment_bs) {
        this.payment_bs = payment_bs;
    }

    public boolean isPayment_wr() {
        return payment_wr;
    }

    public void setPayment_wr(boolean payment_wr) {
        this.payment_wr = payment_wr;
    }

    public String getLat_lng() {
        return lat_lng;
    }

    public void setLat_lng(String lat_lng) {
        this.lat_lng = lat_lng;
    }


    @Override
    public String toString() {
        return "ReceivingLocation{" +
                "by_google_map=" + by_google_map +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", country=" + country +
                ", city=" + city +
                ", address='" + address + '\'' +
                ", phone_number_1='" + phone_number_1 + '\'' +
                ", phone_number_2='" + phone_number_2 + '\'' +
                ", payment_bs=" + payment_bs +
                ", payment_wr=" + payment_wr +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (by_google_map ? 1 : 0));
        dest.writeString(title);
        dest.writeString(lat_lng);
        dest.writeString(location);
        dest.writeParcelable(country, flags);
        dest.writeParcelable(city, flags);
        dest.writeString(address);
        dest.writeString(phone_number_1);
        dest.writeString(phone_number_2);
        dest.writeByte((byte) (payment_bs ? 1 : 0));
        dest.writeByte((byte) (payment_wr ? 1 : 0));
    }
}
