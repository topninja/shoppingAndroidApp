package com.entage.nrd.entage.utilities_1;

import android.content.Context;

import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ConversationMessages {

    private static boolean init = false;

    //private static  HashMap<String, String> messagesId = new HashMap<>();
    private static HashMap<String, String> messages;
    private static ArrayList<String> msgIds;
    private static ArrayList<String> titles;
    private static ArrayList<String> msgIds_entagePage;
    private static ArrayList<String> msgIds_user;

    private static final String cm_welcome_id = "cm_w_";

    private static final String cm_shipping_vendor_id = "cm_s_v_";
    private static final String cm_shipping_user_id = "cm_s_u_";

    private static final String cm_location_vendor_id = "cm_l_v_";
    private static final String cm_location_user_id = "cm_l_u_";

    private static final String cm_edit_vendor_id = "cm_e_v_";
    private static final String cm_edit_user_id = "cm_e_u_";

    private static final String cm_id = "cm_";

    public static void init(Context context) {
        if(!init){
            messages = new HashMap<>();
            msgIds = new ArrayList<>();
            msgIds_entagePage = new ArrayList<>();
            msgIds_user = new ArrayList<>();

            titles = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.conversation_messages_titles)));
            int i =0;

            messages.put(titles.get(i), titles.get(i));
            msgIds.add(titles.get(i)); msgIds_entagePage.add(titles.get(i)); msgIds_user.add(titles.get(i)); i++;// -------
            String[] cm_welcome = context.getResources().getStringArray(R.array.conversation_messages_welcome);
            setup(cm_welcome, cm_welcome_id, msgIds_entagePage, msgIds_user);

            messages.put(titles.get(i), titles.get(i));
            msgIds.add(titles.get(i)); msgIds_entagePage.add(titles.get(i)); msgIds_user.add(titles.get(i)); i++;// -------
            String[] cm_shipping_vendor = context.getResources().getStringArray(R.array.conversation_messages_shipping_vendor);
            setup(cm_shipping_vendor, cm_shipping_vendor_id, msgIds_entagePage, null);
            String[] cm_shipping_user = context.getResources().getStringArray(R.array.conversation_messages_shipping_user);
            setup(cm_shipping_user, cm_shipping_user_id, msgIds_user, null);

            messages.put(titles.get(i), titles.get(i));
            msgIds.add(titles.get(i)); msgIds_entagePage.add(titles.get(i)); msgIds_user.add(titles.get(i)); i++;// -------
            String[] cm_location_vendor = context.getResources().getStringArray(R.array.conversation_messages_location_vendor);
            setup(cm_location_vendor, cm_location_vendor_id, msgIds_entagePage, null);
            String[] cm_location_user = context.getResources().getStringArray(R.array.conversation_messages_location_user);
            setup(cm_location_user, cm_location_user_id, msgIds_user, null);

            messages.put(titles.get(i), titles.get(i));
            msgIds.add(titles.get(i)); msgIds_entagePage.add(titles.get(i)); msgIds_user.add(titles.get(i)); i++;// -------
            String[] cm_edit_vendor = context.getResources().getStringArray(R.array.conversation_messages_edit_vendor);
            setup(cm_edit_vendor, cm_edit_vendor_id, msgIds_entagePage, null);
            String[] cm_edit_user = context.getResources().getStringArray(R.array.conversation_messages_edit_user);
            setup(cm_edit_user, cm_edit_user_id, msgIds_user, null);

            messages.put(titles.get(i), titles.get(i));
            msgIds.add(titles.get(i)); msgIds_entagePage.add(titles.get(i)); msgIds_user.add(titles.get(i));// -------
            String[] cm = context.getResources().getStringArray(R.array.conversation_messages);
            setup(cm, cm_id, msgIds_entagePage, msgIds_user);

            //
            messages.put("order_confirmed", context.getString(R.string.order_confirmed));
            messages.put("payment_claiming", context.getString(R.string.payment_claim));
            messages.put("refuse_to_pay", context.getString(R.string.refuse_to_pay));
            messages.put("pay_succeed", context.getString(R.string.notif_title_order_payment_succeed_text));
            messages.put("customer_order_completed", context.getString(R.string.customer_order_completed));
            messages.put("entage_page_order_completed", context.getString(R.string.entage_page_order_completed));

            messages.put("edit_data_order", context.getString(R.string.edit_data_order));
            messages.put("admin_canceled_order", context.getString(R.string.admin_canceled_order));
            messages.put("entage_page_canceled_order", context.getString(R.string.entage_page_canceled_order));
            messages.put("customer_canceled_order", context.getString(R.string.customer_canceled_order));
            messages.put("refuse_to_edit_data_order", context.getString(R.string.refuse_to_edit_data_order));
            messages.put("agree_to_edit_data_order", context.getString(R.string.agree_to_edit_data_order));

            messages.put("my_address", context.getString(R.string.my_address_title));
            messages.put("receiving_order_address", context.getString(R.string.location_address_receiving_order));

            init = true;
        }
    }

    private static void setup(String[] msg, String _id, ArrayList<String> arrayList1, ArrayList<String> arrayList2){
        for(int i = 0; i< msg.length; i++){
            String id = _id+i;
            messages.put(id, msg[i]);
            msgIds.add(id);
            if(arrayList1!=null){
                arrayList1.add(id);
            }
            if(arrayList2!=null){
                arrayList2.add(id);
            }
        }
    }

    public static String getMessageText(String messagesId) {
        if(titles.contains(messagesId)){
            return "";
        }
        else if(messages.containsKey(messagesId)){
            return messages.get(messagesId);
        }else {
            return "";
        }
    }

    public static ArrayList<String> getMsgIds(){
        return  msgIds;
    }

    public static ArrayList<String> getMsgIds_entagePage() {
        return msgIds_entagePage;
    }

    public static ArrayList<String> getMsgIds_user() {
        return msgIds_user;
    }

    private void getCm_welcome(){

    }



}
