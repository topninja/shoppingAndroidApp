package com.entage.nrd.entage.Models;

public class User {

    private String user_id;
    private String username;
    private String username_lower_case;
    private String email;
    private String phone_number;
    private String anonymous_id;
    private String Sign_in_method ;



    public User() {
    }

    public User(String user_id, String username, String username_lower_case, String email, String phone_number, String anonymous_id,
                String sign_in_method) {
        this.user_id = user_id;
        this.username = username;
        this.username_lower_case = username_lower_case;
        this.email = email;
        this.phone_number = phone_number;
        this.anonymous_id = anonymous_id;
        Sign_in_method = sign_in_method;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername_lower_case() {
        return username_lower_case;
    }

    public void setUsername_lower_case(String username_lower_case) {
        this.username_lower_case = username_lower_case;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAnonymous_id() {
        return anonymous_id;
    }

    public void setAnonymous_id(String anonymous_id) {
        this.anonymous_id = anonymous_id;
    }

    public String getSign_in_method() {
        return Sign_in_method;
    }

    public void setSign_in_method(String sign_in_method) {
        Sign_in_method = sign_in_method;
    }


    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", username_lower_case='" + username_lower_case + '\'' +
                ", email='" + email + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", anonymous_id='" + anonymous_id + '\'' +
                ", Sign_in_method='" + Sign_in_method + '\'' +
                '}';
    }
}
