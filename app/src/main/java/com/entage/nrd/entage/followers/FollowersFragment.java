package com.entage.nrd.entage.followers;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;



public class FollowersFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int ACTIVITY_NUM = 3 ;

    //vars
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private View view;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_viewpager_tablayout , container , false);

        mContext = getActivity();

        init();
        //setUpBottomeNavigationView();

        return view;
    }

    private void init() {
        initWidgets();
        onClickListener();

        setupViewPager();
    }

    private void initWidgets(){
        tabLayout = view.findViewById(R.id.tabs);
        viewPager =  view.findViewById(R.id.viewpager);
    }

    private void onClickListener(){

    }

    private void setupViewPager(){

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter

        adapter.addFragment(new PagesFollowingFragment()); // index 0
        adapter.addFragment(new ItemsLikedFragment()); // index 1

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("صفحات انتاجي");
        tabLayout.getTabAt(1).setText("السلع");

    }



}
