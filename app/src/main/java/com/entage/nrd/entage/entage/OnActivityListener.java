package com.entage.nrd.entage.entage;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import com.entage.nrd.entage.Models.ItemWithDataUser;

public interface OnActivityListener {

    void onActivityListener(Fragment fragment);

    void onActivityListener(Fragment fragment, Bundle bundle);

    void onActivityListener_noStuck(Fragment fragment);

    void onActivityListener_noStuck(Fragment fragment, Bundle bundle);

    void onGridImageSelected(ItemWithDataUser itemWithDataUser);
}
