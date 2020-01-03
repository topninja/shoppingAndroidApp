package com.entage.nrd.entage.Models;

import java.util.ArrayList;

public class OptionsItem {

    String title;
    ArrayList<String> options, selectedOptions;


    public OptionsItem(String title, ArrayList<String> options, ArrayList<String> selectedOptions) {
        this.title = title;
        this.options = options;
        this.selectedOptions = selectedOptions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public ArrayList<String> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(ArrayList<String> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    @Override
    public String toString() {
        return "OptionsItem{" +
                "title='" + title + '\'' +
                ", options=" + options +
                ", selectedOptions=" + selectedOptions +
                '}';
    }
}
