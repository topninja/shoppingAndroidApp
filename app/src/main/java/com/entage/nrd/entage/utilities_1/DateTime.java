package com.entage.nrd.entage.utilities_1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTime {
    private static final String TAG = "DateTime";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'" , Locale.ENGLISH);

    public DateTime() {
    }

    public static String getTimestamp(){
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    public static Date getTimestamp(String _date ){
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = sdf.parse(_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getFullTimestamp(String _date ){
        String str = "";
        SimpleDateFormat sdfSimple = new SimpleDateFormat("yyyy/MMM/dd  HH:mm a" , Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = sdf.parse(_date);
            str = sdfSimple.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static Date getTimestamp_onlyDate(String _date ){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd" , Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = sdf.parse(_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String convertToSimple(String _date){
        String str = "";
        SimpleDateFormat sdfSimple = new SimpleDateFormat("yyyy/MMM/dd " , Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = sdf.parse(_date);
            str = sdfSimple.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String convertToSimple_1(String _date){
        String str = "";
        SimpleDateFormat sdfSimple = new SimpleDateFormat("yyyy/MM/dd" , Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = sdf.parse(_date);
            str = sdfSimple.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }


    public static Date getDateToday(){
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = sdf.parse(getTimestamp());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeFromDate(String _date){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm a");
        return sdf.format(getTimestamp(_date));
    }

    public static String getTimeDate(Date _date){
        return sdf.format(_date);
    }

    public static long getDifferenceDays(Date date_1, Date date_2){
        long difference = date_1.getTime() - date_2.getTime();
        long differenceDays = difference / (24 * 60 * 60 * 1000);
        return differenceDays;
    }

    public static long getDifferenceSeconds(Date date_1, Date date_2){
        long difference = date_1.getTime() - date_2.getTime();
        long differenceSeconds = difference / 1000 % 60;
        return differenceSeconds;
    }

    public static long getDifferenceMinutes(Date date_1, Date date_2){
        long difference = date_1.getTime() - date_2.getTime();
        long differenceMinutes = difference / (60 * 1000) % 60;
        return differenceMinutes;
    }

    public static long getDifferenceHours(Date date_1, Date date_2){
        long difference = date_1.getTime() - date_2.getTime();
        long differenceHours = difference / (60 * 60 * 1000) % 24;
        return differenceHours;
    }

    public static Date convertLongTimeToDate(long longTime){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(longTime);
        return c.getTime();
    }

}
