package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.util.Log;

import com.entage.nrd.entage.R;
import java.util.HashMap;

public class CategoriesItemList {
    private static final String TAG = "CategoriesItemList";

    private static boolean init = false;
    static public String[] categories1 ;
    static public String[] categories_keys ;

    private static  HashMap<String, String> categoriesId = new HashMap<>();
    private static HashMap<String, String> categoriesName = new HashMap<>();

    // main cat
    private static  String[] categories_array_name, categories_array_code ;
    private static  HashMap<String, String> categories_name, categories_code, categories_algolia_code, categories_parent;

    public static void init(Context context) {
        if(!init){

            // main cat
            setupCategories(context.getResources().getStringArray(R.array.categories_arabic),
                    context.getResources().getStringArray(R.array.categories_code));

            categories_algolia_code = new HashMap<>();
            categories_parent = new HashMap<>();

            init = true;
        }
    }

    // main cat
    private static void setupCategories(String[] cat_name, String[] cat_code){
        Log.d(TAG, "setupCategories: " + cat_name.length+" / "+ cat_code.length);
        categories_array_name = cat_name;
        categories_array_code = cat_code;
        categories_name = new HashMap<>();
        categories_code = new HashMap<>();
        if(cat_name.length == cat_code.length){
            for( int i=0 ; i<cat_code.length; i++){
                categories_name.put(categories_array_code[i], categories_array_name[i]);
                categories_code.put(categories_array_name[i], categories_array_code[i]);
            }
        }
    }

    public static String[] getCategories_array_name() {
        return categories_array_name;
    }

    public static String[] getCategories_array_code() {
        return categories_array_code;
    }

    public static String getCategories_name(String code) {
        if(categories_name == null){
            return "";
        }
        return categories_name.get(code);
    }

    public static String getCategories_code(String name) {
        if(categories_name == null){
            return "";
        }
        return categories_code.get(name);
    }

    public static HashMap<String, String> getCategories_algolia_codes() {
        return categories_algolia_code;
    }

    public static HashMap<String, String> getCategories_parent() {
        return categories_parent;
    }

    /////////--------------------------------------------------------------
    public static void setCategories(String[] _categories) {
        if(categories1  == null){
            CategoriesItemList.categories1 = _categories;
        }
    }

    public static void setCategories_keys(String[] _categories_keys) {
        if(categories_keys  == null){
            CategoriesItemList.categories_keys = _categories_keys;
        }
    }

    static public String getCategorieById(String id){
        if(categoriesId.size() == 0){

        }

        return categoriesId.get(id);
    }

    static public String getCategorieByName(String name){
        return categoriesName.get(name);
    }

    public static String[] getCategories1() {
        return categories1;
    }

}
