package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import java.util.List;

public class CustomPagerAdapterLayouts extends PagerAdapter {
    private static final String TAG = "CustomPagerAdapter";

    private Context mContext;
    private  List<Integer> resources;
    private boolean isFromDb = false;
    private String mAppend = "file:/";

    public CustomPagerAdapterLayouts(Context context, List<Integer> resources) {
            mContext = context;
            this.resources = resources;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(resources.get(position), collection, false);

        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return resources.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    }
