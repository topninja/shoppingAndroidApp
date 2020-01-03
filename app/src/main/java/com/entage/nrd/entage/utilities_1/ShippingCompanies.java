package com.entage.nrd.entage.utilities_1;

import android.content.Context;

import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShippingCompanies {

    private static boolean init = false;

    //private static  HashMap<String, String> messagesId = new HashMap<>();
    private static HashMap<String, String> companies;
    private static HashMap<String, String> links;


    public static void init(Context context) {
        if(!init){
            companies = new HashMap<>();
            links = new HashMap<>();

            ArrayList<String> titles = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.shipping_companies)));
             /* 0- <item>announced_later</item>
                1- <item>personal_shipping</item>
                2- <item>aramex</item>
                3- <item>smsa</item>
                4- <item>dhl</item>
                5- <item>fedex</item>
                6- <item>ups</item>
                7- <item>alma</item>
                8- <item>zajil</item>
                9- <item>other</item>*/

             int i=0;
            companies.put(titles.get(i), context.getString(R.string.shipping_company_later));
            links.put(titles.get(i++), "");

            companies.put(titles.get(i), context.getString(R.string.personal_shipping));
            links.put(titles.get(i++), "");

            companies.put(titles.get(i), context.getString(R.string.aramex));
            links.put(titles.get(i++), "https://www.aramex.com/ar/ship/ship-check-shipping-rates");

            companies.put(titles.get(i), context.getString(R.string.smsa));
            links.put(titles.get(i++), "http://www.smsaexpress.com/arabic/index.html");

            companies.put(titles.get(i), context.getString(R.string.dhl));
            links.put(titles.get(i++), "https://www.dhl.com.sa/en/contact_center/contact_express.html");

            companies.put(titles.get(i), context.getString(R.string.fedex));
            links.put(titles.get(i++), "http://www.fedex.com/ae_arabic/newfedex/welcome.html");

            companies.put(titles.get(i), context.getString(R.string.ups));
            links.put(titles.get(i++), "https://www.ups.com/sa/en/Home.page");

            companies.put(titles.get(i), context.getString(R.string.alma));
            links.put(titles.get(i++), "http://www.almaexpress.com/ar/");

            companies.put(titles.get(i), context.getString(R.string.zajil));
            links.put(titles.get(i++), "https://zajil-express.com/");

            companies.put(titles.get(i), context.getString(R.string.other_shipping_company));
            links.put(titles.get(i++), "");

            init = true;
        }
    }

    public static ArrayList<String> getCompaniesList(){
        ArrayList<String> arrayList = new ArrayList<>(companies.values());
        return arrayList;
    }

    public static String getCompanyLink(String company){
        return links.get(company);
    }

    public static String getCompany(String text){
        for(Map.Entry<String, String> map : companies.entrySet()){
            if(text.equals(map.getValue())){
                return map.getKey();
            }
        }
        return "";
    }

    public static String getCompanyShowText(String text){
        return companies.get(text);
    }


}
