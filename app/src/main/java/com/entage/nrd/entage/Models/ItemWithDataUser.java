package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemWithDataUser implements Parcelable {

    private Item item;
    private boolean isLiked;
    private boolean isInWishList;

    public ItemWithDataUser() {

    }

    public ItemWithDataUser(Item item, boolean isLiked, boolean isInWishList) {
        this.item = item;
        this.isLiked = isLiked;
        this.isInWishList = isInWishList;
    }

    protected ItemWithDataUser(Parcel in) {
        item = in.readParcelable(Item.class.getClassLoader());
        isLiked = in.readByte() != 0;
        isInWishList = in.readByte() != 0;
    }

    public static final Creator<ItemWithDataUser> CREATOR = new Creator<ItemWithDataUser>() {
        @Override
        public ItemWithDataUser createFromParcel(Parcel in) {
            return new ItemWithDataUser(in);
        }

        @Override
        public ItemWithDataUser[] newArray(int size) {
            return new ItemWithDataUser[size];
        }
    };

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isInWishList() {
        return isInWishList;
    }

    public void setInWishList(boolean inWishList) {
        isInWishList = inWishList;
    }

    @Override
    public String toString() {
        return "ItemWithDataUser{" +
                "item=" + item +
                ", isLiked=" + isLiked +
                ", isInWishList=" + isInWishList +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(item, flags);
        dest.writeByte((byte) (isLiked ? 1 : 0));
        dest.writeByte((byte) (isInWishList ? 1 : 0));
    }
}
