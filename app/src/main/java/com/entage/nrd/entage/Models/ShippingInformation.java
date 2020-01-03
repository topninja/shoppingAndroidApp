package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

public class ShippingInformation implements  Parcelable{

    private String country_store_item;
    private String city_store_item;
    private boolean shipping_available;
    private boolean locationAvailable;
    private HashMap<String, HashMap<String,AreaShippingAvailable>> area_shipping_available;
    private ArrayList<ReceivingLocation> receiving_location;


    public ShippingInformation(){
    }

    public ShippingInformation(String country_store_item, String city_store_item, boolean shipping_available,
                               boolean locationAvailable, HashMap<String, HashMap<String, AreaShippingAvailable>> area_shipping_available,
                               ArrayList<ReceivingLocation> receiving_location) {
        this.country_store_item = country_store_item;
        this.city_store_item = city_store_item;
        this.shipping_available = shipping_available;
        this.locationAvailable = locationAvailable;
        this.area_shipping_available = area_shipping_available;
        this.receiving_location = receiving_location;
    }


    protected ShippingInformation(Parcel in) {
        country_store_item = in.readString();
        city_store_item = in.readString();
        shipping_available = in.readByte() != 0;
        locationAvailable = in.readByte() != 0;
        receiving_location = in.createTypedArrayList(ReceivingLocation.CREATOR);
    }

    public static final Creator<ShippingInformation> CREATOR = new Creator<ShippingInformation>() {
        @Override
        public ShippingInformation createFromParcel(Parcel in) {
            return new ShippingInformation(in);
        }

        @Override
        public ShippingInformation[] newArray(int size) {
            return new ShippingInformation[size];
        }
    };

    public String getCountry_store_item() {
        return country_store_item;
    }

    public void setCountry_store_item(String country_store_item) {
        this.country_store_item = country_store_item;
    }

    public String getCity_store_item() {
        return city_store_item;
    }

    public void setCity_store_item(String city_store_item) {
        this.city_store_item = city_store_item;
    }

    public boolean isShipping_available() {
        return shipping_available;
    }

    public void setShipping_available(boolean shipping_available) {
        this.shipping_available = shipping_available;
    }

    public boolean isLocationAvailable() {
        return locationAvailable;
    }

    public void setLocationAvailable(boolean locationAvailable) {
        this.locationAvailable = locationAvailable;
    }

    public HashMap<String, HashMap<String, AreaShippingAvailable>> getArea_shipping_available() {
        return area_shipping_available;
    }

    public void setArea_shipping_available(HashMap<String, HashMap<String, AreaShippingAvailable>> area_shipping_available) {
        this.area_shipping_available = area_shipping_available;
    }

    public ArrayList<ReceivingLocation> getReceiving_location() {
        return receiving_location;
    }

    public void setReceiving_location(ArrayList<ReceivingLocation> receiving_location) {
        this.receiving_location = receiving_location;
    }

    @Override
    public String toString() {
        return "ShippingInformation{" +
                "country_store_item='" + country_store_item + '\'' +
                ", city_store_item='" + city_store_item + '\'' +
                ", shipping_available=" + shipping_available +
                ", locationAvailable=" + locationAvailable +
                ", area_shipping_available=" + area_shipping_available +
                ", receiving_location=" + receiving_location +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(country_store_item);
        dest.writeString(city_store_item);
        dest.writeByte((byte) (shipping_available ? 1 : 0));
        dest.writeByte((byte) (locationAvailable ? 1 : 0));
        dest.writeTypedList(receiving_location);
    }
}
