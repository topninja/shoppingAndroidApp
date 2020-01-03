package com.entage.nrd.entage.utilities_1;

import android.content.Context;

import com.entage.nrd.entage.R;

import java.util.HashMap;

public class DescriptionsPackages {

    private static boolean init = false;

    //private static  HashMap<String, String> messagesId = new HashMap<>();
    private static HashMap<String, String> messages;

    public static void init(Context context) {
        if(!init){
            messages = new HashMap<>();


            messages.put("ep_package_items_allow_1", context.getString(R.string.ep_package_items_allow_1));
            messages.put("ep_package_use_all_payment_methods", context.getString(R.string.ep_package_use_all_payment_methods));
            messages.put("ep_package_use_all_receipt_methods", context.getString(R.string.ep_package_use_all_receipt_methods));
            messages.put("ep_package_photo_one_item_1", context.getString(R.string.ep_package_photo_one_item_1));

            messages.put("ep_package_items_allow_2", context.getString(R.string.ep_package_items_allow_2));
            messages.put("ep_package_photo_one_item_2", context.getString(R.string.ep_package_photo_one_item_2));
            messages.put("ep_package_ad_discount_1", context.getString(R.string.ep_package_ad_discount_1));
            messages.put("ep_package_notify_followers_1", context.getString(R.string.ep_package_notify_followers_1));
            messages.put("ep_package_add_offers_1", context.getString(R.string.ep_package_add_offers_1));

            messages.put("ep_package_division_1", context.getString(R.string.ep_package_division_1));

            messages.put("ep_package_create_sharing_link_page", context.getString(R.string.ep_package_create_sharing_link_page));
            messages.put("ep_package_create_sharing_link_item", context.getString(R.string.ep_package_create_sharing_link_item));
            messages.put("ep_package_instant_chat_customers", context.getString(R.string.ep_package_instant_chat_customers));

            messages.put("ep_package_technical_support", context.getString(R.string.ep_package_technical_support));
            messages.put("ep_package_special_name", context.getString(R.string.ep_package_special_name));


            init = true;
        }
    }


    public static String getMessageText(String messagesId) {
        if(messages.containsKey(messagesId)){
            return messages.get(messagesId);
        }else {
            return messagesId;
        }
    }

}
