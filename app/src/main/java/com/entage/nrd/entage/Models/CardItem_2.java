package com.entage.nrd.entage.Models;

public class CardItem_2 {

    private String mTextResource;
    private String mTitleResource;

    public CardItem_2() {
    }

    public CardItem_2(String title, String text) {
        mTitleResource = title;
        mTextResource = text;
    }

    public String getText() {
        return mTextResource;
    }

    public String getTitle() {
        return mTitleResource;
    }
}

