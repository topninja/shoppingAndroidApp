package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class AreaShippingAvailable implements  Parcelable{

    private String user_id;
    private String item_id;
    private String name_area;
    private String id_area;
    private String country_code;
    private boolean shipping_available;
    private boolean free_shipping;
    private String shipping_company;
    private String shipping_price;
    private boolean shipping_price_later;
    private String shipping_time;
    private boolean paymentBs;
    private boolean paymentWr;

    public AreaShippingAvailable() {
    }

    public AreaShippingAvailable(String user_id, String item_id, String name_area, String id_area, String country_code, boolean shipping_available, boolean free_shipping, String shipping_company, String shipping_price, boolean shipping_price_later,
                                 String shipping_time, boolean paymentBs, boolean paymentWr) {
        this.user_id = user_id;
        this.item_id = item_id;
        this.name_area = name_area;
        this.id_area = id_area;
        this.country_code = country_code;
        this.shipping_available = shipping_available;
        this.free_shipping = free_shipping;
        this.shipping_company = shipping_company;
        this.shipping_price = shipping_price;
        this.shipping_price_later = shipping_price_later;
        this.shipping_time = shipping_time;
        this.paymentBs = paymentBs;
        this.paymentWr = paymentWr;
    }

    protected AreaShippingAvailable(Parcel in) {
        user_id = in.readString();
        item_id = in.readString();
        name_area = in.readString();
        id_area = in.readString();
        country_code = in.readString();
        shipping_available = in.readByte() != 0;
        free_shipping = in.readByte() != 0;
        shipping_company = in.readString();
        shipping_price = in.readString();
        shipping_price_later = in.readByte() != 0;
        shipping_time = in.readString();
        paymentBs = in.readByte() != 0;
        paymentWr = in.readByte() != 0;
    }

    public static final Creator<AreaShippingAvailable> CREATOR = new Creator<AreaShippingAvailable>() {
        @Override
        public AreaShippingAvailable createFromParcel(Parcel in) {
            return new AreaShippingAvailable(in);
        }

        @Override
        public AreaShippingAvailable[] newArray(int size) {
            return new AreaShippingAvailable[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getName_area() {
        return name_area;
    }

    public void setName_area(String name_area) {
        this.name_area = name_area;
    }

    public String getId_area() {
        return id_area;
    }

    public void setId_area(String id_area) {
        this.id_area = id_area;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public boolean isShipping_available() {
        return shipping_available;
    }

    public void setShipping_available(boolean shipping_available) {
        this.shipping_available = shipping_available;
    }

    public boolean isFree_shipping() {
        return free_shipping;
    }

    public void setFree_shipping(boolean free_shipping) {
        this.free_shipping = free_shipping;
    }

    public String getShipping_company() {
        return shipping_company;
    }

    public void setShipping_company(String shipping_company) {
        this.shipping_company = shipping_company;
    }

    public String getShipping_price() {
        return shipping_price;
    }

    public void setShipping_price(String shipping_price) {
        this.shipping_price = shipping_price;
    }

    public boolean isShipping_price_later() {
        return shipping_price_later;
    }

    public void setShipping_price_later(boolean shipping_price_later) {
        this.shipping_price_later = shipping_price_later;
    }

    public String getShipping_time() {
        return shipping_time;
    }

    public void setShipping_time(String shipping_time) {
        this.shipping_time = shipping_time;
    }

    public boolean isPaymentBs() {
        return paymentBs;
    }

    public void setPaymentBs(boolean paymentBs) {
        this.paymentBs = paymentBs;
    }

    public boolean isPaymentWr() {
        return paymentWr;
    }

    public void setPaymentWr(boolean paymentWr) {
        this.paymentWr = paymentWr;
    }

    @Override
    public String toString() {
        return "AreaShippingAvailable{" +
                "user_id='" + user_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", name_area='" + name_area + '\'' +
                ", id_area='" + id_area + '\'' +
                ", country_code='" + country_code + '\'' +
                ", shipping_available=" + shipping_available +
                ", free_shipping=" + free_shipping +
                ", shipping_company='" + shipping_company + '\'' +
                ", shipping_price='" + shipping_price + '\'' +
                ", shipping_price_later=" + shipping_price_later +
                ", shipping_time='" + shipping_time + '\'' +
                ", paymentBs=" + paymentBs +
                ", paymentWr=" + paymentWr +
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
        dest.writeString(name_area);
        dest.writeString(id_area);
        dest.writeString(country_code);
        dest.writeByte((byte) (shipping_available ? 1 : 0));
        dest.writeByte((byte) (free_shipping ? 1 : 0));
        dest.writeString(shipping_company);
        dest.writeString(shipping_price);
        dest.writeByte((byte) (shipping_price_later ? 1 : 0));
        dest.writeString(shipping_time);
        dest.writeByte((byte) (paymentBs ? 1 : 0));
        dest.writeByte((byte) (paymentWr ? 1 : 0));
    }
}
