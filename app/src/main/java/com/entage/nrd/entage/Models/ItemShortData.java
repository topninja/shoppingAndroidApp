package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ItemShortData implements Parcelable {

    private String entage_page_id;
    private String item_id;
    private Object item_number;
    private ArrayList<String> users_ids_has_access;

    // FragmentNameItem
    private String name_item;
    private List<String> images_url;

    // FragmentAddOptions
    private OptionsPrices options_prices;

    // FragmentShippingInformation
    private ShippingInformation shipping_information;

    private boolean notifying_questions;

    private String extra_data;

    public ItemShortData() {
    }

    public ItemShortData(String entage_page_id, String item_id, Object item_number,  ArrayList<String> users_ids_has_access,
                         String name_item, List<String> images_url, OptionsPrices options_prices,
                         ShippingInformation shipping_information, boolean notifying_questions, String extra_data) {
        this.entage_page_id = entage_page_id;
        this.item_id = item_id;
        this.item_number = item_number;
        this.users_ids_has_access = users_ids_has_access;
        this.name_item = name_item;
        this.images_url = images_url;
        this.options_prices = options_prices;
        this.shipping_information = shipping_information;
        this.notifying_questions = notifying_questions;
        this.extra_data = extra_data;
    }

    protected ItemShortData(Parcel in) {
        entage_page_id = in.readString();
        item_id = in.readString();
        users_ids_has_access = in.createStringArrayList();
        name_item = in.readString();
        images_url = in.createStringArrayList();
        options_prices = in.readParcelable(OptionsPrices.class.getClassLoader());
        shipping_information = in.readParcelable(ShippingInformation.class.getClassLoader());
        notifying_questions = in.readByte() != 0;
        extra_data = in.readString();
        item_number = in.readLong();
    }

    public static final Creator<ItemShortData> CREATOR = new Creator<ItemShortData>() {
        @Override
        public ItemShortData createFromParcel(Parcel in) {
            return new ItemShortData(in);
        }

        @Override
        public ItemShortData[] newArray(int size) {
            return new ItemShortData[size];
        }
    };

    public String getName_item() {
        return name_item;
    }

    public void setName_item(String name_item) {
        this.name_item = name_item;
    }

    public OptionsPrices getOptions_prices() {
        return options_prices;
    }

    public void setOptions_prices(OptionsPrices options_rices) {
        this.options_prices = options_rices;
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

    public ArrayList<String> getUsers_ids_has_access() {
        return users_ids_has_access;
    }

    public void setUsers_ids_has_access(ArrayList<String> users_ids_has_access) {
        this.users_ids_has_access = users_ids_has_access;
    }

    public Object getItem_number() {
        return item_number;
    }

    public void setItem_number(Object item_number) {
        this.item_number = item_number;
    }

    public List<String> getImages_url() {
        return images_url;
    }

    public void setImages_url(List<String> images_url) {
        this.images_url = images_url;
    }

    public ShippingInformation getShipping_information() {
        return shipping_information;
    }

    public void setShipping_information(ShippingInformation shipping_information) {
        this.shipping_information = shipping_information;
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
        return "ItemShortData{" +
                "entage_page_id='" + entage_page_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", users_ids_has_access=" + users_ids_has_access +
                ", name_item='" + name_item + '\'' +
                ", images_url=" + images_url +
                ", options=" + options_prices +
                ", shipping_information=" + shipping_information +
                ", notifying_questions=" + notifying_questions +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entage_page_id);
        dest.writeString(item_id);
        dest.writeStringList(users_ids_has_access);
        dest.writeString(name_item);
        dest.writeStringList(images_url);
        dest.writeParcelable(options_prices, flags);
        dest.writeParcelable(shipping_information, flags);
        dest.writeByte((byte) (notifying_questions ? 1 : 0));
        dest.writeString(extra_data);
        //dest.writeLong((long) item_number);
    }
}
