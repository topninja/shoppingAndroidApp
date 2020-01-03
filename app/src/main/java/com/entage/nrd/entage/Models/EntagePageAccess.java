package com.entage.nrd.entage.Models;

public class EntagePageAccess {

    private String entage_id;
    private String user_id;
    private boolean authorization_access;
    private boolean admin;

    public EntagePageAccess(String entage_id, String user_id, boolean authorization_access, boolean admin) {
        this.entage_id = entage_id;
        this.user_id = user_id;
        this.authorization_access = authorization_access;
        this.admin = admin;
    }

    public EntagePageAccess() {

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

    public boolean isAuthorization_access() {
        return authorization_access;
    }

    public void setAuthorization_access(boolean authorization_access) {
        this.authorization_access = authorization_access;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return "EntagePageAccess{" +
                "entage_id='" + entage_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", authorization_access=" + authorization_access +
                ", admin=" + admin +
                '}';
    }
}
