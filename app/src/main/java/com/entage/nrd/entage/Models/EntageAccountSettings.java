package com.entage.nrd.entage.Models;

public class EntageAccountSettings {

    private String user_id;
    private String entage_id;
    private String email;
    private long phone_number;
    private long categories_number;
    private String categorie_1;
    private String categorie_2;
    private String categorie_3;
    private String categorie_4;
    private String categorie_5;
    private String categorie_6;

    public EntageAccountSettings(String user_id, String entage_id, String email, long phone_number,
                                 long categories_number, String categorie_1, String categorie_2, String categorie_3,
                                 String categorie_4, String categorie_5, String categorie_6) {
        this.user_id = user_id;
        this.entage_id = entage_id;
        this.email = email;
        this.phone_number = phone_number;
        this.categories_number = categories_number;
        this.categorie_1 = categorie_1;
        this.categorie_2 = categorie_2;
        this.categorie_3 = categorie_3;
        this.categorie_4 = categorie_4;
        this.categorie_5 = categorie_5;
        this.categorie_6 = categorie_6;
    }

    public EntageAccountSettings() {
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEntage_id() {
        return entage_id;
    }

    public void setEntage_id(String entage_id) {
        this.entage_id = entage_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(long phone_number) {
        this.phone_number = phone_number;
    }

    public long getCategories_number() {
        return categories_number;
    }

    public void setCategories_number(long categories_number) {
        this.categories_number = categories_number;
    }

    public String getCategorie_1() {
        return categorie_1;
    }

    public void setCategorie_1(String categorie_1) {
        this.categorie_1 = categorie_1;
    }

    public String getCategorie_2() {
        return categorie_2;
    }

    public void setCategorie_2(String categorie_2) {
        this.categorie_2 = categorie_2;
    }

    public String getCategorie_3() {
        return categorie_3;
    }

    public void setCategorie_3(String categorie_3) {
        this.categorie_3 = categorie_3;
    }

    public String getCategorie_4() {
        return categorie_4;
    }

    public void setCategorie_4(String categorie_4) {
        this.categorie_4 = categorie_4;
    }

    public String getCategorie_5() {
        return categorie_5;
    }

    public void setCategorie_5(String categorie_5) {
        this.categorie_5 = categorie_5;
    }

    public String getCategorie_6() {
        return categorie_6;
    }

    public void setCategorie_6(String categorie_6) {
        this.categorie_6 = categorie_6;
    }

    @Override
    public String toString() {
        return "EntageAccountSettings{" +
                "user_id='" + user_id + '\'' +
                ", entage_id='" + entage_id + '\'' +
                ", email='" + email + '\'' +
                ", phone_number=" + phone_number +
                ", categories_number=" + categories_number +
                ", categorie_1='" + categorie_1 + '\'' +
                ", categorie_2='" + categorie_2 + '\'' +
                ", categorie_3='" + categorie_3 + '\'' +
                ", categorie_4='" + categorie_4 + '\'' +
                ", categorie_5='" + categorie_5 + '\'' +
                ", categorie_6='" + categorie_6 + '\'' +
                '}';
    }
}
