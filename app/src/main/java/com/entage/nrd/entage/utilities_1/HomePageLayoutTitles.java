package com.entage.nrd.entage.utilities_1;

import android.content.Context;

import com.entage.nrd.entage.R;

import java.util.HashMap;

public class HomePageLayoutTitles {

    private static boolean init = false;

    private static HashMap<String, String> titles;

    public static void init(Context context) {
        if(!init){
            titles = new HashMap<>();

            titles.put(context.getString(R.string.layout_special_item), context.getString(R.string.special_item));
            titles.put(context.getString(R.string.layout_active_section), context.getString(R.string.active_section));

            titles.put(context.getString(R.string.layout_last_user_see), context.getString(R.string.last_user_see));
            titles.put(context.getString(R.string.layout_special_entage_pages), context.getString(R.string.special_entage_pages));
            titles.put(context.getString(R.string.layout_items_most_see), context.getString(R.string.items_most_see));
            titles.put(context.getString(R.string.layout_active_store), context.getString(R.string.active_store));

            init = true;
        }
    }

    public static String getTitle(String layout_title) {
        if(layout_title!=null && titles.containsKey(layout_title) && init){
            return titles.get(layout_title);
        }else {
            return layout_title;
        }
    }

}
