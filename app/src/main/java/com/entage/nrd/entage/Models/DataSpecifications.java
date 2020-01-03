package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class DataSpecifications implements Parcelable {


    private String specifications_id;
    private String specifications;
    private String data;


    public DataSpecifications() {

    }

    public DataSpecifications(String specifications_id, String specifications, String data) {
        this.specifications_id = specifications_id;
        this.specifications = specifications;
        this.data = data;
    }

    protected DataSpecifications(Parcel in) {
        specifications_id = in.readString();
        specifications = in.readString();
        data = in.readString();
    }

    public static final Creator<DataSpecifications> CREATOR = new Creator<DataSpecifications>() {
        @Override
        public DataSpecifications createFromParcel(Parcel in) {
            return new DataSpecifications(in);
        }

        @Override
        public DataSpecifications[] newArray(int size) {
            return new DataSpecifications[size];
        }
    };

    public String getSpecifications_id() {
        return specifications_id;
    }

    public void setSpecifications_id(String specifications_id) {
        this.specifications_id = specifications_id;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(specifications_id);
        dest.writeString(specifications);
        dest.writeString(data);
    }
}
