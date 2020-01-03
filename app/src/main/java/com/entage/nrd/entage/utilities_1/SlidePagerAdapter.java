package com.entage.nrd.entage.utilities_1;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class SlidePagerAdapter extends FragmentPagerAdapter {

    private int NUM_ITEMS ;
    private ArrayList<Fragment> fragments;

    public SlidePagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
        this.NUM_ITEMS = fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        /*
         * IMPORTANT: This is the point. We create a RootFragment acting as
         * a container for other fragments
         */
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}