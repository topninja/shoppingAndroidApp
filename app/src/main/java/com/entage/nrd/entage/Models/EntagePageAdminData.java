package com.entage.nrd.entage.Models;

import java.util.Date;

public class EntagePageAdminData{


    private String entage_id;
    private String user_id;
    private Date date_created;
    private String package_entage_page;
    private Object entage_page_number;

    public EntagePageAdminData(String entage_id, String user_id, Date date_created, String package_entage_page) {
        this.entage_id = entage_id;
        this.user_id = user_id;
        this.date_created = date_created;
        this.package_entage_page = package_entage_page;
    }

    public EntagePageAdminData() {

    }

    public Object getEntage_page_number() {
        return entage_page_number;
    }

    public void setEntage_page_number(Object entage_page_number) {
        this.entage_page_number = entage_page_number;
    }

    public String getEntage_id() {
        return entage_id;
    }

    public void setEntage_id(String entage_id) {
        this.entage_id = entage_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }


    public String getPackage_entage_page() {
        return package_entage_page;
    }

    public void setPackage_entage_page(String package_entage_page) {
        this.package_entage_page = package_entage_page;
    }



    @Override
    public String toString() {
        return "EntagePageAdminData{" +
                "entage_id='" + entage_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", date_created='" + date_created + '\'' +
                ", package_entage_page='" + package_entage_page + '\'' +
                '}';
    }


}