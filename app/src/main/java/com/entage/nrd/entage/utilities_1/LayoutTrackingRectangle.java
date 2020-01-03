package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.entage.nrd.entage.R;

public class LayoutTrackingRectangle extends LinearLayout {
    private static final String TAG = "LayoutTrackingCircles";

    LayoutInflater mInflater;
    private View view;

    private LinearLayout linearLayout;
    private ImageView[] circles;
    private int numberOfCircles;

    private float scaleFocus = 1;
    private float scaleNotFocus = 0.8f;
    private int focusAt = 0;
    private int colorFocus;
    private int colorNotFocus;
    private int numberOfSelectedCircles;

    public LayoutTrackingRectangle(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();

    }

    public LayoutTrackingRectangle(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public LayoutTrackingRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init()
    {
        view = mInflater.inflate(R.layout.layout_tracking_rectangle, this, true);
        linearLayout = (LinearLayout) view.findViewById(R.id.linLayout);
        numberOfCircles = linearLayout.getChildCount();
        circles = new ImageView[numberOfCircles];
        for (int i=0; i<numberOfCircles; i++){
            circles[i] = (ImageView) linearLayout.getChildAt(i);
            circles[i].setVisibility(View.GONE);
        }
    }

    public void setNumberCircles(int num){
        numberOfSelectedCircles = num;
        for (int i=0; i<num&&i<numberOfCircles; i++){
            circles[i].setVisibility(View.VISIBLE);
        }
    }

    public void setFocusAt(int index){
        focusAt = index;
        circles[index].setScaleX(scaleFocus);
        circles[index].setScaleY(scaleFocus);
        circles[index].setColorFilter(colorFocus, PorterDuff.Mode.SRC_ATOP);
    }

    private void releaseFocusAt(int index){
        circles[index].setScaleX(scaleNotFocus);
        circles[index].setScaleY(scaleNotFocus);
        circles[index].setColorFilter(colorNotFocus, PorterDuff.Mode.SRC_ATOP);
    }

    public boolean next(){
        if (focusAt < (numberOfSelectedCircles-1)){
            releaseFocusAt(focusAt);

            focusAt = focusAt+1;
            setFocusAt(focusAt);

            return  true;
        }else {
            return false;
        }
    }

    public boolean previous(){
        if (focusAt > 0){
            releaseFocusAt(focusAt);

            focusAt = focusAt-1;
            setFocusAt(focusAt);

            return  true;
        }else {
            return false;
        }
    }

    public void setColors(int colorFocus, int colorNotFocus) {
        this.colorFocus = colorFocus;
        this.colorNotFocus = colorNotFocus;
    }

    public boolean isLast() {
        if((focusAt+1) ==  numberOfSelectedCircles){
            return true;
        }else {
            return false;
        }
    }

    public void setFocusAtAndRelease(int position) {
        releaseFocusAt(focusAt);
        setFocusAt(position);
        focusAt = position;
    }
}
