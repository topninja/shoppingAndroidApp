package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

public class SubscriptionPackage implements Parcelable {


    private String package_id;
    private String package_name;
    private ArrayList<String> package_description;
    private String currency;
    private String price_3, price_6, price_12;
    private String start_date;
    private String expire_date;

    private boolean is_running;

    public SubscriptionPackage() {

    }

    public SubscriptionPackage(String package_name, ArrayList<String> package_description,
                               String currency, String price_3, String price_6, String price_12) {
        this.package_name = package_name;
        this.package_description = package_description;
        this.currency = currency;
        this.price_3 = price_3;
        this.price_6 = price_6;
        this.price_12 = price_12;
    }

    public SubscriptionPackage(String package_id, String start_date, String expire_date) {
        this.package_id = package_id;
        this.start_date = start_date;
        this.expire_date = expire_date;
    }

    protected SubscriptionPackage(Parcel in) {
        package_id = in.readString();
        package_name = in.readString();
        package_description = in.createStringArrayList();
        currency = in.readString();
        price_3 = in.readString();
        price_6 = in.readString();
        price_12 = in.readString();
        start_date = in.readString();
        expire_date = in.readString();
        is_running = in.readByte() != 0;
    }

    public static final Creator<SubscriptionPackage> CREATOR = new Creator<SubscriptionPackage>() {
        @Override
        public SubscriptionPackage createFromParcel(Parcel in) {
            return new SubscriptionPackage(in);
        }

        @Override
        public SubscriptionPackage[] newArray(int size) {
            return new SubscriptionPackage[size];
        }
    };

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(String expire_date) {
        this.expire_date = expire_date;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }

    public ArrayList<String> getPackage_description() {
        return package_description;
    }

    public void setPackage_description(ArrayList<String> package_description) {
        this.package_description = package_description;
    }

    public String getPrice_3() {
        return price_3;
    }

    public void setPrice_3(String price_3) {
        this.price_3 = price_3;
    }

    public String getPrice_6() {
        return price_6;
    }

    public void setPrice_6(String price_6) {
        this.price_6 = price_6;
    }

    public String getPrice_12() {
        return price_12;
    }

    public void setPrice_12(String price_12) {
        this.price_12 = price_12;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isIs_running() {
        return is_running;
    }

    public void setIs_running(boolean is_running) {
        this.is_running = is_running;
    }

    @Override
    public String toString() {
        return "SubscriptionPackage{" +
                "package_id='" + package_id + '\'' +
                ", package_name='" + package_name + '\'' +
                ", package_description=" + package_description +
                ", currency='" + currency + '\'' +
                ", price_3='" + price_3 + '\'' +
                ", price_6='" + price_6 + '\'' +
                ", price_12='" + price_12 + '\'' +
                ", start_date='" + start_date + '\'' +
                ", expire_date='" + expire_date + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(package_id);
        dest.writeString(package_name);
        dest.writeStringList(package_description);
        dest.writeString(currency);
        dest.writeString(price_3);
        dest.writeString(price_6);
        dest.writeString(price_12);
        dest.writeString(start_date);
        dest.writeString(expire_date);
        dest.writeByte((byte) (is_running ? 1 : 0));
    }
}
