package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.entage.nrd.entage.createEntagePage.FragmentShowInfo;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;

public class CustomPagerAdapter extends PagerAdapter {
    private static final String TAG = "CustomPagerAdapter";

    private Context mContext;
    private ModelObject mModelObject;
    private ViewGroup[] viewGroup ;

    private OnActivityListener onActivityListener;

    public CustomPagerAdapter(Context context, ModelObject modelObject) {
            mContext = context;
            mModelObject = modelObject;
            viewGroup= new ViewGroup[mModelObject.getLength()];

        try{
            onActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        }

        @Override
        public Object instantiateItem(ViewGroup collection, final int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(mModelObject.getLayoutResId(position), collection, false);
            if(viewGroup[position] == null){
                viewGroup[position] = layout;
            }
            final TextView textView = layout.findViewById(R.id.text3);
            if(textView != null){
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        if(textView.getText().toString().equals(mContext.getString(R.string.what_are_entage_features_ans_3))){
                            bundle.putString("info", mContext.getString(R.string.what_are_entage_features_ans_3));
                            onActivityListener.onActivityListener(new FragmentShowInfo(), bundle);
                        }else if(textView.getText().toString().equals(mContext.getString(R.string.see_details_fees_sale))){
                            bundle.putString("info", mContext.getString(R.string.see_details_fees_sale));
                            onActivityListener.onActivityListener(new FragmentShowInfo(), bundle);
                        }else if(textView.getText().toString().equals(mContext.getString(R.string.see_packages))){
                            bundle.putString("info", mContext.getString(R.string.see_packages));
                            onActivityListener.onActivityListener(new FragmentShowInfo(), bundle);
                        }
                    }
                });
            }
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mModelObject.getLength();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mModelObject.getTitleResId(position);
        }

        public ViewGroup getViewAt(int position){
            return viewGroup[position];
        }

    }
