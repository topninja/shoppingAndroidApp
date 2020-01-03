package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class Withdraw {

    private ArrayList<String> withdraw_from;
    private String withdraw_from_amount;


    public Withdraw() {
    }

    public Withdraw(ArrayList<String> withdraw_from, String withdraw_from_amount) {
        this.withdraw_from = withdraw_from;
        this.withdraw_from_amount = withdraw_from_amount;
    }

    public ArrayList<String> getWithdraw_from() {
        return withdraw_from;
    }

    public void setWithdraw_from(ArrayList<String> withdraw_from) {
        this.withdraw_from = withdraw_from;
    }

    public String getWithdraw_from_amount() {
        return withdraw_from_amount;
    }

    public void setWithdraw_from_amount(String withdraw_from_amount) {
        this.withdraw_from_amount = withdraw_from_amount;
    }
}
