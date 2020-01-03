package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class Deposit {

    private ArrayList<String> deposit_to;
    private String deposit_to_amount;


    public Deposit() {
    }

    public Deposit(ArrayList<String> deposit_to, String deposit_to_amount) {
        this.deposit_to = deposit_to;
        this.deposit_to_amount = deposit_to_amount;
    }

    public ArrayList<String> getDeposit_to() {
        return deposit_to;
    }

    public void setDeposit_to(ArrayList<String> deposit_to) {
        this.deposit_to = deposit_to;
    }

    public String getDeposit_to_amount() {
        return deposit_to_amount;
    }

    public void setDeposit_to_amount(String deposit_to_amount) {
        this.deposit_to_amount = deposit_to_amount;
    }
}
