package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class ProcessSearch {

    private int processNumber;

    private String dbName;
    private boolean searchForItems;
    private boolean thereText;
    private boolean thereCategories;

    private String text;
    private ArrayList<String> categories;

    private int resultCount;
    private String lastKey;

    private int countFailMatch;
    private int countSuccessMatch;

    private int countBeforeLoad;
    private int resultCountAdded;

    public ProcessSearch() {
    }


    public int getCountSuccessMatch() {
        return countSuccessMatch;
    }

    public void setCountSuccessMatch(int countSuccessMatch) {
        this.countSuccessMatch = countSuccessMatch;
    }

    public int getCountFailMatch() {
        return countFailMatch;
    }

    public void setCountFailMatch(int countFailMatch) {
        this.countFailMatch = countFailMatch;
    }

    public boolean isSearchForItems() {
        return searchForItems;
    }

    public void setSearchForItems(boolean searchForItems) {
        this.searchForItems = searchForItems;
    }

    public boolean isThereText() {
        return thereText;
    }

    public void setThereText(boolean thereText) {
        this.thereText = thereText;
    }

    public boolean isThereCategories() {
        return thereCategories;
    }

    public void setThereCategories(boolean thereCategories) {
        this.thereCategories = thereCategories;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    public String getLastKey() {
        return lastKey;
    }

    public void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }

    public int getProcessNumber() {
        return processNumber;
    }

    public void setProcessNumber(int processNumber) {
        this.processNumber = processNumber;
    }

    public int getCountBeforeLoad() {
        return countBeforeLoad;
    }

    public void setCountBeforeLoad(int countBeforeLoad) {
        this.countBeforeLoad = countBeforeLoad;
    }

    public int getResultCountAdded() {
        return resultCountAdded;
    }

    public void setResultCountAdded(int resultCountAdded) {
        this.resultCountAdded = resultCountAdded;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String toString() {
        return "ProcessSearch{" +
                "processNumber=" + processNumber +
                ", dbName='" + dbName + '\'' +
                ", searchForItems=" + searchForItems +
                ", thereText=" + thereText +
                ", thereCategories=" + thereCategories +
                ", text='" + text + '\'' +
                ", categories=" + categories +
                ", resultCount=" + resultCount +
                ", lastKey='" + lastKey + '\'' +
                ", countFailMatch=" + countFailMatch +
                ", countSuccessMatch=" + countSuccessMatch +
                ", countBeforeLoad=" + countBeforeLoad +
                ", resultCountAdded=" + resultCountAdded +
                '}';
    }
}
