package com.entage.nrd.entage.Models;

public class TransferMoneyRequest {

    private String user_id;

    private String transfer_by;
    private String email;
    private String amount;
    private String total_sar, total_usd;
    private String transaction_fee;
    private String currency;

    private String status;
    private String time;
    private Object number_request;
    private Object timestamp;

    private String request_id;

    public TransferMoneyRequest() {
    }

    public TransferMoneyRequest(String user_id, String transfer_by, String email, String amount, String total_sar, String total_usd,
                                String transaction_fee, String currency, String status,
                                String time, Object number_request, Object timestamp, String request_id) {
        this.user_id = user_id;
        this.transfer_by = transfer_by;
        this.email = email;
        this.amount = amount;
        this.total_sar = total_sar;
        this.total_usd = total_usd;
        this.transaction_fee = transaction_fee;
        this.currency = currency;
        this.status = status;
        this.time = time;
        this.number_request = number_request;
        this.timestamp = timestamp;
        this.request_id = request_id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getTransaction_fee() {
        return transaction_fee;
    }

    public void setTransaction_fee(String transaction_fee) {
        this.transaction_fee = transaction_fee;
    }

    public String getTransfer_by() {
        return transfer_by;
    }

    public void setTransfer_by(String transfer_by) {
        this.transfer_by = transfer_by;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTotal_sar() {
        return total_sar;
    }

    public void setTotal_sar(String total_sar) {
        this.total_sar = total_sar;
    }

    public String getTotal_usd() {
        return total_usd;
    }

    public void setTotal_usd(String total_usd) {
        this.total_usd = total_usd;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getNumber_request() {
        return number_request;
    }

    public void setNumber_request(Object number_request) {
        this.number_request = number_request;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
