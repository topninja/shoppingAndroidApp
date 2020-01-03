package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class Advice {

    private String title;
    private String[] information;
    private boolean open;
    private int img_res;


    public Advice(String title, String[] information, boolean open, int img_res) {
        this.title = title;
        this.information = information;
        this.open = open;
        this.img_res = img_res;
    }

    public int getImg_res() {
        return img_res;
    }

    public void setImg_res(int img_res) {
        this.img_res = img_res;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getInformation() {
        return information;
    }

    public void setInformation(String[] information) {
        this.information = information;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
