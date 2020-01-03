package com.entage.nrd.entage.Models;

public class DataViewCategorie {

    private String code;
    private String image_url;
    private String color_hex;

    public DataViewCategorie() {

    }

    public DataViewCategorie(String code, String image_url, String color_hex) {
        this.code = code;
        this.image_url = image_url;
        this.color_hex = color_hex;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getColor_hex() {
        return color_hex;
    }

    public void setColor_hex(String color_hex) {
        this.color_hex = color_hex;
    }


    @Override
    public String toString() {
        return "DataViewCategorie{" +
                "code='" + code + '\'' +
                ", image_url='" + image_url + '\'' +
                ", color_hex='" + color_hex + '\'' +
                '}';
    }
}
