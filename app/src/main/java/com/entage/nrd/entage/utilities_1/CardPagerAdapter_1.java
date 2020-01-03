package com.entage.nrd.entage.utilities_1;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.entage.nrd.entage.Models.CardItem_1;
import com.entage.nrd.entage.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

public class CardPagerAdapter_1 extends PagerAdapter {
    private static final String TAG = "CardPagerAdapter_1";

    private Context mContext;
    private  List<CardItem_1> cardItems;

    public CardPagerAdapter_1(Context context, List<CardItem_1> cardItems) {
        mContext = context;
        this.cardItems = cardItems;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_card_1, collection, false);
        ImageView imageView = layout.findViewById(R.id.image) ;

        CardItem_1 cardItem = cardItems.get(position);
        if(cardItem.getImg_url() != null){
            if(cardItem.isIs_from_db()){

                UniversalImageLoader.setImage(cardItem.getImg_url(), imageView, null ,"");
            }else {
                imageView.setImageResource(Integer.parseInt(cardItem.getImg_url()));
            }
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
        return cardItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (CharSequence) cardItems.get(position);
    }

    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image. ");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

    }

}
