package com.entage.nrd.entage.Models;

import com.entage.nrd.entage.utilities_1.CategoriesItemList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class CategorieWithChildren {
    private static final String TAG = "CategorieWithChildren";

    private String categorieCode;
    private ArrayList<String> children;
    private ArrayList<String> myPath;

    private HashMap<String, CategorieWithChildren> CategorieWithChildren;
    private HashMap<String, ArrayList<String>> paths;
    private CategorieWithChildren parent;

    private String pathAlgolia;
    private HashMap<String, String> pathOfChildrenForAlgolia;
    private boolean select;


    public CategorieWithChildren(String categorieCode, CategorieWithChildren parent) {
        this.categorieCode = categorieCode;
        this.parent = parent;
        CategoriesItemList.getCategories_parent().put(categorieCode, parent.getCategorieCode());

        children = new ArrayList<>();
        CategorieWithChildren = new HashMap<>();
        paths = new HashMap<>();
    }

    public CategorieWithChildren() {
        children = new ArrayList<>();
        CategorieWithChildren = new HashMap<>();
    }

    public String getCategorieCode() {
        return categorieCode;
    }

    public void setCategorieCode(String categorieCode) {
        this.categorieCode = categorieCode;
        CategoriesItemList.getCategories_parent().put(categorieCode, parent.getCategorieCode());
    }

    public ArrayList<String> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<String> children) {
        this.children = children;
        setPathOfChildrenForAlgolia();
    }

    public HashMap<String, com.entage.nrd.entage.Models.CategorieWithChildren> getCategorieWithChildren() {
        return CategorieWithChildren;
    }

    public void setCategorieWithChildren(HashMap<String, com.entage.nrd.entage.Models.CategorieWithChildren> categorieWithChildren) {
        CategorieWithChildren = categorieWithChildren;
    }

    public com.entage.nrd.entage.Models.CategorieWithChildren getParent() {
        return parent;
    }

    public void setParent(com.entage.nrd.entage.Models.CategorieWithChildren parent) {
        this.parent = parent;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public HashMap<String, ArrayList<String>> getPaths() {
        return paths;
    }

    public void setPaths(HashMap<String, ArrayList<String>> paths) {
        this.paths = paths;
    }

    public void addPath(String code) {
        if(!paths.containsKey(code)){
            paths.put(code, new ArrayList<String>());
        }
        this.paths = paths;
    }

    public ArrayList<String> getMyPath() {
        myPath = new ArrayList<>();
        myPath.add(categorieCode);
        CategorieWithChildren categorie = parent;
        while (categorie.getCategorieCode() != null){
            myPath.add(categorie.getCategorieCode());
            categorie = categorie.getParent();
        }
        Collections.reverse(myPath);
        return myPath;
    }

    public void setMyPath(ArrayList<String> myPath) {
        this.myPath = myPath;
    }

    public ArrayList<String> getPathOfChild(String code) {
        ArrayList<String> path = getMyPath();
        path.add(code);

        return path;
    }

    public String getPathAlgolia() {
        return pathAlgolia;
    }

    public String getPathOfChilForAlgolia(String child) {
        return pathOfChildrenForAlgolia.get(child);
    }

    private void setPathOfChildrenForAlgolia() {
        pathOfChildrenForAlgolia = new HashMap<>();

        if(parent != null){
            if(parent.getCategorieCode() != null){
                pathAlgolia = parent.getPathOfChilForAlgolia(categorieCode);
            }else {
                pathAlgolia = categorieCode.replace("ca_su_","");
            }
        }
        CategoriesItemList.getCategories_algolia_codes().put(pathAlgolia, categorieCode);

        for(String s : children) {
            String code = pathAlgolia + s.replace("ca_su_", "");
            pathOfChildrenForAlgolia.put(s, code);
            CategoriesItemList.getCategories_algolia_codes().put(code, s);
        }
    }

    @Override
    public String toString() {
        return "CategorieWithChildren{" +
                "parentCode='" + categorieCode + '\'' +
                ", children=" + children +
                ", CategorieWithChildren=" + CategorieWithChildren +
                '}';
    }
}
