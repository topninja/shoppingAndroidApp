package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterConversationMessages;

import java.util.ArrayList;
import java.util.List;

public class CustomPagerAdapterRecyclerView extends PagerAdapter {
    private static final String TAG = "CustomPagerAdapter";

    private Context mContext;
    private List<AdapterConversationMessages> adapterCM;
    private List<String> pageTitle;

    public CustomPagerAdapterRecyclerView(Context context, List<AdapterConversationMessages> adapterCM) {
            mContext = context;
            this.adapterCM = adapterCM;

            pageTitle = new ArrayList<>();
            for(int i=0; i<adapterCM.size(); i++){
                pageTitle.add(String.valueOf(i+1));
            }
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.recycler_view, collection, false);
            RecyclerView recyclerView = layout.findViewById(R.id.recyclerView) ;

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapterCM.get(position));

            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return adapterCM.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitle.get(position);
        }
    }
