package com.entage.nrd.entage.utilities_1;

import java.util.HashMap;

public class SpecificationsItemList {

    static public String[] specifications ;
    static public String[] specifications_keys ;

    static public HashMap<String, String> specificationsId = new HashMap<>();
    static public HashMap<String, String> specificationsName = new HashMap<>();

    private static void init() {
        for( int i=0 ; i<specifications.length; i++){
            specificationsId.put(specifications_keys[i],specifications[i]);
            specificationsName.put(specifications[i], specifications_keys[i]);
        }
    }

    public static void setSpecifications(String[] _categories) {
        if(specifications  == null){
            SpecificationsItemList.specifications = _categories;
        }
    }

    public static void setSpecifications_keys(String[] _categories_keys) {
        if(specifications_keys  == null){
            SpecificationsItemList.specifications_keys = _categories_keys;
        }
    }

    static public String getSpecificationsById(String id){
        if(specificationsId.size() == 0){
            init();
        }

        return specificationsId.get(id);
    }

    static public String getSpecificationsByName(String name){
        if(specificationsName.size() == 0){
            init();
        }

        return specificationsName.get(name);
    }

    public static String[] getSpecifications() {
        return specifications;
    }


}
