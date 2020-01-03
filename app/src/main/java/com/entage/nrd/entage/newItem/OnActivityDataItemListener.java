package com.entage.nrd.entage.newItem;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.Models.DescriptionItem;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnActivityDataItemListener {

    void onActivityListener(Fragment fragment);

    void onActivityListener(Fragment fragment, Bundle bundle);

    void onActivityListener_noStuck(Fragment fragment);

    void onActivityListener_noStuck(Fragment fragment, Bundle bundle);

    void setTitle(String title);

    void setItemId(String itemId);

    void setIconBack(int resId);

    void setSavedDescriptions(DescriptionItem descriptions, boolean clearAndSet);

    DescriptionItem getSavedDescriptions();

    boolean getClearAndSet();

    void setGroupSpecifications(ArrayList<DataSpecifications> selectedSpecifications, boolean clearAndSet);

    void setSelectedSpecifications(ArrayList<String> selectedSpecifications);

    ArrayList<String> getSelectedSpecifications();

    ArrayList<DataSpecifications> getGroupSpecifications();

    HashMap<String, HashMap<String, AreaShippingAvailable>> getAreaShippingAvailable();

    void setAreaShippingAvailable(HashMap<String, HashMap<String, AreaShippingAvailable>> areaShippingAvailable);

    ArrayList<ReceivingLocation> getReceivingLocation();

    void setReceivingLocation(ArrayList<ReceivingLocation> receivingLocation);

}
