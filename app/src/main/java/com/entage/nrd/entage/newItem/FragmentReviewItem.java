package com.entage.nrd.entage.newItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.DescriptionItem;
import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.Subscriptions.EntajiPageSubscriptionActivity;
import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.Models.AddingItemToAlgolia;
import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterViewShippingInfo;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.ActivityForOpenFragments;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.NotificationsPriority;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.CustomPagerAdapterItemsImg;
import com.entage.nrd.entage.utilities_1.CustomViewPager;
import com.entage.nrd.entage.utilities_1.LayoutTrackingCircles;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.github.irshulx.Editor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FragmentReviewItem extends Fragment {
    private static final String TAG = "ViewItemFragment";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, myRefArchives, myRefItems;

    private int numberImages, numberOfItems;

    private MessageDialog messageDialog = new MessageDialog();
    private String entagePageId, itemId, nameEntagePage, api_Key_algolia, nameItem;
    private GlobalVariable globalVariable;

    private Item mItemArchive, mItem_dbItems;

    private LinearLayout container;
    private SubscriptionPackage subscriptionPackage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       if(view == null){
           view = inflater.inflate(R.layout.fragment_review_new_item, container, false);
           mContext = getContext();

           getIncomingBundle();
           setupFirebaseAuth();
           init();
       }
       else {
           mOnActivityDataItemListener.setTitle(mContext.getString(R.string.publish_data_item));
       }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
        mOnActivityDataItemListener = null;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId = bundle.getString("entagePageId");
                itemId = bundle.getString(mContext.getString(R.string.field_item_id));
                nameEntagePage = bundle.getString(mContext.getString(R.string.field_entage_name));
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(){
        initWidgets();
        onClickListener();
        setupBarAddItemLayout();

        getItemsCount();
        getPackageEntagePage();

        checkEntagePageActivation();

        setDataInWidgets();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.publish_data_item));

        //addToBasket = view.findViewById(R.id.add_to_basket);
        container = view.findViewById(R.id.container);


        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener() {
    }

    private void setDataInWidgets(){
        //
        setupNameItem();

        // set Images
        setupImages();

        // categories
        categories();

        // set Options
        setupOptions();

        // set Shipping
        setupShippingInfo();

        // set Description
        setupDescription();

        // set Specifications
        setupSpecifications();

        view.findViewById(R.id.progress_9).setVisibility(View.GONE);
    }

    private void setupNameItem(){
        Query query = myRefArchives.child(mContext.getString(R.string.field_name_item));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    nameItem = dataSnapshot.getValue().toString();
                    if(isAdded() && mContext != null){
                        View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                        ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_name_item);
                        ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.name_item));
                        ((TextView)v.findViewById(R.id.information)).setText(nameItem);
                        container.addView(v);
                    }

                    view.findViewById(R.id.progress_9).setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupImages(){
        Query query = myRefArchives.child(mContext.getString(R.string.field_images_url));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists()){
                   ArrayList<String> urls = new ArrayList<>((Collection<? extends String>) dataSnapshot.getValue());

                   if(urls != null&& urls.size() > 0 ){
                       if(isAdded() && mContext != null){
                           View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                           ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_images_item);
                           ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.photos_item));
                           ((TextView)v.findViewById(R.id.information)).setVisibility(View.GONE);

                           v.findViewById(R.id.view_images).setVisibility(View.VISIBLE);
                           final LayoutTrackingCircles layoutTrackingCircles = v.findViewById(R.id.tracking);
                           CustomViewPager viewPager = v.findViewById(R.id.viewpager);

                           numberImages = urls.size();
                           layoutTrackingCircles.setColors(mContext.getResources().getColor(R.color.entage_blue_1),
                                   mContext.getResources().getColor(R.color.entage_gray));
                           layoutTrackingCircles.setNumberCircles(numberImages);
                           layoutTrackingCircles.setFocusAt(numberImages-1);
                           if(numberImages==1){layoutTrackingCircles.setVisibility(View.GONE);}


                           CustomPagerAdapterItemsImg customPagerAdapter = new CustomPagerAdapterItemsImg(mContext, urls, true);

                           viewPager.setAdapter(customPagerAdapter);

                           viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                               public void onPageScrollStateChanged(int state) {
                               }
                               public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                               }
                               public void onPageSelected(int position) {
                                   layoutTrackingCircles.setFocusAtAndRelease(numberImages - (position+1));
                               }
                           });

                           container.addView(v);
                       }
                   }
               }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setupOptions(){
        Query query = myRefArchives.child(mContext.getString(R.string.field_options_prices));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    OptionsPrices optionsPrices = dataSnapshot.getValue(OptionsPrices.class);

                    if(optionsPrices != null){
                        if(isAdded() && mContext != null){
                            View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                            ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_options_item);
                            ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.item_options).substring(0,
                                    mContext.getString(R.string.item_options).length()-1));


                            StringBuilder s = new StringBuilder();
                            if(optionsPrices.getOptionsTitle() != null){
                                for(int i=0; i<optionsPrices.getOptionsTitle().size() ; i++){
                                    s.append(optionsPrices.getOptionsTitle().get(i))
                                            .append(": ").append(printArray(optionsPrices.getOptions().get(i))).append("\n");
                                }
                            }
                            ((TextView)v.findViewById(R.id.information)).setText(s.toString().trim());

                            container.addView(v);
                        }

                        if(isAdded() && mContext != null){
                            View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                            ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_price_item);
                            ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.price_item).substring(0,
                                    mContext.getString(R.string.price_item).length()-1));

                            StringBuilder s = new StringBuilder();
                            if(optionsPrices.getLinkingOptions() != null){
                                for(int i=0; i<optionsPrices.getLinkingOptions().size() ; i++){
                                    s.append("- ")
                                            .append(printArray(optionsPrices.getLinkingOptions().get(i)))
                                            .append(": ")
                                            .append(optionsPrices.getCurrency_price())
                                            .append(" ")
                                            .append(optionsPrices.isIs_price_unify()? optionsPrices.getMain_price()+"" :
                                                    optionsPrices.getPrices().get(i)+"").append("\n");
                                }
                            }else {
                                s.append(optionsPrices.getMain_price())
                                        .append(optionsPrices.getCurrency_price()).append("\n");
                            }
                            ((TextView)v.findViewById(R.id.information)).setText(s.toString().trim());

                            container.addView(v);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setupSpecifications(){
        Query query = myRefArchives.child(mContext.getString(R.string.field_specifications));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final ArrayList<DataSpecifications> dataSpecification = new ArrayList<>();

                    for (DataSnapshot single: dataSnapshot.getChildren()) {
                        dataSpecification.add(single.getValue(DataSpecifications.class));
                    }

                    if(dataSpecification.size()> 0 && dataSpecification.get(0).getSpecifications_id()!=null  &&
                            dataSpecification.get(0).getSpecifications_id().equals("-1")){
                        dataSpecification.remove(0);
                    }


                    if(dataSpecification.size() > 0){
                        if(isAdded() && mContext != null){
                            View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                            ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_specifications_item);
                            ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.add_specifications));

                            StringBuilder s = new StringBuilder();
                            for(DataSpecifications ds : dataSpecification){
                                s.append(ds.getSpecifications()).append(": ").append(ds.getData()).append("\n");
                            }
                            ((TextView)v.findViewById(R.id.information)).setText(s.toString().trim());
                            container.addView(v);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setupDescription(){
        Query query = myRefArchives.child(mContext.getString(R.string.field_description_item));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    DescriptionItem descriptionItem = dataSnapshot .getValue(DescriptionItem.class);

                    if(descriptionItem != null){
                        if(isAdded() && mContext != null){
                            View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data,
                                    container, false);
                            ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_description_item);
                            ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.add_description));
                            ((TextView)v.findViewById(R.id.information)).setVisibility(View.GONE);

                            Editor editor = v.findViewById(R.id.renderer);

                            Map<Integer, String> headingTypeface = UtilitiesMethods.getHeadingTypeface();
                            Map<Integer, String> contentTypeface = UtilitiesMethods.getContentface();
                            editor.setHeadingTypeface(headingTypeface);
                            editor.setContentTypeface(contentTypeface);
                            editor.setDividerLayout(R.layout.tmpl_divider_layout);
                            editor.setEditorImageLayout(R.layout.tmpl_image_view_render);
                            editor.setListItemLayout(R.layout.tmpl_list_item);
                            editor.setVisibility(View.VISIBLE);

                            editor.render(descriptionItem.getContent_html());

                            container.addView(v);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void setupShippingInfo() {
        Query query = myRefArchives.child(mContext.getString(R.string.field_shipping_information));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                    if(isAdded() && mContext != null){
                        View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                        ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_shipping_item);
                        ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.select_places_shipping_available));
                        ((TextView)v.findViewById(R.id.information)).setVisibility(View.GONE);

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

                        RecyclerView recyclerViewShipping = v.findViewById(R.id.recyclerView_shipping);
                        recyclerViewShipping.setNestedScrollingEnabled(false);
                        recyclerViewShipping.setHasFixedSize(true);
                        recyclerViewShipping.setLayoutManager(new LinearLayoutManager(mContext.getApplicationContext()));
                        recyclerViewShipping.setAdapter(new AdapterViewShippingInfo(mContext, recyclerViewShipping,
                                areaShippingAvailable, null, false));
                        recyclerViewShipping.setVisibility(View.VISIBLE);

                        container.addView(v);
                    }

                    if(isAdded() && mContext != null){
                        View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                        ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_location_2);
                        ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.add_location_shipping_available));
                        ((TextView)v.findViewById(R.id.information)).setVisibility(View.GONE);

                        ArrayList<ReceivingLocation> receivingLocation = new ArrayList<>();
                        if(shippingInformation.isLocationAvailable() && shippingInformation.getReceiving_location()!=null){
                            receivingLocation.addAll(shippingInformation.getReceiving_location());
                        }
                        if(receivingLocation.size()==0){
                            receivingLocation.add(new ReceivingLocation());
                        }

                        RecyclerView recyclerViewLocations = v.findViewById(R.id.recyclerView_shipping);
                        recyclerViewLocations.setNestedScrollingEnabled(false);
                        recyclerViewLocations.setHasFixedSize(true);
                        recyclerViewLocations.setLayoutManager(new LinearLayoutManager(mContext.getApplicationContext()));
                        recyclerViewLocations.setAdapter(new AdapterViewShippingInfo(mContext, recyclerViewLocations,
                                null, receivingLocation, false));
                        recyclerViewLocations.setVisibility(View.VISIBLE);

                        container.addView(v);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void categories(){
        Query query = myRefArchives.child(mContext.getString(R.string.field_categories_item));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final ArrayList<String> categories = (ArrayList<String>) dataSnapshot.getValue();

                    if(categories!= null){
                        if(isAdded() && mContext != null){
                            View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_review_item_data, container, false);
                            ((ImageView)v.findViewById(R.id.image)).setImageResource(R.drawable.ic_categories_item);
                            ((TextView)v.findViewById(R.id._title)).setText(mContext.getString(R.string.add_item_the_categories));

                            ArrayList<String> arrayList =
                                    UtilitiesMethods.getCategoriesNames(categories, null, null);

                            ((TextView)v.findViewById(R.id.information)).setText(printArray(arrayList));
                            container.addView(v);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private String printArray(ArrayList<String> arrayList){
        StringBuilder s = new StringBuilder();
        for(String text : arrayList){
            s.append(text).append(", ");
        }
        s = new StringBuilder(s.substring(0, s.length() - 2));
        return s.toString();
    }

    private void checkEntagePageActivation(){
        mFirebaseDatabase.getReference()
                .child(getString(R.string.dbname_entage_pages_status))
                .child(entagePageId)
                .child("status_page")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(!dataSnapshot.getValue().equals("PAGE_AUTHORIZED")){
                                activate();
                            }
                        }else {
                            activate();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private AlertDialog alertDialog;
    private void activate(){
        if(isAdded() && mContext != null){

            View _view = this.getLayoutInflater().inflate(R.layout.dialog_activate_entage_page, null, false);

            TextView activation = _view.findViewById(R.id.activation);
            TextView cancel = _view.findViewById(R.id.cancel);

            activation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                    intent.putExtra("notification_flag", "ActivateEntagePageFragment");
                    startActivity(intent);
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    getActivity().onBackPressed();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(_view);
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    private void getPackageEntagePage(){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_subscription))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_current_subscription))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    subscriptionPackage = dataSnapshot.getValue(SubscriptionPackage.class);
                }

                save.setClickable(true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getItemsCount(){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_categories))
                .child(entagePageId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(snapshot.child(mContext.getString(R.string.field_categorie_items)).exists()){
                            numberOfItems += snapshot.child(mContext.getString(R.string.field_categorie_items)).getChildrenCount();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void upgradeDialog(String text, boolean subscribe){
        if(isAdded() && mContext!=null){
            View _view = this.getLayoutInflater().inflate(R.layout.dialog_upgrade_subscription, null);
            TextView textView = _view.findViewById(R.id.text);
            TextView upgrade = _view.findViewById(R.id.upgrade);
            TextView cancel = _view.findViewById(R.id.cancel);

            if(subscribe){
                upgrade.setText(mContext.getString(R.string.subscribe));
            }

            textView.setText(text);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(_view);
            final AlertDialog alert = builder.create();

            upgrade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putString("entajiPagesIds", entagePageId);
                    Intent intent = new Intent(mContext, EntajiPageSubscriptionActivity.class);
                    intent.putExtras(bundle);
                    mContext.startActivity(intent);
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });

            alert.show();
        }
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        if(itemId != null){
            myRef = mFirebaseDatabase.getReference();

            myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                    .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                    .child(itemId);
            myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                    .child(itemId);
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void publishData(){
        showProgress(false);

        // first chick is new item
        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){ // edit item
                    mItem_dbItems = dataSnapshot.getValue(Item.class);
                }
                getItemFromArchives(dataSnapshot.exists());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error(databaseError.getMessage());
            }
        });
    }

    private void getItemFromArchives(final boolean edit_or_new){
        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mItemArchive = dataSnapshot.getValue(Item.class);

                    if(edit_or_new){ // edit item
                        ArrayList<String> keyFields = new ArrayList<>();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            keyFields.add(snapshot.getKey());
                        }
                        getChangedData(keyFields);

                    }else { // new item

                        String package_id = subscriptionPackage.getPackage_id();
                        if(subscriptionPackage.isIs_running()){
                            if(package_id == null){
                                showProgress(true);
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.entaji_page_package_error));
                            }
                            else if(package_id.equals("1_starter")){
                                if(numberOfItems < 10){
                                    // Start With addToAlgolia
                                    getAPIKeyForAlgolia();
                                }else {
                                    showProgress(true);
                                    upgradeDialog(mContext.getString(R.string.max_items_1), false);
                                }
                            }
                            else if(package_id.equals("2_flame")){
                                if(numberOfItems< 20){
                                    // Start With addToAlgolia
                                    getAPIKeyForAlgolia();
                                }else {
                                    showProgress(true);
                                    upgradeDialog(mContext.getString(R.string.max_items_2), false);
                                }
                            }
                            else {
                                showProgress(true);
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.entaji_page_package_error));
                            }
                        }else {
                            showProgress(true);
                            upgradeDialog(mContext.getString(R.string.your_subscribe_finished)+" "+ subscriptionPackage.getExpire_date(), true);
                        }

                    }
                }
                else {
                    error(mContext.getString(R.string.no_data_to_publish));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error(databaseError.getMessage());
            }
        });
    }

    private void getChangedData(ArrayList<String> keyFields){
        if(keyFields.contains(mContext.getString(R.string.field_name_item)) ||
                keyFields.contains(mContext.getString(R.string.field_categories_item))){
            editDataAlgolia(mItemArchive.getName_item(),
                    mItemArchive.getCategories_item()!= null? new ArrayList<>(mItemArchive.getCategories_item()) : null,
                    new ArrayList<>(mItem_dbItems.getCategories_item()));
        }

        for(String key : keyFields){
            if(key.equals(mContext.getString(R.string.field_name_item))){
                mItem_dbItems.setName_item(mItemArchive.getName_item());
                //
                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_sharing_links))
                        .child(mContext.getString(R.string.field_sharing_links_items))
                        .child(itemId)
                        .removeValue();
            }
            else if(key.equals(mContext.getString(R.string.field_images_url))){
                editImagesUrls(new ArrayList<>(mItemArchive.getImages_url()), new ArrayList<>(mItem_dbItems.getImages_url()));
                mItem_dbItems.setImages_url(mItemArchive.getImages_url());
                //
                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_sharing_links))
                        .child(mContext.getString(R.string.field_sharing_links_items))
                        .child(itemId)
                        .removeValue();
            }
            else if(key.equals(mContext.getString(R.string.field_categories_item))){
                editCategoriesItem(new ArrayList<>(mItemArchive.getCategories_item()), new ArrayList<>(mItem_dbItems.getCategories_item()));
                mItem_dbItems.setCategories_item(mItemArchive.getCategories_item());
            }
            else if(key.equals(mContext.getString(R.string.field_description_item))){
                editDescriptionImagesUrls(mItemArchive.getDescription()!=null && mItemArchive.getDescription().getImages_url()!= null?
                                new ArrayList<>(mItemArchive.getDescription().getImages_url()) : new ArrayList<String>(),
                        mItem_dbItems.getDescription()!=null && mItem_dbItems.getDescription().getImages_url()!= null?
                                new ArrayList<>(mItem_dbItems.getDescription().getImages_url()) : null);
                mItem_dbItems.setDescription(mItemArchive.getDescription());
            }
            else if(key.equals(mContext.getString(R.string.field_specifications))){
                mItem_dbItems.setSpecifications(mItemArchive.getSpecifications());
            }
            else if(key.equals(mContext.getString(R.string.field_options_prices))){
                mItem_dbItems.setOptions_prices(mItemArchive.getOptions_prices());
            }
            else if(key.equals(mContext.getString(R.string.field_shipping_information))){
                mItem_dbItems.setShipping_information(mItemArchive.getShipping_information());
            }
            else if(key.equals(mContext.getString(R.string.field_item_for_men))){
                mItem_dbItems.setMen_item(mItemArchive.isMen_item());
            }
            else if(key.equals(mContext.getString(R.string.field_item_for_women))){
                mItem_dbItems.setWomen_item(mItemArchive.isWomen_item());
            }
            else if(key.equals(mContext.getString(R.string.field_item_for_children))){
                mItem_dbItems.setChildren_item(mItemArchive.isChildren_item());
            }
        }


        mItem_dbItems.setLast_edit_was_in(mItemArchive.getLast_edit_was_in());
        mItem_dbItems.setLast_publish_was_in(DateTime.getTimestamp());

        // add to database
        myRef.child(mContext.getString(R.string.dbname_items)).child(itemId).
                setValue(mItem_dbItems)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                Toast.LENGTH_SHORT).show();

                        //remove from archive
                        myRef.child(mContext.getString(R.string.dbname_entage_pages_archives))
                                .child(mItem_dbItems.getEntage_page_id())
                                .child(mContext.getString(R.string.field_saved_items))
                                .child(itemId)
                                .removeValue();

                        getActivity().onBackPressed();

                    }
                });
    }

    private void getNameEntagePage(){
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entaji_pages_names))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_name_entage_page));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    nameEntagePage = dataSnapshot.getValue().toString();
                }
                // Start With addToAlgolia
                getAPIKeyForAlgolia();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error(databaseError.getMessage());
            }
        });
    }

    private void getAPIKeyForAlgolia(){
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_app_data))
                .child("api_Key_algolia");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    api_Key_algolia = dataSnapshot.getValue().toString();

                    addToAlgolia();
                }
                else {
                    error("");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                error(databaseError.getMessage());
            }
        });
    }

    private void addToAlgolia(){
        Index index = new Client(globalVariable.getApplicationID(),api_Key_algolia).getIndex("entage_items");

        AddingItemToAlgolia itemToAlgolia = new AddingItemToAlgolia(mItemArchive.getName_item(), itemId,
                StringManipulation.convertPrintedArrayListToArrayListObject(mItemArchive.getCategories_item().get(0)).get(0),
                StringManipulation.getCategoriesForAlgolia(mItemArchive.getCategories_item()), mItemArchive.getEntage_page_id());
        index.addObjectAsync(new JSONObject(itemToAlgolia.getItem()), new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                try {
                    if(error == null){
                        String objectID =  content.get("objectID").toString();

                        mItemArchive.setAlgolia_id(objectID);
                        uploadItem();

                    }else {
                        error(error.getMessage());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void uploadItem(){

        mItemArchive.setLast_publish_was_in(DateTime.getTimestamp());
        Object o = ServerValue.TIMESTAMP;
        mItemArchive.setTimestamp(o);
        mItemArchive.setItem_number(o);

        if(mItemArchive.getDescription() == null){
            mItemArchive.setDescription(new DescriptionItem(mItemArchive.getItem_id(), "-1",null));
        }

        if(mItemArchive.getSpecifications() == null){
            DataSpecifications data =  new DataSpecifications("-1", "-1", "-1");
            mItemArchive.setSpecifications(new ArrayList<>(Collections.singleton(data)));
        }

        // add to database
        myRef.child(getString(R.string.dbname_entage_page_categories)) // entage_page_categories
                .child(mItemArchive.getEntage_page_id()) // Entage_page_id
                .child(mItemArchive.getItem_in_categorie()) // Categorie Name .child(newItem.getCategories_item().get(0))
                .child(getString(R.string.field_categorie_items)) // Categorie_items
                .child(itemId) // Item Id
                .setValue(itemId)
                // Item
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        final String itmeId = itemId;
                        // add in items_by_categories node
                        for(String path : mItemArchive.getCategories_item()){
                            ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
                            DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_items_by_categories));
                            for(String catId : cat){
                                databaseReference = databaseReference.child(catId);

                                databaseReference.child("items_id")
                                        .child(itmeId)
                                        .setValue(itmeId);
                            }
                        }

                        myRef.child(mContext.getString(R.string.dbname_items_status))
                                .child(itemId)
                                .child("status_item")
                                .setValue(mContext.getString(R.string.item_authorized));

                        // add to database
                        myRef.child(mContext.getString(R.string.dbname_items))
                                .child(itemId)
                                .setValue(mItemArchive)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //remove from archive
                                        myRef.child(mContext.getString(R.string.dbname_entage_pages_archives))
                                                .child(mItemArchive.getEntage_page_id())
                                                .child(mContext.getString(R.string.field_saved_items))
                                                .child(itmeId)
                                                .removeValue();

                                        // Notification
                                        String topic = Topics.getTopicsNotifying(mItemArchive.getEntage_page_id());
                                        String title = NotificationsTitles.addNewItem_Title(mContext);
                                        String notifyKey = myRef.child(mContext.getString(R.string.dbname_notifications)).push().getKey();

                                        NotificationOnApp notificationOnApp = new NotificationOnApp(entagePageId,
                                                itmeId, null, null,
                                                title, mItemArchive.getName_item(),
                                                mContext.getString(R.string.notif_flag_open_item), entagePageId,
                                                null, nameEntagePage,
                                                DateTime.getTimestamp(),
                                                NotificationsPriority.addNewItem(),
                                                false);

                                        if (notifyKey != null) {
                                            myRef.child(mContext.getString(R.string.dbname_subscriptions_emails))
                                                    .child(Topics.getTopicsFollowing(mItemArchive.getEntage_page_id()))
                                                    .child(mContext.getString(R.string.field_notifications))
                                                    .child(notifyKey)
                                                    .setValue(notificationOnApp);

                                            myRef.child(mContext.getString(R.string.dbname_notifications))
                                                    .child(mContext.getString(R.string.field_notification_to_topic))
                                                    .child(notifyKey)
                                                    .setValue(new Notification(mItemArchive.getEntage_page_id(), itemId,
                                                            "-1", topic, title, mItemArchive.getName_item(),
                                                            mContext.getString(R.string.notif_flag_open_item),
                                                            mItemArchive.getUsers_ids_has_access().get(0),
                                                            "topic", nameEntagePage, "-1", itemId));
                                        }

                                        // Increase post number
                                        increasePost(1);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e.getMessage().equals("Permission denied")){
                            error(mContext.getString(R.string.error_permission_denied));
                        }else {
                            error(e.getMessage());
                        }
                    }
                }) ;
    }

    private void increasePost(final long increaseBy){
        Log.d(TAG, "increasePost: number post + 1");

        Query query = myRef
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(mItemArchive.getEntage_page_id())
                .child("posts");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    long _numberPost = (long) dataSnapshot.getValue();

                    _numberPost = _numberPost+increaseBy;
                    myRef.child(mContext.getString(R.string.dbname_entage_pages))
                            .child(mItemArchive.getEntage_page_id())
                            .child(mContext.getString(R.string.entage_posts))
                            .setValue(_numberPost);

                    Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), EntageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
              showProgress(true);
            }
        });

    }

    private void editDataAlgolia(final String name, final ArrayList<String> newData, final ArrayList<String> currentData){

        // get first api
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_app_data))
                .child("api_Key_algolia");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    api_Key_algolia = dataSnapshot.getValue().toString();

                    globalVariable = ((GlobalVariable)mContext.getApplicationContext());
                    Client client = new Client(globalVariable.getApplicationID(), api_Key_algolia);
                    Index index_items = client.getIndex("entage_items");

                    AddingItemToAlgolia itemToAlgolia = new AddingItemToAlgolia(null, null, null,
                            null, null);

                    if(name != null){
                        itemToAlgolia.setItem_name(name);
                    }

                    if((newData != null) && (newData != currentData)){
                        itemToAlgolia.setCategorie_level_1(StringManipulation.convertPrintedArrayListToArrayListObject(newData.get(0)).get(0));
                        itemToAlgolia.setCategorie_level_2(StringManipulation.getCategoriesForAlgolia(newData));
                    }

                    index_items.partialUpdateObjectAsync(new JSONObject(itemToAlgolia.getItem()), mItem_dbItems.getAlgolia_id()
                            , false,
                            new CompletionHandler() {
                                @Override
                                public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                                    if (error != null) { }
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    private void editCategoriesItem(ArrayList<String> newData, ArrayList<String> currentData){
        if(mItem_dbItems.getCategories_item() != newData){
            // remove current in db
            for(String path : currentData){
                ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
                DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_items_by_categories));
                for(String catId : cat){
                    databaseReference = databaseReference.child(catId);

                    databaseReference.child("items_id")
                            .child(itemId)
                            .removeValue();
                }
            }

            // add new to db
            for(String path : newData){
                ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
                DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_items_by_categories));
                for(String catId : cat){
                    databaseReference = databaseReference.child(catId);

                    databaseReference.child("items_id")
                            .child(itemId)
                            .setValue(itemId);
                }
            }
        }
    }

    private void editImagesUrls(ArrayList<String> newData, ArrayList<String> currentData){
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        // deleting urls
        for(String imgURL : currentData){
            if(!newData.contains(imgURL)){
                firebaseStorage.getReferenceFromUrl(imgURL).delete();
            }
        }
    }

    private void editDescriptionImagesUrls(ArrayList<String> newData, ArrayList<String> currentData){
        if(currentData != null){
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            for(String imgURL : currentData){
                if(!newData.contains(imgURL)){
                    firebaseStorage.getReferenceFromUrl(imgURL).delete();
                }
            }
        }
    }

    private void error(String msg){
        showProgress(true);
        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_title) + "\n"+
                msg);
    }

    // setup Bar Add Item Layout
    private TextView save;
    private RelativeLayout nextStep;
    private ImageView tips;

    private void setupBarAddItemLayout(){
        save = view.findViewById(R.id.save);
        save.setText(mContext.getString(R.string.publish_data_item));
        nextStep = view.findViewById(R.id.next_step);
        nextStep.setVisibility(View.GONE);
        view.findViewById(R.id.progress_next_step).setVisibility(View.GONE);

        tips = view.findViewById(R.id.tips);

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        tips.setVisibility(View.GONE);

        save.setClickable(false);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subscriptionPackage != null){
                    publishData();
                }else {
                    Toast.makeText(mContext, getString(R.string.waite_to_loading), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showProgress(boolean boo){
        save.setEnabled(boo);
        save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
    }

}
