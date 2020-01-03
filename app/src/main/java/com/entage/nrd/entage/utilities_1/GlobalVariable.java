package com.entage.nrd.entage.utilities_1;

import android.app.Application;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.Models.DescriptionItem;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.Following;
import com.entage.nrd.entage.Models.LocationInformation;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;

public class GlobalVariable  extends Application {

    private final String ApplicationID = "W1UDE71XLR";
    private final String APIKey = "dd6977780336b9e0b8b841ca959dbb92";
    private CategorieWithChildren categoriesLists ;
    public static final String GEONAMES_USERNAME = "entage"; // GeonamesSearch

    private LocationInformation locationInformation;

    private Currency currency ;

    private String language ;

   // private int itemInBasket ;

    private int unreadMessages ;

    private ArrayList<String> wishList = new ArrayList<>();

    private HashMap<String, Boolean> likesList = new HashMap<>();

    private ArrayList<String> followingList = new ArrayList<>();

    private HashMap<String, EntagePage> entagePages = new HashMap<>();

    private HashMap<String, Following> followingData = new HashMap<>();

    private ArrayList<String> mySinglesSpecifications ;

    private HashMap<String, DescriptionItem> savedDataDescriptions;

    private HashMap<String, ArrayList<DataSpecifications> > myGroupSpecifications;

    //
    public void clear(){
        wishList.clear();
        likesList.clear();
        followingList.clear();
        followingData.clear();
        //itemInBasket=0;
        unreadMessages = 0;
        //locationInformation = null;
        if(mySinglesSpecifications != null){
            mySinglesSpecifications.clear();
        }
        if(savedDataDescriptions != null){
            savedDataDescriptions.clear();
        }
        if(myGroupSpecifications != null){
            myGroupSpecifications.clear();
        }
    }

    public String getApplicationID() {
        return ApplicationID;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public LocationInformation getLocationInformation() {
        return locationInformation;
    }

    public void setLocationInformation(LocationInformation locationInformation) {
        this.locationInformation = locationInformation;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /*public int getItemInBasket() {
        return itemInBasket;
    }

    public void setItemInBasket(int itemInBasket) {
        this.itemInBasket = itemInBasket;
    }*/

    public int getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(int unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public CategorieWithChildren getCategoriesLists() {
        return categoriesLists;
    }

    public void setCategoriesLists(CategorieWithChildren categoriesLists) {
        this.categoriesLists = categoriesLists;
    }

    public ArrayList<String> getWishList() {
        return wishList;
    }

    public HashMap<String, Boolean> getLikesList() {
        return likesList;
    }

    public ArrayList<String> getFollowingList() {
        return followingList;
    }

    public HashMap<String, EntagePage> getEntagePages() {
        return entagePages;
    }

    public HashMap<String, Following> getFollowingData() {
        return followingData;
    }

    public ArrayList<String> getMySinglesSpecifications() {
        return mySinglesSpecifications;
    }

    public void setMySinglesSpecifications(ArrayList<String> mySinglesSpecifications) {
        this.mySinglesSpecifications = mySinglesSpecifications;
    }

    public HashMap<String, DescriptionItem> getSavedDataDescriptions() {
        return savedDataDescriptions;
    }

    public void setSavedDataDescriptions(HashMap<String, DescriptionItem> savedDataDescriptions) {
        this.savedDataDescriptions = savedDataDescriptions;
    }

    public HashMap<String, ArrayList<DataSpecifications> > getMyGroupSpecifications() {
        return myGroupSpecifications;
    }

    public void setMyGroupSpecifications(HashMap<String, ArrayList<DataSpecifications>> myGroupSpecifications) {
        this.myGroupSpecifications = myGroupSpecifications;
    }
}
