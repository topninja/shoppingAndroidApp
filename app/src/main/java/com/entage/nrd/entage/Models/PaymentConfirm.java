package com.entage.nrd.entage.Models;

import com.braintreepayments.api.models.PaymentMethodNonce;
import com.entage.nrd.entage.payment.LineItem;

import org.json.JSONObject;

import java.util.List;

public class PaymentConfirm {

    private PaymentMethodNonce payment_method_nonce;
    private String nonce;
    private String amount;
    private String items_total;
    private String purchase_total;
    private String transaction_fee;

    private String amount_sar;
    private String items_total_sar;
    private String purchase_total_sar;
    private String transaction_fee_sar;

    private String converter_currency;
    private String currency;

    private String user_id;
    private String user_name;
    private String full_user_name;
    private String user_country;
    private String user_city;
    private String phone;
    private String email;

    private String entage_page_user_id;
    private String entage_page_id;

    private String order_id;
    private String message_id;
    private String payment_id;
    private Object order_number;
    private Object payment_number;
    private String payment_for;
    private String previous_payment_id;

    private String shipping_amount;
    private int items_count;
    private List<LineItem> items_information;

    private int subscribe_type;
    private SubscriptionPackage package_new_subscription;
    private SubscriptionPackage current_subscription;


    private String checkout_by;
    private String time;

    public PaymentConfirm() {
    }

    public int getSubscribe_type() {
        return subscribe_type;
    }

    public void setSubscribe_type(int subscribe_type) {
        this.subscribe_type = subscribe_type;
    }

    public SubscriptionPackage getPackage_new_subscription() {
        return package_new_subscription;
    }

    public void setPackage_new_subscription(SubscriptionPackage package_new_subscription) {
        this.package_new_subscription = package_new_subscription;
    }

    public SubscriptionPackage getCurrent_subscription() {
        return current_subscription;
    }

    public void setCurrent_subscription(SubscriptionPackage current_subscription) {
        this.current_subscription = current_subscription;
    }

    public String getItems_total() {
        return items_total;
    }

    public void setItems_total(String items_total) {
        this.items_total = items_total;
    }

    public String getItems_total_sar() {
        return items_total_sar;
    }

    public void setItems_total_sar(String items_total_sar) {
        this.items_total_sar = items_total_sar;
    }

    public String getPrevious_payment_id() {
        return previous_payment_id;
    }

    public void setPrevious_payment_id(String previous_payment_id) {
        this.previous_payment_id = previous_payment_id;
    }

    public String getConverter_currency() {
        return converter_currency;
    }

    public void setConverter_currency(String converter_currency) {
        this.converter_currency = converter_currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAmount_sar() {
        return amount_sar;
    }

    public void setAmount_sar(String amount_sar) {
        this.amount_sar = amount_sar;
    }

    public String getPurchase_total_sar() {
        return purchase_total_sar;
    }

    public void setPurchase_total_sar(String purchase_total_sar) {
        this.purchase_total_sar = purchase_total_sar;
    }

    public String getTransaction_fee_sar() {
        return transaction_fee_sar;
    }

    public void setTransaction_fee_sar(String transaction_fee_sar) {
        this.transaction_fee_sar = transaction_fee_sar;
    }

    public String getPurchase_total() {
        return purchase_total;
    }

    public void setPurchase_total(String purchase_total) {
        this.purchase_total = purchase_total;
    }

    public String getTransaction_fee() {
        return transaction_fee;
    }

    public void setTransaction_fee(String transaction_fee) {
        this.transaction_fee = transaction_fee;
    }

    public String getPayment_for() {
        return payment_for;
    }

    public void setPayment_for(String payment_for) {
        this.payment_for = payment_for;
    }

    public Object getPayment_number() {
        return payment_number;
    }

    public void setPayment_number(Object payment_number) {
        this.payment_number = payment_number;
    }

    public void setPayment_method_nonce(PaymentMethodNonce payment_method_nonce) {
        this.payment_method_nonce = payment_method_nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setFull_user_name(String full_user_name) {
        this.full_user_name = full_user_name;
    }

    public void setUser_country(String user_country) {
        this.user_country = user_country;
    }

    public void setUser_city(String user_city) {
        this.user_city = user_city;
    }

    public void setEntage_page_user_id(String entage_page_user_id) {
        this.entage_page_user_id = entage_page_user_id;
    }

    public void setEntage_page_id(String entage_page_id) {
        this.entage_page_id = entage_page_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public void setOrder_number(Object order_number) {
        this.order_number = order_number;
    }

    public void setShipping_amount(String shipping_amount) {
        this.shipping_amount = shipping_amount;
    }

    public void setItems_count(int items_count) {
        this.items_count = items_count;
    }

    public void setItems_information(List<LineItem> items_information) {
        this.items_information = items_information;
    }

    public void setCheckout_by(String checkout_by) {
        this.checkout_by = checkout_by;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public PaymentMethodNonce getPayment_method_nonce() {
        return payment_method_nonce;
    }

    public String getNonce() {
        return nonce;
    }

    public String getAmount() {
        return amount;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getFull_user_name() {
        return full_user_name;
    }

    public String getUser_country() {
        return user_country;
    }

    public String getUser_city() {
        return user_city;
    }

    public String getEntage_page_user_id() {
        return entage_page_user_id;
    }

    public String getEntage_page_id() {
        return entage_page_id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public Object getOrder_number() {
        return order_number;
    }

    public String getShipping_amount() {
        return shipping_amount;
    }

    public int getItems_count() {
        return items_count;
    }

    public List<LineItem> getItems_information() {
        return items_information;
    }

    public String getCheckout_by() {
        return checkout_by;
    }

    public String getTime() {
        return time;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
