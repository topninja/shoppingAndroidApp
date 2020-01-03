package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.adapters.AdapterViewShippingInfo;
import com.entage.nrd.entage.newItem.OnActivityDataItemListener;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewShippingInfoFragment extends Fragment {
    private static final String TAG = "FragmentViewShippingInf";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view;

    private RelativeLayout shipping, locations;
    private RecyclerView recyclerViewShipping, recyclerViewLocations;

    private ShippingInformation shippingInformation;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_shipping_info, container, false);
        mContext = getContext();

        getIncomingBundle();

        init();

        return view;
    }

    private void getIncomingBundle() {
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                shippingInformation = bundle.getParcelable("shippingInformation");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void init(){
        if(mOnActivityDataItemListener != null){
            view.findViewById(R.id.appBarLayout).setVisibility(View.GONE);
            mOnActivityDataItemListener.setTitle(mContext.getString(R.string.information_details)+" "+
                    mContext.getString(R.string.the_shipping) +" " +mContext.getString(R.string.and) +
                    mContext.getString(R.string.receiving_locations));
        }else {
            ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.information_details)+" "+
                    mContext.getString(R.string.the_shipping) +" " +mContext.getString(R.string.and) +
                    mContext.getString(R.string.receiving_locations));

            ImageView mBackArrow = view.findViewById(R.id.back);
            mBackArrow.setVisibility(View.VISIBLE);
            mBackArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }

        shipping = view.findViewById(R.id.layout_shipping_available);
        locations = view.findViewById(R.id.layout_receiving_location_available);

        recyclerViewShipping = view.findViewById(R.id.recyclerView_shipping);
        recyclerViewLocations = view.findViewById(R.id.recyclerView_location);

        if(shippingInformation != null){
            setupArea_shipping_available();
            setupReceiving_location();
        }
    }

    private void setupArea_shipping_available(){
        final ImageView arrow = (ImageView) shipping.getChildAt(2);
        ArrayList<AreaShippingAvailable> areaShippingAvailable = new ArrayList<>();
        if(shippingInformation.isShipping_available() && shippingInformation.getArea_shipping_available() != null){
            for(Map.Entry<String, HashMap<String, AreaShippingAvailable>> hashMapEntry :
                    shippingInformation.getArea_shipping_available().entrySet()){
                for(Map.Entry<String, AreaShippingAvailable> hashMap : hashMapEntry.getValue().entrySet()){
                    areaShippingAvailable.add(hashMap.getValue());
                }
            }
        }
        if(areaShippingAvailable.size()==0){
            areaShippingAvailable.add(new AreaShippingAvailable());
        }

        recyclerViewShipping.setNestedScrollingEnabled(false);
        recyclerViewShipping.setHasFixedSize(true);
        recyclerViewShipping.setLayoutManager(new LinearLayoutManager(mContext.getApplicationContext()));
        recyclerViewShipping.setAdapter(new AdapterViewShippingInfo(mContext, recyclerViewShipping, areaShippingAvailable, null,
                true));

        shipping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerViewShipping.getVisibility() == View.GONE){
                    UtilitiesMethods.expand(recyclerViewShipping);
                }else {
                    UtilitiesMethods.collapse(recyclerViewShipping);
                }
                arrow.animate().rotation(arrow.getRotation()+180).setDuration(500).start();
            }
        });

        if(areaShippingAvailable.size()>4){
            arrow.setRotation(270);
            recyclerViewShipping.setVisibility(View.GONE);
            UtilitiesMethods.collapse(recyclerViewShipping);
        }
    }

    private void setupReceiving_location(){
        final ImageView arrow = (ImageView) locations.getChildAt(2);
        ArrayList<ReceivingLocation> receivingLocation = new ArrayList<>();
        if(shippingInformation.isLocationAvailable() && shippingInformation.getReceiving_location()!=null){
            receivingLocation.addAll(shippingInformation.getReceiving_location());
        }
        if(receivingLocation.size()==0){
            receivingLocation.add(new ReceivingLocation());
        }

        recyclerViewLocations.setNestedScrollingEnabled(false);
        recyclerViewLocations.setHasFixedSize(true);
        recyclerViewLocations.setLayoutManager(new LinearLayoutManager(mContext.getApplicationContext()));
        recyclerViewLocations.setAdapter(new AdapterViewShippingInfo(mContext, recyclerViewLocations, null, receivingLocation,
                true));

        locations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerViewLocations.getVisibility() == View.GONE){
                    UtilitiesMethods.expand(recyclerViewLocations);
                }else {
                    UtilitiesMethods.collapse(recyclerViewLocations);
                }
                arrow.animate().rotation(arrow.getRotation()+180).setDuration(500).start();
            }
        });


        if(receivingLocation.size()>4){
            arrow.setRotation(270);
            recyclerViewLocations.setVisibility(View.GONE);
            UtilitiesMethods.collapse(recyclerViewLocations);
        }

    }

}
