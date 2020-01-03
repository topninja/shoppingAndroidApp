package com.entage.nrd.entage.Models;

import java.util.HashMap;

public class EditDataOrder {

    private HashMap<String, ItemOrder> old_item_orders;
    private HashMap<String, ItemOrder> new_item_orders;

    private String date;
    private boolean confirmed;
    private boolean refused;

    private String message_id;
    private String extra_data;

    private String date_replay;

    public EditDataOrder() {
    }

    public EditDataOrder(HashMap<String, ItemOrder> old_item_orders, HashMap<String, ItemOrder> new_item_orders, String date, boolean confirmed,
                         boolean refused, String message_id, String extra_data, String date_replay) {
        this.old_item_orders = old_item_orders;
        this.new_item_orders = new_item_orders;
        this.date = date;
        this.confirmed = confirmed;
        this.refused = refused;
        this.message_id = message_id;
        this.extra_data = extra_data;
        this.date_replay = date_replay;
    }

    public HashMap<String, ItemOrder> getOld_item_orders() {
        return old_item_orders;
    }

    public void setOld_item_orders(HashMap<String, ItemOrder> old_item_orders) {
        this.old_item_orders = old_item_orders;
    }

    public HashMap<String, ItemOrder> getNew_item_orders() {
        return new_item_orders;
    }

    public void setNew_item_orders(HashMap<String, ItemOrder> new_item_orders) {
        this.new_item_orders = new_item_orders;
    }

    public boolean isRefused() {
        return refused;
    }

    public void setRefused(boolean refused) {
        this.refused = refused;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }

    public String getDate_replay() {
        return date_replay;
    }

    public void setDate_replay(String date_replay) {
        this.date_replay = date_replay;
    }

    public EditDataOrder copy(){
        return new EditDataOrder(old_item_orders, new_item_orders, date, confirmed, refused, message_id, extra_data, date_replay);
    }
}
