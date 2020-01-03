package com.entage.nrd.entage.Models;

public class PaymentClaim {

    private String user_id;
    private String entage_page_id;
    private String order_id;
    private String payment_claim_user_id;

    private String payment_claim_num;

    private String date_claiming;

    private String total_items_price;
    private String shipping_price;
    private String shipping_company;

    private boolean refused;
    private boolean is_paid;

    private String message_id;
    private String extra_data;

    private String date_replay;


    public PaymentClaim() {
    }

    public PaymentClaim(String user_id, String entage_page_id, String order_id, String payment_claim_user_id,
                        String payment_claim_num, String date_claiming, String total_items_price, String shipping_price,
                        String shipping_company, boolean refused, boolean is_paid, String message_id,
                        String extra_data, String date_replay) {
        this.user_id = user_id;
        this.entage_page_id = entage_page_id;
        this.order_id = order_id;
        this.payment_claim_user_id = payment_claim_user_id;
        this.payment_claim_num = payment_claim_num;
        this.date_claiming = date_claiming;
        this.total_items_price = total_items_price;
        this.shipping_price = shipping_price;
        this.shipping_company = shipping_company;
        this.refused = refused;
        this.is_paid = is_paid;
        this.message_id = message_id;
        this.extra_data = extra_data;
        this.date_replay = date_replay;
    }

    public String getDate_replay() {
        return date_replay;
    }

    public void setDate_replay(String date_replay) {
        this.date_replay = date_replay;
    }

    public boolean isIs_paid() {
        return is_paid;
    }

    public void setIs_paid(boolean is_paid) {
        this.is_paid = is_paid;
    }

    public String getPayment_claim_user_id() {
        return payment_claim_user_id;
    }

    public void setPayment_claim_user_id(String payment_claim_user_id) {
        this.payment_claim_user_id = payment_claim_user_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
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

    public String getPayment_claim_num() {
        return payment_claim_num;
    }

    public void setPayment_claim_num(String payment_claim_num) {
        this.payment_claim_num = payment_claim_num;
    }

    public String getDate_claiming() {
        return date_claiming;
    }

    public void setDate_claiming(String date_claiming) {
        this.date_claiming = date_claiming;
    }

    public String getTotal_items_price() {
        return total_items_price;
    }

    public void setTotal_items_price(String total_items_price) {
        this.total_items_price = total_items_price;
    }

    public String getShipping_price() {
        return shipping_price;
    }

    public void setShipping_price(String shipping_price) {
        this.shipping_price = shipping_price;
    }

    public String getShipping_company() {
        return shipping_company;
    }

    public void setShipping_company(String shipping_company) {
        this.shipping_company = shipping_company;
    }

    public boolean isRefused() {
        return refused;
    }

    public void setRefused(boolean refused) {
        this.refused = refused;
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }

    @Override
    public String toString() {
        return "PaymentClaim{" +
                "user_id='" + user_id + '\'' +
                ", entage_page_id='" + entage_page_id + '\'' +
                ", order_id='" + order_id + '\'' +
                ", payment_claim_user_id='" + payment_claim_user_id + '\'' +
                ", payment_claim_num='" + payment_claim_num + '\'' +
                ", date_claiming='" + date_claiming + '\'' +
                ", total_items_price=" + total_items_price +
                ", shipping_price=" + shipping_price +
                ", shipping_company='" + shipping_company + '\'' +
                ", confirm_by_user=" + refused +
                ", is_paid=" + is_paid +
                ", message_id='" + message_id + '\'' +
                ", extra_data='" + extra_data + '\'' +
                '}';
    }


    public PaymentClaim copy(){
        return new PaymentClaim(user_id, entage_page_id, order_id, payment_claim_user_id, payment_claim_num, date_claiming,
                total_items_price, shipping_price, shipping_company, refused,  is_paid, message_id, extra_data, date_replay);
    }

}
