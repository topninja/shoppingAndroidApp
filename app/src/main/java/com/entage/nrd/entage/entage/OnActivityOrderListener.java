package com.entage.nrd.entage.entage;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.Models.Order;

public interface OnActivityOrderListener {

/*    void onActivityListener(Fragment fragment);

    void onActivityListener(Fragment fragment, Bundle bundle);

    void onActivityListener_noStuck(Fragment fragment);

    void onActivityListener_noStuck(Fragment fragment, Bundle bundle);*/

    void setCountNewMsg(int count, String ordersType);
}
