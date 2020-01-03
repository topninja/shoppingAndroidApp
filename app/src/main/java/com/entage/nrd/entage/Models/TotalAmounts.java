package com.entage.nrd.entage.Models;

public class TotalAmounts {


    private String total_sar, total_usd;


    public TotalAmounts() {
    }

    public TotalAmounts(String total_sar, String total_usd) {
        this.total_sar = total_sar;
        this.total_usd = total_usd;
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
}
