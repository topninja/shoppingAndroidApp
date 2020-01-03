package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ItemBasket implements Parcelable {

    private String user_id;
    private String entage_page_id;
    private String item_id;
    private String item_basket_id;
    private Object item_number;
    private ArrayList<String> options;
    private String options_id;
    private String price;
    private String time_added;


    public ItemBasket() {
    }

    public ItemBasket(String user_id, String entage_page_id, String item_id, String item_basket_id,
                      Object item_number, ArrayList<String> options, String options_id, String price,
                      String time_added) {
        this.user_id = user_id;
        this.entage_page_id = entage_page_id;
        this.item_id = item_id;
        this.item_basket_id = item_basket_id;
        this.item_number = item_number;
        this.options = options;
        this.options_id = options_id;
        this.price = price;
        this.time_added = time_added;
    }

    protected ItemBasket(Parcel in) {
        user_id = in.readString();
        entage_page_id = in.readString();
        item_id = in.readString();
        item_basket_id = in.readString();
        options = in.createStringArrayList();
        options_id = in.readString();
        price = in.readString();
        time_added = in.readString();
        item_number = in.readLong();
    }

    public static final Creator<ItemBasket> CREATOR = new Creator<ItemBasket>() {
        @Override
        public ItemBasket createFromParcel(Parcel in) {
            return new ItemBasket(in);
        }

        @Override
        public ItemBasket[] newArray(int size) {
            return new ItemBasket[size];
        }
    };


    public Object getItem_number() {
        return item_number;
    }

    public void setItem_number(Object item_number) {
        this.item_number = item_number;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEntage_page_id() {
        return entage_page_id;
    }

    public void setEntage_page_id(String entage_page_id) {
        this.entage_page_id = entage_page_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public String getOptions_id() {
        return options_id;
    }

    public void setOptions_id(String options_id) {
        this.options_id = options_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTime_added() {
        return time_added;
    }

    public void setTime_added(String time_added) {
        this.time_added = time_added;
    }

    public String getItem_basket_id() {
        return item_basket_id;
    }

    public void setItem_basket_id(String item_basket_id) {
        this.item_basket_id = item_basket_id;
    }

    @Override
    public String toString() {
        return "ItemBasket{" +
                "user_id='" + user_id + '\'' +
                ", entage_page_id='" + entage_page_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", options=" + options +
                ", price=" + price +
                ", time_added='" + time_added + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(entage_page_id);
        dest.writeString(item_id);
        dest.writeString(item_basket_id);
        dest.writeStringList(options);
        dest.writeString(options_id);
        dest.writeString(price);
        dest.writeString(time_added);
        //dest.writeLong((long) item_number);
    }
}
