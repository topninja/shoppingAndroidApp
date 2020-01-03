package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class DescriptionItem implements Parcelable {

    private String item_id;
    private String content_html;
    private ArrayList<String> images_url;


    public DescriptionItem() {
    }

    public DescriptionItem(String item_id, String content_html, ArrayList<String> images_url) {
        this.item_id = item_id;
        this.content_html = content_html;
        this.images_url = images_url;
    }

    protected DescriptionItem(Parcel in) {
        item_id = in.readString();
        content_html = in.readString();
        images_url = in.createStringArrayList();
    }

    public static final Creator<DescriptionItem> CREATOR = new Creator<DescriptionItem>() {
        @Override
        public DescriptionItem createFromParcel(Parcel in) {
            return new DescriptionItem(in);
        }

        @Override
        public DescriptionItem[] newArray(int size) {
            return new DescriptionItem[size];
        }
    };

    public String getContent_html() {
        return content_html;
    }

    public void setContent_html(String content_html) {
        this.content_html = content_html;
    }

    public ArrayList<String> getImages_url() {
        return images_url;
    }

    public void setImages_url(ArrayList<String> images_url) {
        this.images_url = images_url;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(item_id);
        dest.writeString(content_html);
        dest.writeStringList(images_url);
    }


    @Override
    public String toString() {
        return "DescriptionItem{" +
                "item_id='" + item_id + '\'' +
                ", content_html='" + content_html + '\'' +
                ", images_url=" + images_url +
                '}';
    }
}
