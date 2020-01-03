package com.entage.nrd.entage.utilities_1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class MiddleCircle {
    private static final String TAG = "MiddleCircle";

    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public static boolean visibilityCircle = false;

    public ImageView circle_5;
    public ImageView circle_4;
    public ImageView circle_3;

    public ImageView entage;

    private float siz_5 = 1.35f;
    private float siz_4 = 1.3f;
    private float siz_3 = 1.25f;

    private int duration_5 = 200 ;
    private int duration_4 = 400 ;
    private int duration_3 = 600 ;

    public MiddleCircle(ImageView circle_5, ImageView circle_4, ImageView circle_3, ImageView entage,
                        boolean isEntagePage, boolean animate) {
        this.circle_5 = circle_5;
        this.circle_4 = circle_4;
        this.circle_3 = circle_3;
        this.entage = entage;
        Log.d(TAG, "MiddleCircle: " + visibilityCircle);

        if(isEntagePage && !animate){
            entage.setVisibility(View.VISIBLE);
            showCircle_noAnimate();
        }else {
            if(isEntagePage){
                circle_5.setVisibility(View.GONE);
                circle_4.setVisibility(View.GONE);
                circle_3.setVisibility(View.GONE);
                entage.setVisibility(View.VISIBLE);
                visibilityCircle = true;
                showCircle();
            }else {
                entage.setVisibility(View.GONE);
                if (visibilityCircle){
                    circle_5.setVisibility(View.VISIBLE);
                    circle_4.setVisibility(View.VISIBLE);
                    circle_3.setVisibility(View.VISIBLE);
                    visibilityCircle = false;
                    hideCircle();
                }
            }
        }

    }

    public void toggleCircle(boolean isEntagePage){
        Log.d(TAG, "toggleLike: toggling heart: " + isEntagePage);

        if(isEntagePage){
            visibilityCircle = true;
            showCircle();
        }else {
            if (visibilityCircle){
                visibilityCircle = false;
                hideCircle();
            }
        }
    }

    private void hideCircle() {
        AnimatorSet animatorSet5 = new AnimatorSet();
        AnimatorSet animatorSet4 = new AnimatorSet();
        AnimatorSet animatorSet3 = new AnimatorSet();
        if(circle_5.getVisibility() == View.VISIBLE){
            Log.d(TAG, "toggleLike: toggling red heart off. ");
            circle_5.setScaleX(siz_5);
            circle_5.setScaleY(siz_5);
            circle_4.setScaleX(siz_4);
            circle_4.setScaleY(siz_4);
            circle_3.setScaleX(siz_3);
            circle_3.setScaleY(siz_3);

            ObjectAnimator scaleDownX_5 = ObjectAnimator.ofFloat(circle_5, "scaleX", siz_5, 0f);
            scaleDownX_5.setDuration(duration_5);
            scaleDownX_5.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX_4 = ObjectAnimator.ofFloat(circle_4, "scaleX", siz_4, 0f);
            scaleDownX_4.setDuration(duration_4);
            scaleDownX_4.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX_3 = ObjectAnimator.ofFloat(circle_3, "scaleX", siz_3, 0f);
            scaleDownX_3.setDuration(duration_3);
            scaleDownX_3.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownY_5 = ObjectAnimator.ofFloat(circle_5, "scaleY", siz_5, 0f);
            scaleDownY_5.setDuration(duration_5);
            scaleDownY_5.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownY_4 = ObjectAnimator.ofFloat(circle_4, "scaleY", siz_4, 0f);
            scaleDownY_4.setDuration(duration_4);
            scaleDownY_4.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownY_3 = ObjectAnimator.ofFloat(circle_3, "scaleY", siz_3, 0f);
            scaleDownY_3.setDuration(duration_3);
            scaleDownY_3.setInterpolator(DECELERATE_INTERPOLATOR);


            //circle.setVisibility(View.GONE);
            animatorSet5.playTogether(scaleDownY_5, scaleDownX_5);
            animatorSet4.playTogether(scaleDownY_4, scaleDownX_4);
            animatorSet3.playTogether(scaleDownY_3, scaleDownX_3);
        }
        animatorSet5.start();
        animatorSet4.start();
        animatorSet3.start();

        animatorSet5.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                circle_5.setVisibility(View.GONE);
                circle_4.setVisibility(View.GONE);
                circle_3.setVisibility(View.GONE);
            }
        });
    }

    private void showCircle() {
        AnimatorSet animatorSet5 = new AnimatorSet();
        AnimatorSet animatorSet4 = new AnimatorSet();
        AnimatorSet animatorSet3 = new AnimatorSet();
        if(circle_5.getVisibility() == View.GONE){
            Log.d(TAG, "toggleLike: toggling red heart on. ");
            circle_5.setScaleX(0f);
            circle_5.setScaleY(0f);
            circle_4.setScaleX(0f);
            circle_4.setScaleY(0f);
            circle_3.setScaleX(0f);
            circle_3.setScaleY(0f);

            ObjectAnimator scaleDownX_5 = ObjectAnimator.ofFloat(circle_5, "scaleX", 0f, siz_5);
            scaleDownX_5.setDuration(duration_5);
            scaleDownX_5.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX_4 = ObjectAnimator.ofFloat(circle_4, "scaleX", 0f, siz_4);
            scaleDownX_4.setDuration(duration_4);
            scaleDownX_4.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX_3 = ObjectAnimator.ofFloat(circle_3, "scaleX", 0f, siz_3);
            scaleDownX_3.setDuration(duration_3);
            scaleDownX_3.setInterpolator(DECELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownY_5 = ObjectAnimator.ofFloat(circle_5, "scaleY", 0f, siz_5);
            scaleDownY_5.setDuration(duration_5);
            scaleDownY_5.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownY_4 = ObjectAnimator.ofFloat(circle_4, "scaleY", 0f, siz_4);
            scaleDownY_4.setDuration(duration_4);
            scaleDownY_4.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownY_3 = ObjectAnimator.ofFloat(circle_3, "scaleY", 0f, siz_3);
            scaleDownY_3.setDuration(duration_3);
            scaleDownY_3.setInterpolator(DECELERATE_INTERPOLATOR);


            circle_5.setVisibility(View.VISIBLE);
            circle_4.setVisibility(View.VISIBLE);
            circle_3.setVisibility(View.VISIBLE);

            animatorSet5.playTogether(scaleDownY_5, scaleDownX_5);
            animatorSet4.playTogether(scaleDownY_4, scaleDownX_4);
            animatorSet3.playTogether(scaleDownY_3, scaleDownX_3);
        }
        animatorSet5.start();
        animatorSet4.start();
        animatorSet3.start();
    }

    private void showCircle_noAnimate() {
        circle_5.setScaleX(siz_5);
        circle_5.setScaleY(siz_5);
        circle_4.setScaleX(siz_4);
        circle_4.setScaleY(siz_4);
        circle_3.setScaleX(siz_3);
        circle_3.setScaleY(siz_3);
        circle_5.setVisibility(View.VISIBLE);
        circle_4.setVisibility(View.VISIBLE);
        circle_3.setVisibility(View.VISIBLE);
    }


}
