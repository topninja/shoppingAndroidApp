package com.entage.nrd.entage.utilities_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ModelObject {

    private HashMap<String, Integer> layouts ;
    private ArrayList<Integer> layoutResId;
    private ArrayList<String> layoutResTitle;

    private boolean isOpposite ;

    public ModelObject(ArrayList<Integer> layoutResId, boolean isOpposite) {
        this.layoutResId = layoutResId;
        this.isOpposite = isOpposite ;
        layouts = new HashMap<>();
        layoutResTitle = new ArrayList<>();

        if(isOpposite){
            Collections.reverse(layoutResId);
        }
        for (int i=0; i<layoutResId.size(); i++){
            String key = String.valueOf(i);
            layoutResTitle.add(key);
            layouts.put(key, layoutResId.get(i));
        }

    }

    public ModelObject(ArrayList<Integer> _layoutResId, ArrayList<String> _layoutResTitle, boolean isOpposite) {
        this.layoutResId = _layoutResId;
        this.isOpposite = isOpposite ;
        layouts = new HashMap<>();
        this.layoutResTitle = _layoutResTitle;

        if(isOpposite){
            //Collections.reverse(layoutResId);
            //Collections.reverse(layoutResTitle);
        }
        for (int i=0; i<layoutResId.size(); i++){
            layouts.put(layoutResTitle.get(i), layoutResId.get(i));
        }
    }

    public int getLayoutResId(int position) {
        return layoutResId.get(position);
    }

    public String getTitleResId(int position) {
        return layoutResTitle.get(position);
    }

    public int getLength(){
        return layouts.size();
    }

    public void setOpposite(boolean b) {
        this.isOpposite = b ;
    }


    public boolean isOpposite() {
        return isOpposite;
    }


}
