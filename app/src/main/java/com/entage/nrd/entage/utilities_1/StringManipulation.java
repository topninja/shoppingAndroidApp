package com.entage.nrd.entage.utilities_1;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class StringManipulation {

    public static String expandUsername(String username){
        return username.replace("."," ");
    }

    public static String condenseUsername(String username){
        return username.replace(" ",".");
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static String getTags(String string){
        if (string.indexOf("#") > 0){
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;
            for (char c : charArray){
             if(c == '#'){
                 foundWord = true;
                 sb.append(c);
             }else {
                 if(foundWord){
                     sb.append(c);
                 }
             }
             if(c == ' '){
                 foundWord = false;
             }
            }
            String s = sb.toString().replace(" ", "").replace("#",",#");
            return s.substring(1, s.length());
        }
        return string;
    }

    public static String removeStars(String string){
        string = string.replace("*","");
        return string;
    }

    public static String removeLastSpace(String text){
        if(text.length()>0){
            if(text.charAt(text.length()-1)== ' '){
                text = text.substring(0,text.length()-1);
                return removeLastSpace(text);
            }else {
                return text;
            }
        }else {
            return text;
        }
    }

    public static String ConvertLanguageNumber (String d) {
        /*DecimalFormat formatter = new DecimalFormat("##"); //
        //formatter.setRoundingMode(RoundingMode.DOWN); // Towards zero
        return formatter.format(Integer.parseInt(d));*/

        return d;
    }

    public static String firstTwoNumbers (double d) {
        DecimalFormat formatter = new DecimalFormat("##.##"); //
        formatter.setRoundingMode(RoundingMode.DOWN); // Towards zero
        return formatter.format(d);
    }

    public static ArrayList<String> convertPrintedArrayListToArrayListObject(String printedArrayList){
        // trim enclosing brackets and then split by comma and space
        String[] array = printedArrayList.substring(1, printedArrayList.length() - 1).split(", ");

        return new ArrayList<>(Arrays.asList(array));
    }

    public static ArrayList<String> getCategoriesForAlgolia(ArrayList<String> categoriesFromDb) {
        ArrayList<String> categoriesForAlgolia = new ArrayList<>();

        for(String string : categoriesFromDb){
            ArrayList<String> converted = convertPrintedArrayListToArrayListObject(string);
            int len = converted.size();
            for(int i=0; i<len; i++){
                converted.add(converted.get(0).replace("ca_su_",""));
                converted.remove(0);
            }

            String alg = converted.get(0) ;
            for(int i=1; i<len; i++){
                alg += converted.get(i);
            }
            categoriesForAlgolia.add(alg);
        }

        return categoriesForAlgolia;
    }

    public static String listToString(ArrayList<String> list, String divider) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(divider).append(list.get(i));
        }
        return result.toString();
    }

    public static String printArrayListItemsOrder(ArrayList<String> arrayList){
        if(arrayList != null && arrayList.size()>0){
            StringBuilder text = new StringBuilder("(");
            for(String s : arrayList){
                text.append(s).append(", ");
            }
            text = new StringBuilder(text.substring(0, text.length() - 2));
            text.append(")");

            return text.toString();
        }else {
            return"";
        }
    }

    public static String replaceSomeCharsToSpace(String string){
        string = string.replace("\n","");
        string = string.replace("_"," ");
        string = string.replace("#"," ");
        string = string.replace("'\'"," ");
        string = string.replace("/"," ");
        string = string.replace("."," ");

        string = string.replace("#","");
        string = string.replace("]","");
        string = string.replace("[","");
        string = string.replace("$","");
        string = string.replace(")","");
        string = string.replace("(","");
        string = string.replace("*","");
        string = string.replace("+","");
        string = string.replace("-","");
        string = string.replace("/","");
        string = string.replace("'\'","");
        string = string.replace("|","");
        string = string.replace("=","");
        string = string.replace("-","");
        string = string.replace("&","");
        string = string.replace("^","");
        string = string.replace("@","");
        string = string.replace("!","");
        string = string.replace("`","");
        string = string.replace("?","");
        string = string.replace("}","");
        string = string.replace("{","");
        string = string.replace(">","");
        string = string.replace("<","");
        string = string.replace(";","");
        string = string.replace(":","");
        return string;
    }
}
