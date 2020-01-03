package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

public class CustomPagerAdapterItemsImg extends PagerAdapter {
    private static final String TAG = "CustomPagerAdapter";

    private Context mContext;
    private  List<String> Urls;
    private boolean isFromDb = false;
    private String mAppend = "file:/";

    public CustomPagerAdapterItemsImg(Context context, List<String> Urls, boolean isFromDb) {
            mContext = context;
            this.Urls = Urls;
            this.isFromDb = isFromDb;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.layout_img_item, collection, false);
            SqaureImageView imageView = layout.findViewById(R.id.image) ;
            if(isFromDb){
                if(Urls.get(position).contains("https://firebasestorage.googleapis.com")){ // when add new image,
                    UniversalImageLoader.setImage(Urls.get(position), imageView, null ,"");
                }else {
                    setImage(Urls.get(position) , imageView , mAppend);
                }
            }else {
                setImage(Urls.get(position) , imageView , mAppend);
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
            return Urls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return Urls.get(position);
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
