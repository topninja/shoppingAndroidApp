package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Order implements Parcelable{

    private String user_id;
    private String entage_page_id;
    private String order_id;
    private Object order_number;
    private String time_order;

    private String store_name;
    private HashMap<String, ItemOrder> item_orders;

    private AreaShippingAvailable area_selected;
    private ReceivingLocation location_selected;

    private String sending_item_method;
    private String payment_method;

    private String status;
    private String confirm_date;
    private String cancelled_date;

    private String reason_for_cancellation;
    private String cancelled_by_1;
    private String cancelled_by_2;

    private String extra_data;
    private boolean is_paid;

    private PaymentClaim payment_claim;

    public Order() {
    }

    public Order(String user_id, String entage_page_id, String order_id,
                 Object order_number, String time_order, String store_name,
                 HashMap<String, ItemOrder> item_orders, AreaShippingAvailable area_selected,
                 ReceivingLocation location_selected, String sending_item_method, String payment_method,
                 String status, String confirm_date, String cancelled_date, String reason_for_cancellation,
                 String cancelled_by_1, String cancelled_by_2, String extra_data, boolean is_paid, PaymentClaim payment_claim) {
        this.user_id = user_id;
        this.entage_page_id = entage_page_id;
        this.order_id = order_id;
        this.order_number = order_number;
        this.time_order = time_order;
        this.store_name = store_name;
        this.item_orders = item_orders;
        this.area_selected = area_selected;
        this.location_selected = location_selected;
        this.sending_item_method = sending_item_method;
        this.payment_method = payment_method;
        this.status = status;
        this.confirm_date = confirm_date;
        this.cancelled_date = cancelled_date;
        this.reason_for_cancellation = reason_for_cancellation;
        this.cancelled_by_1 = cancelled_by_1;
        this.cancelled_by_2 = cancelled_by_2;
        this.extra_data = extra_data;
        this.is_paid = is_paid;
        this.payment_claim = payment_claim;
    }

    public Order(String user_id, String entage_page_id, Object order_number, String order_id, String store_name, String time_order,
                 String sending_item_method, String payment_method, String status) {
        this.user_id = user_id;
        this.entage_page_id = entage_page_id;
        this.order_id = order_id;
        this.store_name = store_name;
        this.order_number = order_number;
        this.time_order = time_order;
        this.sending_item_method = sending_item_method;
        this.payment_method = payment_method;
        this.status = status;
    }


    protected Order(Parcel in) {
        user_id = in.readString();
        entage_page_id = in.readString();
        order_id = in.readString();
        store_name = in.readString();
        time_order = in.readString();
        area_selected = in.readParcelable(AreaShippingAvailable.class.getClassLoader());
        location_selected = in.readParcelable(ReceivingLocation.class.getClassLoader());
        sending_item_method = in.readString();
        payment_method = in.readString();
        status = in.readString();
        confirm_date = in.readString();
        cancelled_date = in.readString();
        reason_for_cancellation = in.readString();
        cancelled_by_1 = in.readString();
        cancelled_by_2 = in.readString();
        extra_data = in.readString();
        order_number = in.readLong();
        is_paid = in.readByte() != 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };



    public boolean isIs_paid() {
        return is_paid;
    }

    public void setIs_paid(boolean is_paid) {
        this.is_paid = is_paid;
    }

    public Object getOrder_number() {
        return order_number;
    }

    public void setOrder_number(Object order_number) {
        this.order_number = order_number;
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

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getTime_order() {
        return time_order;
    }

    public void setTime_order(String time_order) {
        this.time_order = time_order;
    }

    public HashMap<String, ItemOrder> getItem_orders() {
        return item_orders;
    }

    public void setItem_orders(HashMap<String, ItemOrder> item_orders) {
        this.item_orders = item_orders;
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


    public String getSending_item_method() {
        return sending_item_method;
    }

    public void setSending_item_method(String sending_item_method) {
        this.sending_item_method = sending_item_method;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason_for_cancellation() {
        return reason_for_cancellation;
    }

    public void setReason_for_cancellation(String reason_for_cancellation) {
        this.reason_for_cancellation = reason_for_cancellation;
    }

    public String getConfirm_date() {
        return confirm_date;
    }

    public void setConfirm_date(String confirm_date) {
        this.confirm_date = confirm_date;
    }

    public String getCancelled_date() {
        return cancelled_date;
    }

    public void setCancelled_date(String cancelled_date) {
        this.cancelled_date = cancelled_date;
    }

    public String getCancelled_by_1() {
        return cancelled_by_1;
    }

    public void setCancelled_by_1(String cancelled_by_1) {
        this.cancelled_by_1 = cancelled_by_1;
    }

    public String getCancelled_by_2() {
        return cancelled_by_2;
    }

    public void setCancelled_by_2(String cancelled_by_2) {
        this.cancelled_by_2 = cancelled_by_2;
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }

    public String getStore_name() {
        return store_name;
    }

    public void setStore_name(String store_name) {
        this.store_name = store_name;
    }

    public PaymentClaim getPayment_claim() {
        return payment_claim;
    }

    public void setPayment_claim(PaymentClaim payment_claim) {
        this.payment_claim = payment_claim;
    }

    public Order copy(){
        return new Order( user_id,  entage_page_id,  order_id,
                 order_number,  time_order,  store_name,
                 item_orders,  area_selected,
                 location_selected,  sending_item_method,  payment_method,
                 status,  confirm_date,  cancelled_date,  reason_for_cancellation,
                 cancelled_by_1,  cancelled_by_2,  extra_data,  is_paid, payment_claim);
    }

    @Override
    public String toString() {
        return "Order{" +
                "user_id='" + user_id + '\'' +
                ", entage_page_id='" + entage_page_id + '\'' +
                ", order_id='" + order_id + '\'' +
                ", time_order='" + time_order + '\'' +
                ", item_orders=" + item_orders +
                ", area_selected=" + area_selected +
                ", location_selected=" + location_selected +
                ", sending_item_method='" + sending_item_method + '\'' +
                ", payment_method='" + payment_method + '\'' +
                ", status='" + status + '\'' +
                ", reason_for_cancellation='" + reason_for_cancellation + '\'' +
                ", cancelled_by_1='" + cancelled_by_1 + '\'' +
                ", cancelled_by_2='" + cancelled_by_2 + '\'' +
                ", extra_data='" + extra_data + '\'' +
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
        dest.writeString(order_id);
        dest.writeString(store_name);
        dest.writeString(time_order);
        dest.writeParcelable(area_selected, flags);
        dest.writeParcelable(location_selected, flags);
        dest.writeString(sending_item_method);
        dest.writeString(payment_method);
        dest.writeString(status);
        dest.writeString(confirm_date);
        dest.writeString(cancelled_date);
        dest.writeString(reason_for_cancellation);
        dest.writeString(cancelled_by_1);
        dest.writeString(cancelled_by_2);
        dest.writeString(extra_data);
        //dest.writeLong((long) order_number);
        dest.writeByte((byte) (is_paid ? 1 : 0));
    }
}
