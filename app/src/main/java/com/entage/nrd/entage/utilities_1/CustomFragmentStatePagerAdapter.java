package com.entage.nrd.entage.utilities_1;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.entage.nrd.entage.entage.FragmentCategorie;

import java.util.ArrayList;

public class CustomFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = "CustomFragmentStatePage";

    private ArrayList<FragmentCategorie> pages;

    public CustomFragmentStatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public CustomFragmentStatePagerAdapter(FragmentManager fragmentManager, ArrayList<FragmentCategorie> pages) {
        super(fragmentManager);
        this.pages = pages;
    }

    @Override
    public Fragment getItem(int index) {
        return pages.get(index);
    }

    @Override
    public int getCount() {
        return pages.size();
    }


    /*
@Override
    public Parcelable saveState() {
        return null;
    }
 @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        try {
            super.restoreState(state, loader);
        } catch (Exception e) {
            Log.e("TAG", "Error Restore State of Fragment : " + e.getMessage(), e);
        }
    }
    */

    @Override
    public int getItemPosition(Object object) {
        int index = pages.indexOf (object);

        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }
}