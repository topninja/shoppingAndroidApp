package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class EntagePageDivision implements Parcelable {

    private String division_name_db;
    private String division_name;
    private boolean is_public;

    public EntagePageDivision() {

    }

    public EntagePageDivision(String division_name_db, String division_name, boolean is_public) {
        this.division_name_db = division_name_db;
        this.division_name = division_name;
        this.is_public = is_public;
    }

    protected EntagePageDivision(Parcel in) {
        division_name_db = in.readString();
        division_name = in.readString();
        is_public = in.readByte() != 0;
    }

    public static final Creator<EntagePageDivision> CREATOR = new Creator<EntagePageDivision>() {
        @Override
        public EntagePageDivision createFromParcel(Parcel in) {
            return new EntagePageDivision(in);
        }

        @Override
        public EntagePageDivision[] newArray(int size) {
            return new EntagePageDivision[size];
        }
    };

    public String getDivision_name_db() {
        return division_name_db;
    }

    public void setDivision_name_db(String division_name_db) {
        this.division_name_db = division_name_db;
    }

    public String getDivision_name() {
        return division_name;
    }

    public void setDivision_name(String division_name) {
        this.division_name = division_name;
    }

    public boolean isIs_public() {
        return is_public;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(division_name_db);
        dest.writeString(division_name);
        dest.writeByte((byte) (is_public ? 1 : 0));
    }
}
