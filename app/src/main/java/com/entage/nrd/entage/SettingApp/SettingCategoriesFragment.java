package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class SettingCategoriesFragment extends Fragment {
    private static final String TAG = "SettingCategoriesFrag";

    private OnActivityListener mOnActivityListener;
    private View view;
    private Context mContext;

    private ImageView back;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_setting_categories, container, false);
        mContext = getActivity();
        inti();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnActivityListener = (OnActivityListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException;" + e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti() {
        initWidgets();
        onClickListener();

        viewPager = view.findViewById(R.id.viewpager);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter


        adapter.addFragment(new SettingCategoriesAddFragment()); // index 0
        adapter.addFragment(new SettingCategoriesEditFragment()); // index 1

        viewPager.setAdapter(adapter);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(1).setText("تعديل فئات");
        tabLayout.getTabAt(0).setText("إنشاء فئات");

    }

    private void initWidgets() {
        back = view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
    }

    private void onClickListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }


}
