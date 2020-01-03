package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ItemOrder implements Parcelable {

    private String order_id;
    private String item_id;
    private Object item_number;

    private String item_basket_id;
    private String store_name;
    private String item_name;

    private ArrayList<String> options;
    private int quantity;

    private AreaShippingAvailable area_selected;
    private ReceivingLocation location_selected;

    private String time_cancellation;
    private String reason_for_cancellation;
    private String cancelled_by;

    private String item_price;
    private String shipping_price;

    public ItemOrder() {
    }

    public ItemOrder(String order_id, String item_id, Object item_number, String item_basket_id, String store_name, String item_name, ArrayList<String> options,
                     int quantity, AreaShippingAvailable area_selected, ReceivingLocation location_selected,
                     String time_cancellation, String reason_for_cancellation, String cancelled_by,
                     String item_price, String shipping_price) {
        this.order_id = order_id;
        this.item_id = item_id;
        this.item_number = item_number;
        this.item_basket_id = item_basket_id;
        this.store_name = store_name;
        this.item_name = item_name;
        this.options = options;
        this.quantity = quantity;
        this.area_selected = area_selected;
        this.location_selected = location_selected;
        this.time_cancellation = time_cancellation;
        this.reason_for_cancellation = reason_for_cancellation;
        this.cancelled_by = cancelled_by;
        this.item_price = item_price;
        this.shipping_price = shipping_price;
    }

    public ItemOrder(String order_id, String item_id, Object item_number, String item_basket_id, String store_name, String item_name, ArrayList<String> options, int quantity,
                     AreaShippingAvailable area_selected, ReceivingLocation location_selected,
                     String item_price, String shipping_price) {
        this.order_id = order_id;
        this.item_id = item_id;
        this.item_number = item_number;
        this.item_basket_id = item_basket_id;
        this.store_name = store_name;
        this.item_name = item_name;
        this.options = options;
        this.quantity = quantity;
        this.area_selected = area_selected;
        this.location_selected = location_selected;
        this.item_price = item_price;
        this.shipping_price = shipping_price;
    }

    protected ItemOrder(Parcel in) {
        order_id = in.readString();
        item_id = in.readString();
        item_number = in.readString();
        item_basket_id = in.readString();
        store_name = in.readString();
        item_name = in.readString();
        options = in.createStringArrayList();
        quantity = in.readInt();
        area_selected = in.readParcelable(AreaShippingAvailable.class.getClassLoader());
        location_selected = in.readParcelable(ReceivingLocation.class.getClassLoader());
        time_cancellation = in.readString();
        reason_for_cancellation = in.readString();
        cancelled_by = in.readString();
        item_price = in.readString();
        shipping_price = in.readString();
    }

    public static final Creator<ItemOrder> CREATOR = new Creator<ItemOrder>() {
        @Override
        public ItemOrder createFromParcel(Parcel in) {
            return new ItemOrder(in);
        }

        @Override
        public ItemOrder[] newArray(int size) {
            return new ItemOrder[size];
        }
    };

    public String getItem_basket_id() {
        return item_basket_id;
    }

    public void setItem_basket_id(String item_basket_id) {
        this.item_basket_id = item_basket_id;
    }

    public Object getItem_number() {
        return item_number;
    }

    public void setItem_number(Object item_number) {
        this.item_number = item_number;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public AreaShippingAvailable getArea_selected() {
        return area_selected;
    }

    public void setArea_selected(AreaShippingAvailable area_selected) {
        this.area_selected = area_selected;
    }

    public ReceivingLocation getLocation_selected() {
        return location_selected;
    }

    public void setLocation_selected(ReceivingLocation location_selected) {
        this.location_selected = location_selected;
    }

    public String getTime_cancellation() {
        return time_cancellation;
    }

    public void setTime_cancellation(String time_cancellation) {
        this.time_cancellation = time_cancellation;
    }

    public String getReason_for_cancellation() {
        return reason_for_cancellation;
    }

    public void setReason_for_cancellation(String reason_for_cancellation) {
        this.reason_for_cancellation = reason_for_cancellation;
    }

    public String getCancelled_by() {
        return cancelled_by;
    }

    public void setCancelled_by(String cancelled_by) {
        this.cancelled_by = cancelled_by;
    }

    public String getItem_price() {
        return item_price;
    }

    public void setItem_price(String item_price) {
        this.item_price = item_price;
    }

    public String getShipping_price() {
        return shipping_price;
    }

    public void setShipping_price(String shipping_price) {
        this.shipping_price = shipping_price;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public ItemOrder copy() {
        return new ItemOrder(order_id, item_id, item_number, item_basket_id, store_name, item_name, options, quantity,
                area_selected, location_selected, time_cancellation, reason_for_cancellation,cancelled_by, item_price, shipping_price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(order_id);
        dest.writeString(item_id);
        dest.writeString(item_basket_id);
        dest.writeString(store_name);
        dest.writeString(item_name);
        //dest.writeLong((long)item_number);
        dest.writeStringList(options);
        dest.writeInt(quantity);
        dest.writeParcelable(area_selected, flags);
        dest.writeParcelable(location_selected, flags);
        dest.writeString(time_cancellation);
        dest.writeString(reason_for_cancellation);
        dest.writeString(cancelled_by);
        dest.writeString(item_price);
        dest.writeString(shipping_price);
    }

    @Override
    public String toString() {
        return "ItemOrder{" +
                "order_id='" + order_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", options=" + options +
                ", quantity=" + quantity +
                ", area_selected=" + area_selected +
                ", location_selected=" + location_selected +
                ", time_cancellation='" + time_cancellation + '\'' +
                ", reason_for_cancellation='" + reason_for_cancellation + '\'' +
                ", cancelled_by='" + cancelled_by + '\'' +
                ", item_price=" + item_price +
                ", shipping_price=" + shipping_price +
                '}';
    }
}
