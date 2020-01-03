package com.entage.nrd.entage.Models;

public class PaymentInformation {

    private String payment_id;
    private Object payment_number;
    private String payment_for;

    private Deposit deposit;
    private Withdraw withdraw;

    private String checkout_by;
    private String user_id;

    private String amount;
    private String amount_sar;
    private String purchase_total;
    private String purchase_total_sar;
    private String transaction_fee;
    private String transaction_fee_sar;
    private String converter_currency;
    private String currency;

    private String order_id;
    private Object order_number;
    private String message_id;
    private String entage_page_id;
    private String entage_page_user_id;

    private String id;
    private String transaction_id;
    private String time;
    private Object timestamp;




    public PaymentInformation() {

    }

    public PaymentInformation(String payment_id, Object payment_number, String payment_for, Deposit deposit,
                              Withdraw withdraw, String checkout_by, String user_id, String amount, String amount_sar,
                              String purchase_total, String purchase_total_sar, String transaction_fee,
                              String transaction_fee_sar, String converter_currency, String currency, String order_id,
                              Object order_number, String message_id, String entage_page_id, String entage_page_user_id,
                              String id, String transaction_id, String time, Object timestamp) {
        this.payment_id = payment_id;
        this.payment_number = payment_number;
        this.payment_for = payment_for;
        this.deposit = deposit;
        this.withdraw = withdraw;
        this.checkout_by = checkout_by;
        this.user_id = user_id;
        this.amount = amount;
        this.amount_sar = amount_sar;
        this.purchase_total = purchase_total;
        this.purchase_total_sar = purchase_total_sar;
        this.transaction_fee = transaction_fee;
        this.transaction_fee_sar = transaction_fee_sar;
        this.converter_currency = converter_currency;
        this.currency = currency;
        this.order_id = order_id;
        this.order_number = order_number;
        this.message_id = message_id;
        this.entage_page_id = entage_page_id;
        this.entage_page_user_id = entage_page_user_id;
        this.id = id;
        this.transaction_id = transaction_id;
        this.time = time;
        this.timestamp = timestamp;
    }

    public String getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(String payment_id) {
        this.payment_id = payment_id;
    }

    public Object getPayment_number() {
        return payment_number;
    }

    public void setPayment_number(Object payment_number) {
        this.payment_number = payment_number;
    }

    public String getPayment_for() {
        return payment_for;
    }

    public void setPayment_for(String payment_for) {
        this.payment_for = payment_for;
    }

    public Deposit getDeposit() {
        return deposit;
    }

    public void setDeposit(Deposit deposit) {
        this.deposit = deposit;
    }

    public Withdraw getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(Withdraw withdraw) {
        this.withdraw = withdraw;
    }

    public String getCheckout_by() {
        return checkout_by;
    }

    public void setCheckout_by(String checkout_by) {
        this.checkout_by = checkout_by;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount_sar() {
        return amount_sar;
    }

    public void setAmount_sar(String amount_sar) {
        this.amount_sar = amount_sar;
    }

    public String getPurchase_total() {
        return purchase_total;
    }

    public void setPurchase_total(String purchase_total) {
        this.purchase_total = purchase_total;
    }

    public String getPurchase_total_sar() {
        return purchase_total_sar;
    }

    public void setPurchase_total_sar(String purchase_total_sar) {
        this.purchase_total_sar = purchase_total_sar;
    }

    public String getTransaction_fee() {
        return transaction_fee;
    }

    public void setTransaction_fee(String transaction_fee) {
        this.transaction_fee = transaction_fee;
    }

    public String getTransaction_fee_sar() {
        return transaction_fee_sar;
    }

    public void setTransaction_fee_sar(String transaction_fee_sar) {
        this.transaction_fee_sar = transaction_fee_sar;
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

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public Object getOrder_number() {
        return order_number;
    }

    public void setOrder_number(Object order_number) {
        this.order_number = order_number;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getEntage_page_id() {
        return entage_page_id;
    }

    public void setEntage_page_id(String entage_page_id) {
        this.entage_page_id = entage_page_id;
    }

    public String getEntage_page_user_id() {
        return entage_page_user_id;
    }

    public void setEntage_page_user_id(String entage_page_user_id) {
        this.entage_page_user_id = entage_page_user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
