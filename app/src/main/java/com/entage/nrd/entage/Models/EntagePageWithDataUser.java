package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class EntagePageWithDataUser implements Parcelable {

    private EntagePage entagePage;
    private boolean checkingFollow;
    private boolean followed;
    private Following followingData;

    public EntagePageWithDataUser() {

    }

    public EntagePageWithDataUser(EntagePage entagePage, boolean checkingFollow, boolean followed, Following followingData) {
        this.entagePage = entagePage;
        this.checkingFollow = checkingFollow;
        this.followed = followed;
        this.followingData = followingData;
    }

    protected EntagePageWithDataUser(Parcel in) {
        entagePage = in.readParcelable(EntagePage.class.getClassLoader());
        checkingFollow = in.readByte() != 0;
        followed = in.readByte() != 0;
    }

    public static final Creator<EntagePageWithDataUser> CREATOR = new Creator<EntagePageWithDataUser>() {
        @Override
        public EntagePageWithDataUser createFromParcel(Parcel in) {
            return new EntagePageWithDataUser(in);
        }

        @Override
        public EntagePageWithDataUser[] newArray(int size) {
            return new EntagePageWithDataUser[size];
        }
    };

    public EntagePage getEntagePage() {
        return entagePage;
    }

    public void setEntagePage(EntagePage entagePage) {
        this.entagePage = entagePage;
    }

    public boolean isCheckingFollow() {
        return checkingFollow;
    }

    public void setCheckingFollow(boolean checkingFollow) {
        this.checkingFollow = checkingFollow;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    public Following getFollowingData() {
        return followingData;
    }

    public void setFollowingData(Following followingData) {
        this.followingData = followingData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(entagePage, flags);
        dest.writeByte((byte) (checkingFollow ? 1 : 0));
        dest.writeByte((byte) (followed ? 1 : 0));
    }

    @Override
    public String toString() {
        return "EntagePageWithDataUser{" +
                "entagePage=" + entagePage +
                ", checkingFollow=" + checkingFollow +
                ", followed=" + followed +
                ", followingData=" + followingData +
                '}';
    }
}
