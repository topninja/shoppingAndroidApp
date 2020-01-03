package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewShippingInfo {
    private static final String TAG = "ViewOptionsPrices";

    private Context mContext;
    private LinearLayout layout_shipping;
    private ShippingInformation shippingInformation;
    private boolean isInFragment;
    private GlobalVariable globalVariable;

    private String userCityId, userStateId, userCountryId;

    private ArrayList<String> areasIdsAS, areasIdsRL;
    private HashMap<String, AreaShippingAvailable> areaShippingAvailable;
    private HashMap<String, ReceivingLocation> receivingLocation;

    private boolean isShippingToMyLocation = false;
    private boolean isReceivingLocationInMyCity = false;

    public ViewShippingInfo(Context mContext, LinearLayout layout_shipping, ShippingInformation shippingInformation, boolean isInFragment,
                            GlobalVariable _globalVariable) {
        this.mContext = mContext;
        this.layout_shipping = layout_shipping;
        this.shippingInformation = shippingInformation;
        this.isInFragment = isInFragment;
        this.globalVariable = _globalVariable;

        if(globalVariable == null){
            globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        }

        if(globalVariable.getLocationInformation() != null){
            userCityId = globalVariable.getLocationInformation().getCity_id();
            userStateId = globalVariable.getLocationInformation().getState_id();
            userCountryId = globalVariable.getLocationInformation().getCountry_id();
        }
        init();
    }

    private void init(){
        String s = mContext.getString(R.string.information_details);
        if(shippingInformation.isShipping_available() && shippingInformation.getArea_shipping_available() != null){
            areaShippingAvailable = new HashMap<>();
            areasIdsAS = new ArrayList<>();
            for(Map.Entry<String, HashMap<String, AreaShippingAvailable>> hashMapEntry : shippingInformation.getArea_shipping_available().entrySet()){
                for(Map.Entry<String, AreaShippingAvailable> hashMap : hashMapEntry.getValue().entrySet()){
                    areaShippingAvailable.put(hashMap.getKey(), hashMap.getValue());
                    areasIdsAS.add(hashMap.getValue().getId_area());
                }
            }

            setupArea_shipping_available();

            s+= " " + mContext.getString(R.string.the_shipping);
        }

        if(shippingInformation.isLocationAvailable() && shippingInformation.getReceiving_location()!=null){
            receivingLocation = new HashMap<>();
            areasIdsRL = new ArrayList<>();
            for(ReceivingLocation rl : shippingInformation.getReceiving_location()){
                receivingLocation.put(rl.getCity().getId_area(), rl);
                areasIdsRL.add(rl.getCity().getId_area());
            }
            setupReceiving_location();

            s+= " " + (shippingInformation.getArea_shipping_available() != null?mContext.getString(R.string.and):"") +
                    mContext.getString(R.string.receiving_locations);
        }

        //
        TextView more_details = layout_shipping.findViewById(R.id.more_details_shipping_info);
        more_details.setText(s);
    }

    private void setupArea_shipping_available(){
        layout_shipping.findViewById(R.id.layout_shipping_available).setVisibility(View.VISIBLE);

        if(shippingInformation.getArea_shipping_available()!=null){
            layout_shipping.findViewById(R.id.layout_shipping_home).setVisibility(View.VISIBLE);

            Log.d(TAG, "setupArea_shipping_available: " + userCityId+", " +userCountryId+", "+userStateId + ", " + areasIdsAS.toString());
            // check if item shipping to user's city or country or state
            if( (userCityId!=null && areasIdsAS.contains(userCityId)) ||
                    (userCountryId!=null && areasIdsAS.contains(userCountryId)) ||
                    (userStateId!=null && areasIdsAS.contains(userStateId))) {

                isShippingToMyLocation = true;
                ((TextView)layout_shipping.findViewById(R.id.text_shipping_home)).setText(mContext.getString(R.string.available)+" "+
                        mContext.getString(R.string.shipping_home));

                // check is free shipping to user's city or country or state
                if( (userCityId!=null && areaShippingAvailable.containsKey(userCityId) && areaShippingAvailable.get(userCityId).isFree_shipping()) ||
                        (userCountryId!=null && areaShippingAvailable.containsKey(userCountryId) && areaShippingAvailable.get(userCountryId).isFree_shipping()) ||
                        (userStateId!=null && areaShippingAvailable.containsKey(userStateId) && areaShippingAvailable.get(userStateId).isFree_shipping())){
                    layout_shipping.findViewById(R.id.layout_shipping_home_free).setVisibility(View.VISIBLE);
                }

            }
            else {
                ((TextView)layout_shipping.findViewById(R.id.text_shipping_home)).setText(mContext.getString(R.string.no)+" "+
                        mContext.getString(R.string.available)+" "+
                        mContext.getString(R.string.shipping_home));
            }
        }


        String st = mContext.getString(R.string.available)+" "+ mContext.getString(R.string.item_shipping_to)+" ";
        st += areaShippingAvailable.get(areasIdsAS.get(0)).getName_area();
        for(int i=1; i<areasIdsAS.size(); i++){
           if(i<3){
               st += ", "+ areaShippingAvailable.get(areasIdsAS.get(i)).getName_area();
           }else {
               st += " " +mContext.getString(R.string.and_more_areas);
               break;
           }
        }
        ((TextView)layout_shipping.findViewById(R.id.text_shipping_available_areas)).setText(st);
    }

    private void setupReceiving_location(){
        layout_shipping.findViewById(R.id.layout_receiving_location_available).setVisibility(View.VISIBLE);

        if(shippingInformation.getReceiving_location()!=null){
            layout_shipping.findViewById(R.id.layout_shipping_home_1).setVisibility(View.VISIBLE);

            // check if there is receiving location in user's city
            if((userCityId!=null && areasIdsRL.contains(userCityId)) ){

                isReceivingLocationInMyCity = true;
                ((TextView)layout_shipping.findViewById(R.id.text_receiving_location_home)).setText(mContext.getString(R.string.available)+" "+
                        mContext.getString(R.string.receiving_location_home));

            }else {
                ((TextView)layout_shipping.findViewById(R.id.text_receiving_location_home)).setText(mContext.getString(R.string.no)+" "+
                        mContext.getString(R.string.available)+" "+
                        mContext.getString(R.string.receiving_location_home));
            }
        }

        String st = mContext.getString(R.string.available)+" "+ mContext.getString(R.string.item_receiving_location_in)+" ";
        st += receivingLocation.get(areasIdsRL.get(0)).getCity().getName_area();
        for(int i=1; i<areasIdsRL.size(); i++){
            if(i<3){
                st += ", "+ receivingLocation.get(areasIdsRL.get(i)).getCity().getName_area();
            }else {
                st +=  " " +mContext.getString(R.string.and_more_areas);
                break;
            }
        }
        ((TextView)layout_shipping.findViewById(R.id.text_receiving_location_areas)).setText(st);
    }

    public boolean isShippingToMyLocation() {
        return isShippingToMyLocation;
    }

    public boolean isReceivingLocationInMyCity() {
        return isReceivingLocationInMyCity;
    }

    public static HashMap<String, AreaShippingAvailable> areasShippingAvailable(ShippingInformation shippingInformation){
        HashMap<String, AreaShippingAvailable> areaShippingAvailable = new HashMap<>();
        if(shippingInformation.isShipping_available() && shippingInformation.getArea_shipping_available() != null){
            areaShippingAvailable = new HashMap<>();
            for(Map.Entry<String, HashMap<String, AreaShippingAvailable>> hashMapEntry : shippingInformation.getArea_shipping_available().entrySet()){
                for(Map.Entry<String, AreaShippingAvailable> hashMap : hashMapEntry.getValue().entrySet()){
                    areaShippingAvailable.put(hashMap.getKey(), hashMap.getValue());
                }
            }
        }
        return areaShippingAvailable;
    }

    public static HashMap<String, ReceivingLocation> receivingLocations(ShippingInformation shippingInformation){
        HashMap<String, ReceivingLocation> receivingLocation = new HashMap<>();
        if(shippingInformation.isLocationAvailable() && shippingInformation.getReceiving_location()!=null){
            for(ReceivingLocation rl : shippingInformation.getReceiving_location()){
                receivingLocation.put(rl.getCity().getId_area(), rl);
            }
        }
        return receivingLocation;
    }

}
