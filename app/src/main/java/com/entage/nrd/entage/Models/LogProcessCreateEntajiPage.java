package com.entage.nrd.entage.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class LogProcessCreateEntajiPage {

    private EntagePageAdminData entagePageAdminData;
    private EntagePageAccess entagePageAccess;
    private EntagePageSettings entagePageSettings;
    private EntagePage entagePage;

    private HashMap<String,Object> itemToAlgolia;

    private NotificationOnApp notificationOnApp;
    private Notification notification;

    private ArrayList<ArrayList<String>> categoriesForDb;

    private String tokenId;

    HashMap<String, Boolean> logs ;

    public LogProcessCreateEntajiPage() {
    }

    public LogProcessCreateEntajiPage(EntagePageAdminData entagePageAdminData, EntagePageAccess entagePageAccess,
                                      EntagePageSettings entagePageSettings, EntagePage entagePage, HashMap<String, Boolean> logs) {
        this.entagePageAdminData = entagePageAdminData;
        this.entagePageAccess = entagePageAccess;
        this.entagePageSettings = entagePageSettings;
        this.entagePage = entagePage;
        this.logs = logs;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public ArrayList<ArrayList<String>> getCategoriesForDb() {
        return categoriesForDb;
    }

    public void setCategoriesForDb(ArrayList<ArrayList<String>> categoriesForDb) {
        this.categoriesForDb = categoriesForDb;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public NotificationOnApp getNotificationOnApp() {
        return notificationOnApp;
    }

    public void setNotificationOnApp(NotificationOnApp notificationOnApp) {
        this.notificationOnApp = notificationOnApp;
    }

    public HashMap<String, Object> getItemToAlgolia() {
        return itemToAlgolia;
    }

    public void setItemToAlgolia(HashMap<String, Object> itemToAlgolia) {
        this.itemToAlgolia = itemToAlgolia;
    }

    public EntagePageAdminData getEntagePageAdminData() {
        return entagePageAdminData;
    }

    public void setEntagePageAdminData(EntagePageAdminData entagePageAdminData) {
        this.entagePageAdminData = entagePageAdminData;
    }

    public EntagePageAccess getEntagePageAccess() {
        return entagePageAccess;
    }

    public void setEntagePageAccess(EntagePageAccess entagePageAccess) {
        this.entagePageAccess = entagePageAccess;
    }

    public EntagePageSettings getEntagePageSettings() {
        return entagePageSettings;
    }

    public void setEntagePageSettings(EntagePageSettings entagePageSettings) {
        this.entagePageSettings = entagePageSettings;
    }

    public EntagePage getEntagePage() {
        return entagePage;
    }

    public void setEntagePage(EntagePage entagePage) {
        this.entagePage = entagePage;
    }

    public HashMap<String, Boolean> getLogs() {
        return logs;
    }

    public void setLogs(HashMap<String, Boolean> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "LogProcessCreateEntajiPage{" +
                "entagePageAdminData=" + entagePageAdminData +
                ", entagePageAccess=" + entagePageAccess +
                ", entagePageSettings=" + entagePageSettings +
                ", entagePage=" + entagePage +
                ", logs=" + logs +
                '}';
    }
}
