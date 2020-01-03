package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Question implements Parcelable {

    private CustomerQuestion customerQuestion;
    private SellerAnswer sellerAnswer;

    public Question() {

    }

    public Question(CustomerQuestion customerQuestion, SellerAnswer sellerAnswer) {
        this.customerQuestion = customerQuestion;

        this.sellerAnswer = sellerAnswer;
    }

    protected Question(Parcel in) {
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    public CustomerQuestion getCustomerQuestion() {
        return customerQuestion;
    }

    public void setCustomerQuestion(CustomerQuestion customerQuestion) {
        this.customerQuestion = customerQuestion;
    }

    public SellerAnswer getSellerAnswer() {
        return sellerAnswer;
    }

    public void setSellerAnswer(SellerAnswer sellerAnswer) {
        this.sellerAnswer = sellerAnswer;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
