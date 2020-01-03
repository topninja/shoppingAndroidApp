package com.entage.nrd.entage.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class AddingItemToAlgolia {

    String item_name ;
    String item_id ;
    String categorie_level_1 ;
    ArrayList<String> categorie_level_2;;
    String entage_page_id;

    HashMap<String,Object> item;
    int price = 0;

    public AddingItemToAlgolia(String item_name, String item_id, String categorie_level_1, ArrayList<String> categorie_level_2,
                               String entage_page_id) {
        item = new HashMap<>();
        this.item_name = item_name;
        this.item_id = item_id;
        this.categorie_level_1 = categorie_level_1;
        this.categorie_level_2 = categorie_level_2;
        this.entage_page_id = entage_page_id;
    }

    public AddingItemToAlgolia(String item_name, String item_id, String categorie_level_1, ArrayList<String> categorie_level_2,
                               String entage_page_id, String price) {
        item = new HashMap<>();
        this.item_name = item_name;
        this.item_id = item_id;
        this.categorie_level_1 = categorie_level_1;
        this.categorie_level_2 = categorie_level_2;
        this.entage_page_id = entage_page_id;
        this.price = Integer.parseInt(price);
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }


    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public void setCategorie_level_1(String categorie_level_1) {
        this.categorie_level_1 = categorie_level_1;
    }

    public void setCategorie_level_2(ArrayList<String> categorie_level_2) {
        this.categorie_level_2 = categorie_level_2;
    }

    public ArrayList<String> getCategorie_level_2() {
        return categorie_level_2;
    }

    public HashMap<String, Object> getItem() {
        if(item_name!=null){
            item.put("item_name", item_name);
        }

        if(item_id!=null){
            item.put("item_id", item_id);
        }

        if(categorie_level_1!=null){
            item.put("categorie_level_1", categorie_level_1);
        }

        if(categorie_level_2!=null){
            item.put("categorie_level_2", categorie_level_2);
        }

        if(entage_page_id!=null){
            item.put("entage_page_id", entage_page_id);
        }

        if(price != 0){
            item.put("price", price);
        }
        return item;
    }

    @Override
    public String toString() {
        return "AddingItemToAlgolia{" +
                "item_name='" + item_name + '\'' +
                ", item_id='" + item_id + '\'' +
                ", item=" + item +
                '}';
    }
}
