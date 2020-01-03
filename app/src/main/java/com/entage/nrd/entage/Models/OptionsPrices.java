package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class OptionsPrices implements Parcelable {

    private ArrayList<String> optionsTitle;
    private ArrayList<ArrayList<String>> options;

    private ArrayList<ArrayList<String>> linkingOptions;

    private ArrayList<String> prices ;

    private String main_price;

    private String currency_price;

    private String default_option;

    private boolean is_price_unify;

    public OptionsPrices() {
    }

    public OptionsPrices(ArrayList<String> optionsTitle, ArrayList<ArrayList<String>> options,
                         ArrayList<ArrayList<String>> linkingOptions, ArrayList<String> prices,
                         String main_price, String currency_price, String default_option, boolean is_price_unify) {
        this.optionsTitle = optionsTitle;
        this.options = options;
        this.linkingOptions = linkingOptions;
        this.prices = prices;
        this.main_price = main_price;
        this.currency_price = currency_price;
        this.default_option = default_option;
        this.is_price_unify = is_price_unify;
    }

    protected OptionsPrices(Parcel in) {
        optionsTitle = in.createStringArrayList();
        prices = in.createStringArrayList();
        main_price = in.readString();
        currency_price = in.readString();
        default_option = in.readString();
        is_price_unify = in.readByte() != 0;
    }

    public static final Creator<OptionsPrices> CREATOR = new Creator<OptionsPrices>() {
        @Override
        public OptionsPrices createFromParcel(Parcel in) {
            return new OptionsPrices(in);
        }

        @Override
        public OptionsPrices[] newArray(int size) {
            return new OptionsPrices[size];
        }
    };

    public ArrayList<String> getOptionsTitle() {
        return optionsTitle;
    }

    public void setOptionsTitle(ArrayList<String> optionsTitle) {
        this.optionsTitle = optionsTitle;
    }

    public ArrayList<ArrayList<String>> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<ArrayList<String>> options) {
        this.options = options;
    }

    public ArrayList<ArrayList<String>> getLinkingOptions() {
        return linkingOptions;
    }

    public void setLinkingOptions(ArrayList<ArrayList<String>> linkingOptions) {
        this.linkingOptions = linkingOptions;
    }

    public ArrayList<String> getPrices() {
        return prices;
    }

    public void setPrices(ArrayList<String> prices) {
        this.prices = prices;
    }

    public String getMain_price() {
        return main_price;
    }

    public void setMain_price(String main_price) {
        this.main_price = main_price;
    }

    public String getCurrency_price() {
        return currency_price;
    }

    public void setCurrency_price(String currency_price) {
        this.currency_price = currency_price;
    }

    public String getDefault_option() {
        return default_option;
    }

    public void setDefault_option(String default_option) {
        this.default_option = default_option;
    }

    public boolean isIs_price_unify() {
        return is_price_unify;
    }

    public void setIs_price_unify(boolean is_price_unify) {
        this.is_price_unify = is_price_unify;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(optionsTitle);
        dest.writeStringList(prices);
        dest.writeString(main_price);
        dest.writeString(currency_price);
        dest.writeString(default_option);
        dest.writeByte((byte) (is_price_unify ? 1 : 0));
    }
}
