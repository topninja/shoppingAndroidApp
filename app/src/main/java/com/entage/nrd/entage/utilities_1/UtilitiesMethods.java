package com.entage.nrd.entage.utilities_1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UtilitiesMethods {
    private static final String TAG = "UtilitiesMethods";

    public static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    public static void focusOnViewHorizontalScrollView(final ScrollView scroll, final View view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int vLeft = view.getLeft();
                int vRight = view.getRight();
                int sWidth = scroll.getWidth();
                scroll.smoothScrollTo(((vLeft + vRight - sWidth) / 2), 0 );
            }
        });
    }

    public static void focusOnViewVerticalScrollView(final ScrollView scroll, final View view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int vTop = view.getTop();
                int vBottom = view.getBottom();
                int sHeight = scroll.getHeight();
                scroll.smoothScrollTo(0, ((vTop + vBottom - sHeight) / 2) );
            }
        });
    }

    public static void focusOnViewHorizontalScrollView(final NestedScrollView scroll, final View view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int vLeft = view.getLeft();
                int vRight = view.getRight();
                int sWidth = scroll.getWidth();
                scroll.smoothScrollTo(((vLeft + vRight - sWidth) / 2), 0 );
            }
        });
    }

    public static void focusOnViewVerticalScrollView(final NestedScrollView scroll, final View view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int vTop = view.getTop();
                int vBottom = view.getBottom();
                int sHeight = scroll.getHeight();
                scroll.smoothScrollTo(0, ((vTop + vBottom - sHeight) / 2) );
            }
        });
    }


    public static void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }else {
                    v.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
                }
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void expand(final View v, final int targetHeight, final float from) {
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }else if(interpolatedTime > from){
                    v.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
                }
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v, final float upTo) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    //v.setVisibility(View.GONE);
                    cancel();
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime * upTo);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static boolean checkPermissions(Context mContext){
        boolean boo = false;
        if(Permissions.checkPermissionsArray(Permissions.PERMISSIONS,  mContext)){
            boo = true;
        }
        else{
            Permissions.verifyPermissions(Permissions.PERMISSIONS, mContext);
        }

        return boo;
    }

    public static boolean checkPermissionsLocations(Context mContext){
        boolean boo = false;
        if(Permissions.checkPermissionsArray(Permissions.PERMISSIONS_LOCATIONS,  mContext)){
            boo = true;
        }
        else{
            Permissions.verifyPermissions(Permissions.PERMISSIONS_LOCATIONS, mContext);
        }

        return boo;
    }

    public static CategorieWithChildren getCategorieFromPath(CategorieWithChildren main, String path){
        CategorieWithChildren categorie = main;

        ArrayList<String> arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(path);
        String ourCategorie = arrayList.get(arrayList.size()-1);
        int x=0;
        do {
            categorie = categorie.getCategorieWithChildren().get(arrayList.get(x));
            x++;
        }while (!categorie.getCategorieCode().equals(ourCategorie));

        return categorie;
    }

    public static CategorieWithChildren getCategorieFromPath(CategorieWithChildren main, ArrayList<String> arrayList){
        CategorieWithChildren categorie = main;

        String ourCategorie = arrayList.get(arrayList.size()-1);
        int x=0;
        do {
            categorie = categorie.getCategorieWithChildren().get(arrayList.get(x));
            if(categorie == null){ // if ourCategorie is child only
                return null;
            }
            x++;
        }while (!categorie.getCategorieCode().equals(ourCategorie));

        return categorie;
    }

    public static void rotateAnimation(ImageView imageView){
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        imageView.startAnimation(rotateAnimation);
    }

    public static void animationFill(final View view, final int duration, final float from, final float to, boolean isIncrease ,
                               boolean animate) {
        AnimatorSet animator = new AnimatorSet();
        view.setScaleX(from);
        view.setScaleY(from);

        final float[] _to = new float[1];
        if(animate) {
            if(isIncrease){
                _to[0] = to+0.3f;
            }else {
                _to[0] = from+0.3f;
            }
        }else {
            _to[0] = to;
        }

        ObjectAnimator scaleDownX_5 = ObjectAnimator.ofFloat(view, "scaleX", from, _to[0]);
        scaleDownX_5.setDuration(duration);
        scaleDownX_5.setInterpolator(DECELERATE_INTERPOLATOR);
        ObjectAnimator scaleDownY_5 = ObjectAnimator.ofFloat(view, "scaleY", from,  _to[0]);
        scaleDownY_5.setDuration(duration);
        scaleDownY_5.setInterpolator(DECELERATE_INTERPOLATOR);

        if(animate) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationFill(view, duration - (duration/2),  _to[0], to, false,false);
                }
            });
        }

        animator.playTogether(scaleDownY_5, scaleDownX_5);
        animator.start();
    }

    public static String getTimeDiff(Context context, String time){
        String string = context.getString(R.string.since) +" ";

        // get diff Day
        String diffDay = String.
                valueOf(DateTime.getDifferenceDays(DateTime.getDateToday(),DateTime.getTimestamp(time)));
        if(diffDay.equals("0")){

            // get diff Hours
            String diffHou = String.
                    valueOf(DateTime.getDifferenceHours(DateTime.getDateToday(),DateTime.getTimestamp(time)));
            if(diffHou.equals("0")){

                // get diff Minutes
                String diffMin = String.
                        valueOf(DateTime.getDifferenceMinutes(DateTime.getDateToday(),DateTime.getTimestamp(time)));
                if(diffMin.equals("0")){

                    // get diff Seconds
                    String diffSec = String.
                            valueOf(DateTime.getDifferenceSeconds(DateTime.getDateToday(),DateTime.getTimestamp(time)));
                    string+= diffSec +" "+ context.getString(R.string.seconds);
                }else {
                    string+= diffMin +" "+ context.getString(R.string.minutes);
                }
            }else {
                string+=  diffHou +" "+ context.getString(R.string.hours);
            }
        }else {
            string+= diffDay +" "+ context.getString(R.string.days);
        }

        return string;
    }

    public static String getMessage(Context context, String msg_key, String extra_data){
        String string = msg_key;
        if(msg_key == null){
            return "";
        }
        else if(msg_key.equals(context.getString(R.string.notif_title_add_new_question))){
            return context.getString(R.string.notif_title_add_new_question_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_add_item_to_basket))){
            return context.getString(R.string.notif_title_add_item_to_basket_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_answered_for_question))){
            return context.getString(R.string.notif_title_answered_for_question_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_new_message))){
            return context.getString(R.string.notif_title_new_message_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_confirm_order))){
            return context.getString(R.string.notif_title_confirm_order_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_cancelled_order))){
            return context.getString(R.string.notif_title_cancelled_order_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_welcome_entaji_page))){
            return context.getString(R.string.notif_title_welcome_entaji_page_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_wish_best))){
            return context.getString(R.string.notif_title_wish_best_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_add_new_item))){
            return extra_data + " " + context.getString(R.string.notif_title_add_new_item_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_app_new_update))){
            return context.getString(R.string.notif_title_app_new_update_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_entaji_app))){
            return context.getString(R.string.notif_title_entaji_app_text);

        }
        else if(msg_key.equals(context.getString(R.string.notif_title_payment_clim))){
            return context.getString(R.string.store)+ " " + extra_data + " " +
                    context.getString(R.string.notif_title_payment_clim_text);
        }
        else if(msg_key.equals(context.getString(R.string.notif_title_order_payment_succeed))){
            return context.getString(R.string.notif_title_order_payment_succeed_text);
        }
        else {
            return string;
        }
    }

    public static void setupOptionsPrices(Context mContext, OptionsPrices optionsPrices, int textSizePrice, RelativeLayout priceLayout,
                                           GlobalVariable globalVariable){
        new ViewOptionsPrices(mContext, optionsPrices, priceLayout, textSizePrice, 0, globalVariable);
    }

    public static ArrayList<String> getCategoriesNames(ArrayList<String> categories, ArrayList<String> categories_item_code,
                                                       HashMap<String, ArrayList<String>> categories_paths){
        ArrayList<String> categories_text = new ArrayList<>();
        for(String code : categories){
            ArrayList<String> list = StringManipulation.convertPrintedArrayListToArrayListObject(code);
            for(int i=0; i<list.size(); i++){
                if(categories_paths != null){
                    categories_paths.put(list.get(i), new ArrayList<>(list.subList(0,i+1)) );
                }

                String string = CategoriesItemList.getCategories_name(list.get(i));
                if(!categories_text.contains(string)){
                    if(categories_item_code != null){
                        categories_item_code.add(list.get(i));
                    }
                    categories_text.add(string);
                }
            }
        }
        return categories_text;
    }

    private static NotificationManager notificationManager;

    public static void removeNotification(Context mContext, String notifyTag){
        if(notificationManager == null){
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        notificationManager.cancel(notifyTag, MyFirebaseMessagingService.NOTIFY_ID);

        removeNotificationGroup(mContext);
    }

    public static void removeNotificationGroup(Context mContext) {
        if(notificationManager == null){
            notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        StatusBarNotification[] statusBarNotification = notificationManager.getActiveNotifications();
        int countGroup_1 = 0;
        int countGroup_2 = 0;
        for(StatusBarNotification notification : statusBarNotification){
            String key = notification.getGroupKey();
            if(key != null){
                if(key.contains(MyFirebaseMessagingService.GROUP_KEY_1)){
                    countGroup_1++;
                }else {
                    countGroup_2++;
                }
            }
        }
        //Log.d(TAG, "removeNotificationGroup: GROUP_KEY_1: "+ countGroup_1 + ", GROUP_KEY_2: "+countGroup_2);
        if(countGroup_1 == 1){
            notificationManager.cancel(MyFirebaseMessagingService.GROUP_KEY_1, MyFirebaseMessagingService.NOTIFY_ID);
        }
        if(countGroup_2 == 1){
            notificationManager.cancel(MyFirebaseMessagingService.GROUP_KEY_2, MyFirebaseMessagingService.NOTIFY_ID);
        }
    }

    public static void setMessageRead(Context mContext, String notifyTag){

    }

    public static Map<Integer, String> getHeadingTypeface() {
        Map<Integer, String> typefaceMap = new HashMap<>();
        typefaceMap.put(Typeface.NORMAL, "fonts/GreycliffCF-Bold.ttf");
        typefaceMap.put(Typeface.BOLD, "fonts/GreycliffCF-Heavy.ttf");
        typefaceMap.put(Typeface.ITALIC, "fonts/GreycliffCF-Heavy.ttf");
        typefaceMap.put(Typeface.BOLD_ITALIC, "fonts/GreycliffCF-Bold.ttf");
        return typefaceMap;
    }

    public static Map<Integer, String> getContentface() {
        Map<Integer, String> typefaceMap = new HashMap<>();
        typefaceMap.put(Typeface.NORMAL, "fonts/Lato-Medium.ttf");
        typefaceMap.put(Typeface.BOLD, "fonts/Lato-Bold.ttf");
        typefaceMap.put(Typeface.ITALIC, "fonts/Lato-MediumItalic.ttf");
        typefaceMap.put(Typeface.BOLD_ITALIC, "fonts/Lato-BoldItalic.ttf");
        return typefaceMap;
    }

}
