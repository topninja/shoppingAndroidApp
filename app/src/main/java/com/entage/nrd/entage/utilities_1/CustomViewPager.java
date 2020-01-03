package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    private static final String TAG = "CustomViewPagerSwapping";

    private boolean swiping = true;
    private boolean isOpposite ;

    private OnClickListener mOnClickListener;

    public CustomViewPager(Context context) {
        super(context);

        setup();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setup();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
        /*if(swiping) {
            return super.onInterceptTouchEvent(ev);
        }else {
            return swiping;
        }*
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
        /*if(swiping) {
            return super.onTouchEvent(ev);
        }else {
            return swiping;
        }*/
    }

    public void setSwiping(boolean swiping) {
        this.swiping = swiping;
    }

    public void MoveNext() {
        //it doesn't matter if you're already in the last item
        if(isOpposite){
            this.setCurrentItem(this.getCurrentItem() - 1);
        }else {
            this.setCurrentItem(this.getCurrentItem() + 1);
        }
    }


    public void MovePrevious() {
        //it doesn't matter if you're already in the first item
        if(isOpposite){
            this.setCurrentItem(this.getCurrentItem() + 1);
        }else {
            this.setCurrentItem(this.getCurrentItem() - 1);
        }
    }

    public void setAdapter(@Nullable PagerAdapter adapter, boolean b) {
        this.isOpposite = b ;
        super.setAdapter(adapter);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h > height) height = h;
        }

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void setup() {
        final GestureDetector tapGestureDetector = new GestureDetector(getContext(), new TapGestureListener());

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tapGestureDetector.onTouchEvent(event);

                return false;
            }
        });
    }


    private class TapGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(mOnClickListener != null) {
                mOnClickListener.onViewPagerClick(CustomViewPager.this);
            }

            return true;
        }
    }

    public void setOnViewPagerClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onViewPagerClick(ViewPager viewPager);
    }

}
