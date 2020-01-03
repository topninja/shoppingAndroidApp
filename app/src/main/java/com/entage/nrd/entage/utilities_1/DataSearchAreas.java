package com.entage.nrd.entage.utilities_1;

public class DataSearchAreas {

    String nameArea;
    boolean selectArea ;
    boolean freeShipping ;


    public DataSearchAreas(String nameArea, boolean selectArea, boolean checkBoxIsFreeShipping) {
        this.nameArea = nameArea;
        this.selectArea = selectArea;
        this.freeShipping = checkBoxIsFreeShipping;
    }

    public String getNameArea() {
        return nameArea;
    }

    public void setNameArea(String nameArea) {
        this.nameArea = nameArea;
    }

    public boolean isSelectArea() {
        return selectArea;
    }

    public void setSelectArea(boolean selectArea) {
        this.selectArea = selectArea;
    }

    public boolean isFreeShipping() {
        return freeShipping;
    }

    public void setFreeShipping(boolean freeShipping) {
        this.freeShipping = freeShipping;
    }
}
