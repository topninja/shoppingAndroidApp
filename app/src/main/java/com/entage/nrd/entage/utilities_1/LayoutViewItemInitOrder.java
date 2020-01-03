package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.basket.AddTotalPriceItems;
import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ItemBasket;
import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LayoutViewItemInitOrder {
    private static final String TAG = "LayoutViewItemInitOrder";

    private FirebaseDatabase mFirebaseDatabase;
    private AddTotalPriceItems mAddTotalPriceItems;

    private Context mContext;
    private ArrayList<ItemBasket> itemsBasket;
    private ArrayList<String> items_basket_id;
    private String itemName;

    private LinearLayout mainLayout, holder, container_qty;
    private ImageView arrow;
    private TextView items_status, total_price_item;
    private HashMap<String, EditText> editTexts_quantity;

    //private OptionsPrices optionsPrices;
    private ShippingInformation shippingInformation;
    private Object shippingMethod;
    private String paymentsMethod;
    private String shippingPrices = "0.0";
    private String itemPrice = "0.0";

    private String textPayment_bs, textPayment_wr, currencyName, itemId;

    private final String ShippingPriceLeft = "-1";

    public LayoutViewItemInitOrder() {


    }

    public LayoutViewItemInitOrder(Context mContext, ArrayList<ItemBasket> itemsBasket, String itemName, LinearLayout mainLayout,
                                   String currencyName) {
        this.mContext = mContext;
        this.itemsBasket = itemsBasket;
        this.itemName = itemName;
        this.mainLayout = mainLayout;
        this.currencyName = currencyName;

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        textPayment_bs = mContext.getString(R.string.payment_method_bs);
        textPayment_wr = mContext.getString(R.string.payment_method_wr);

        try{
            mAddTotalPriceItems = (AddTotalPriceItems) mContext;
        }
        catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }

        itemId = itemsBasket.get(0).getItem_id();
        items_basket_id = new ArrayList<>();
        for(int i=0; i<itemsBasket.size(); i++){
            items_basket_id.add(itemsBasket.get(i).getItem_basket_id());
        }

        init();
    }

    private void init() {
        Log.d(TAG, "init: " +  itemName.indexOf("("));
        if(itemName.contains("(")){
            itemName = itemName.substring(0, itemName.indexOf("("));
        }

        ((TextView) mainLayout.findViewById(R.id.layout_item_text)).setText(itemName);

        ((TextView) mainLayout.findViewById(R.id.number_item)).setText(mContext.getString(R.string.number_item) +": "+ itemsBasket.get(0).getItem_number());
        ((TextView) mainLayout.findViewById(R.id.item_name)).setText(mContext.getString(R.string.name_item) +": "+itemName);

        container_qty = mainLayout.findViewById(R.id.container_qty);

        editTexts_quantity = new HashMap<>();
        for(int i=0; i<itemsBasket.size(); i++){
            ItemBasket itemBasket = itemsBasket.get(i);

            LinearLayout layout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.layout_init_order_qty, null, false);

            ((TextView) layout.findViewById(R.id.options_item)).setText(itemBasket.getOptions() != null ?
                    itemBasket.getOptions().toString() : " - ");

            final EditText quantity = layout.findViewById(R.id.quantity);
            editTexts_quantity.put(itemBasket.getItem_basket_id(), quantity);

            TextView price_item = layout.findViewById(R.id.price_item);

            if(itemBasket.getPrice() != null){
                quantity.setEnabled(true);
                itemPrice = itemBasket.getPrice();
                price_item.setText(mContext.getString(R.string.price_item) +" "+itemPrice +" "+currencyName);
            }else {
                quantity.setEnabled(false);
                quantity.setText("0");
                itemPrice = "0.0";
                price_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                price_item.setTextColor(mContext.getColor(R.color.red));
                price_item.setText(mContext.getString(R.string.happened_wrong_price));
            }

            quantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String text = quantity.getText().toString();
                    int x = 0;
                    if(text.length()>0){
                        x = Integer.parseInt(text);
                    }

                    addTotalPriceItem("0");
                }
                @Override
                public void afterTextChanged(Editable editable) { }
            });

            container_qty.addView(layout);
        }

        total_price_item = mainLayout.findViewById(R.id.total_price_item);

        mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info).setVisibility(View.GONE);
        holder = mainLayout.findViewById(R.id.holder);
        UtilitiesMethods.collapse(holder);

        arrow = mainLayout.findViewById(R.id.img);
        mainLayout.findViewById(R.id.layout_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(arrow.getVisibility() == View.VISIBLE){
                    if(holder.getVisibility() == View.GONE){
                        UtilitiesMethods.expand(holder);
                    }else {
                        UtilitiesMethods.collapse(holder);
                    }
                    arrow.animate().rotation(arrow.getRotation() + 180).setDuration(500).start();
                }
            }
        });

        items_status = mainLayout.findViewById(R.id.status_item);

        // check status of the itmes
        checkStatusItem();
    }

    private void checkStatusItem(){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items_status))
                .child(itemId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status_item = dataSnapshot.child(mContext.getString(R.string.field_status_item)).getValue(String.class);
                    if(status_item.equals(mContext.getString(R.string.item_authorized))){
                        mainLayout.findViewById(R.id.status_item_layout).setVisibility(View.GONE);
                        //fetchOptionsPrices(itemBasket.getItem_id(), mainLayout);
                        fetchShipping_information(itemId, mainLayout);

                    }else {
                        if(status_item.equals(mContext.getString(R.string.item_unauthorized))){
                            items_status.setText(mContext.getString(R.string.status_item_unauthorized));
                        }else if(status_item.equals(mContext.getString(R.string.item_pending))){
                            items_status.setText(mContext.getString(R.string.status_item_pending));
                        }
                        ((RelativeLayout)arrow.getParent()).getChildAt(1).setVisibility(View.GONE);
                        arrow.setVisibility(View.VISIBLE);
                    }
                }else {
                    items_status.setText(mContext.getString(R.string.status_item_pending));
                    ((RelativeLayout)arrow.getParent()).getChildAt(1).setVisibility(View.GONE);
                    arrow.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ((RelativeLayout)arrow.getParent()).getChildAt(1).setVisibility(View.GONE);
            }
        });
    }

    private void fetchShipping_information(final String itemId, final LinearLayout mainLayout){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId)
                .child(mContext.getString(R.string.field_shipping_information));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                    if (shippingInformation.isShipping_available() && shippingInformation.getArea_shipping_available() != null) {
                        mainLayout.findViewById(R.id.areas_shipping).setEnabled(true);
                    }
                    if (shippingInformation.isLocationAvailable() && shippingInformation.getReceiving_location() != null) {
                        mainLayout.findViewById(R.id.receiving_location).setEnabled(true);
                    }

                    if (mainLayout.findViewById(R.id.areas_shipping).isEnabled()) {
                        ((RadioButton) mainLayout.findViewById(R.id.areas_shipping)).setChecked(true);
                        ((TextView) mainLayout.findViewById(R.id.shippinf_or_receiving_locations))
                                .setText(mContext.getString(R.string.selected_area_shipping));
                        mainLayout.findViewById(R.id.selected_places).setVisibility(View.VISIBLE);

                    } else {
                        if (mainLayout.findViewById(R.id.receiving_location).isEnabled()) {
                            ((RadioButton) mainLayout.findViewById(R.id.receiving_location)).setChecked(true);
                            ((TextView) mainLayout.findViewById(R.id.shippinf_or_receiving_locations))
                                    .setText(mContext.getString(R.string.selected_receiving_location));
                            mainLayout.findViewById(R.id.info_location_item).setVisibility(View.VISIBLE);
                        }
                    }

                    setupWidgets();
                }else {
                    ((RelativeLayout)mainLayout.findViewById(R.id.img).getParent()).getChildAt(1).setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ((RelativeLayout)mainLayout.findViewById(R.id.img).getParent()).getChildAt(1).setVisibility(View.GONE);
            }
        });
    }

    private void setupWidgets(){
        //
        shippingPrices = "0.0";

        addTotalPriceItem("0");

        final RelativeLayout info_location_item = mainLayout.findViewById(R.id.info_location_item);
        final RelativeLayout selected_places = mainLayout.findViewById(R.id.selected_places);

        //  areas_shipping
        final RadioButton r1 = mainLayout.findViewById(R.id.payment_bs);
        final RadioButton r2 = mainLayout.findViewById(R.id.payment_wr);
        ((RadioGroup) mainLayout.findViewById(R.id.radioGrp)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // reset
                mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info).setVisibility(View.GONE);
                shippingMethod = null;
                paymentsMethod = null;
                r1.setEnabled(false);
                r2.setEnabled(false);
                addTotalPriceItem("0");

                if (checkedId == R.id.areas_shipping) {
                    ((TextView) mainLayout.findViewById(R.id.shippinf_or_receiving_locations))
                            .setText(mContext.getString(R.string.selected_area_shipping));
                    info_location_item.setVisibility(View.GONE);
                    selected_places.setVisibility(View.VISIBLE);
                } else {
                    ((TextView) mainLayout.findViewById(R.id.shippinf_or_receiving_locations))
                            .setText(mContext.getString(R.string.selected_receiving_location));
                    selected_places.setVisibility(View.GONE);
                    info_location_item.setVisibility(View.VISIBLE);
                }
            }
        });

        // payment_methods
        ((RadioGroup) mainLayout.findViewById(R.id.radioGrp_payment_methods)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.payment_bs) {
                    paymentsMethod = textPayment_bs;
                } else {
                    paymentsMethod = textPayment_wr;
                }
            }
        });

        info_location_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showList(mContext.getString(R.string.receiving_locations), false,
                        mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info),
                        (RadioGroup) mainLayout.findViewById(R.id.radioGrp_payment_methods), total_price_item);
            }
        });

        selected_places.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showList(mContext.getString(R.string.selected_laces), true,
                        mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info),
                        (RadioGroup) mainLayout.findViewById(R.id.radioGrp_payment_methods), total_price_item);
            }
        });

        ((RelativeLayout)mainLayout.findViewById(R.id.img).getParent()).getChildAt(1).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.img).setVisibility(View.VISIBLE);
    }

    private void showList(String title, boolean areasShipping, final View view, final RadioGroup payment_methods,
                          final TextView total_price_item){

        View dialogView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_view_list_shipping_info, null);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGrp);

        ((TextView)dialogView.findViewById(R.id.message)).setText(title);

        if(areasShipping){

            final HashMap<Integer, AreaShippingAvailable> areas = new HashMap<>();
            for(Map.Entry<String, HashMap<String, AreaShippingAvailable>> hashMapEntry :
                    shippingInformation.getArea_shipping_available().entrySet()){
                for(Map.Entry<String, AreaShippingAvailable> hashMap : hashMapEntry.getValue().entrySet()){
                    AreaShippingAvailable areaShippingAvailable = hashMap.getValue();
                    areas.put(Integer.parseInt(areaShippingAvailable.getId_area()), areaShippingAvailable);

                    RadioButton radioButton = radioButton(areaShippingAvailable.getName_area());

                    final LinearLayout linearLayout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                            .inflate(R.layout.layout_adapter_shipping_info, null, false);

                    setAreaShippingData(areaShippingAvailable, linearLayout, true, null);

                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            linearLayout.setVisibility(isChecked? View.VISIBLE:View.GONE);
                        }
                    });
                    linearLayout.setVisibility(View.GONE);

                    if(shippingMethod != null && shippingMethod instanceof AreaShippingAvailable &&
                            ((AreaShippingAvailable) shippingMethod).getId_area().equals(areaShippingAvailable.getId_area())){
                        radioButton.setChecked(true);
                    }
                    radioButton.setId(Integer.parseInt(areaShippingAvailable.getId_area()));
                    radioGroup.addView(radioButton);
                    radioGroup.addView(linearLayout);
                }
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if(areas.containsKey(checkedId)){
                        shippingMethod = areas.get(checkedId);
                        setAreaShippingData(areas.get(checkedId), view, false, payment_methods);

                        //
                        shippingPrices = "0.0"; // reset
                        if(areas.get(checkedId).isFree_shipping()){
                            addTotalPriceItem("0");
                        }
                        else if(areas.get(checkedId).isShipping_price_later() || areas.get(checkedId).getShipping_company() == null ||
                                ShippingCompanies.getCompanyShowText(areas.get(checkedId).getShipping_company())
                                .equals(mContext.getString(R.string.shipping_company_later))){
                            shippingPrices = ShippingPriceLeft;
                            addTotalPriceItem(ShippingPriceLeft);
                        }
                        else if(!areas.get(checkedId).isFree_shipping()){
                            shippingPrices = areas.get(checkedId).getShipping_price();
                            addTotalPriceItem("0");
                        }
                    }
                }
            });
        }

        else {
            for(int i=0; i< shippingInformation.getReceiving_location().size();  i++){
                ReceivingLocation rl = shippingInformation.getReceiving_location().get(i);

                RadioButton radioButton = radioButton(rl.getTitle());

                final LinearLayout linearLayout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                        .inflate(R.layout.layout_adapter_shipping_info, null, false);

                setReceiving_locationData(rl,  linearLayout, true, payment_methods);

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        linearLayout.setVisibility(isChecked? View.VISIBLE:View.GONE);
                    }
                });
                linearLayout.setVisibility(View.GONE);

                if(shippingMethod != null && shippingMethod instanceof ReceivingLocation &&
                        shippingInformation.getReceiving_location().indexOf(shippingMethod) == i){
                    radioButton.setChecked(true);
                }
                radioButton.setId(i);
                radioGroup.addView(radioButton);
                radioGroup.addView(linearLayout);
            }

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    shippingMethod = shippingInformation.getReceiving_location().get(checkedId);
                    setReceiving_locationData(shippingInformation.getReceiving_location().get(checkedId),  view,
                            false, payment_methods);

                    shippingPrices = "0.0"; // reset
                    addTotalPriceItem("0");
                }
            });
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialogView);

        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alert = builder.create();
        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void setAreaShippingData(AreaShippingAvailable areaShippingAvailable, View linearLayout, boolean goneTitle,
                                     RadioGroup payment_methods){
        linearLayout.setVisibility(View.VISIBLE);

        if(!goneTitle){
            ((RelativeLayout)linearLayout.findViewById(R.id.name_area).getParent()).setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
            ((TextView)linearLayout.findViewById(R.id.name_area)).setText(areaShippingAvailable.getName_area());
            linearLayout.findViewById(R.id.down_arrow).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.layout_payment_methods).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.line100).setVisibility(View.GONE);

            payment_methods.findViewById(R.id.payment_bs).setEnabled(areaShippingAvailable.isPaymentBs());
            payment_methods.findViewById(R.id.payment_wr).setEnabled(areaShippingAvailable.isPaymentWr());
            if(areaShippingAvailable.isPaymentBs()){
                paymentsMethod = textPayment_bs;
                ((RadioButton)payment_methods.findViewById(R.id.payment_bs)).setChecked(areaShippingAvailable.isPaymentBs());
            }else{
                paymentsMethod = textPayment_wr;
                ((RadioButton)payment_methods.findViewById(R.id.payment_wr)).setChecked(areaShippingAvailable.isPaymentWr());
            }



        }else {
            linearLayout.findViewById(R.id.payment_before_sending).setClickable(false);
            linearLayout.findViewById(R.id.payment_upon_receipt).setClickable(false);
            linearLayout.findViewById(R.id.select_area).setVisibility(View.GONE);
        }

        String string = ShippingCompanies.getCompanyShowText(areaShippingAvailable.getShipping_company());

        ((TextView)linearLayout.findViewById(R.id.shipping_companies))
                .setText(Html.fromHtml("<b>"+mContext.getString(R.string.shipping_company_name)+":</b> "+ string));

        linearLayout.findViewById(R.id.shipping_price).setVisibility(View.VISIBLE);
        if(string != null && string.equals(mContext.getString(R.string.shipping_company_later))){
            ((TextView)linearLayout.findViewById(R.id.shipping_price))
                    .setText(Html.fromHtml("<b>"+mContext.getString(R.string.shipping_price)+":</b> " +string));

        }else if(string != null){
            ((TextView)linearLayout.findViewById(R.id.shipping_price))
                    .setText( Html.fromHtml("<b>"+mContext.getString(R.string.shipping_price)+":</b> " +
                            (areaShippingAvailable.isShipping_price_later()? mContext.getString(R.string.shipping_price_later):
                                    (areaShippingAvailable.getShipping_price()+" "+currencyName))
                    ));
        }

        ((CheckBox)linearLayout.findViewById(R.id.payment_before_sending))
                .setChecked(areaShippingAvailable.isPaymentBs());
        ((CheckBox)linearLayout.findViewById(R.id.payment_upon_receipt))
                .setChecked(areaShippingAvailable.isPaymentWr());

        linearLayout.findViewById(R.id.layout_shipping_home_free)
                .setVisibility(areaShippingAvailable.isFree_shipping()? View.VISIBLE:View.GONE);
        if(areaShippingAvailable.isFree_shipping()){
            ((TextView)linearLayout.findViewById(R.id.shipping_price))
                    .setText(Html.fromHtml("<b>"+mContext.getString(R.string.shipping_price)+":</b> " +
                            mContext.getString(R.string.shipping_is_free)));
        }
    }

    private void setReceiving_locationData(ReceivingLocation rl, View linearLayout, boolean goneTitle,
                                           RadioGroup payment_methods){
        linearLayout.setVisibility(View.VISIBLE);

        if(!goneTitle){
            ((RelativeLayout)linearLayout.findViewById(R.id.name_area).getParent()).setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
            ((TextView)linearLayout.findViewById(R.id.name_area)).setText(rl.getTitle() +","+ rl.getCity().getName_area());
            linearLayout.findViewById(R.id.down_arrow).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.layout_payment_methods).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.line100).setVisibility(View.GONE);

            payment_methods.findViewById(R.id.payment_bs).setEnabled(rl.isPayment_bs());
            payment_methods.findViewById(R.id.payment_wr).setEnabled(rl.isPayment_wr());
            if(rl.isPayment_bs()){
                paymentsMethod = textPayment_bs;
                ((RadioButton)payment_methods.findViewById(R.id.payment_bs)).setChecked(rl.isPayment_bs());
            }else{
                paymentsMethod = textPayment_wr;
                ((RadioButton)payment_methods.findViewById(R.id.payment_wr)).setChecked(rl.isPayment_wr());
            }

        }else {
           linearLayout.findViewById(R.id.payment_before_sending).setClickable(false);
           linearLayout.findViewById(R.id.payment_upon_receipt).setClickable(false);
            linearLayout.findViewById(R.id.select_area).setVisibility(View.GONE);
        }

        ((TextView)linearLayout.findViewById(R.id.shipping_companies))
                .setText(Html.fromHtml("<b>"+mContext.getString(R.string.receiving_locations_appear_in_end)+"</b> "+
                        rl.getCity().getName_area() + ", " + rl.getCountry().getName_area() + ". " +
                        "<br>"+ mContext.getString(R.string.description_address_1)+": "+ rl.getAddress()+" "+
                        "<br>"+mContext.getString(R.string.receiving_locations_appear_in_end_1)));

        linearLayout.findViewById(R.id.shipping_price).setVisibility(View.GONE);

        ((CheckBox)linearLayout.findViewById(R.id.payment_before_sending))
                .setChecked(rl.isPayment_bs());
        ((CheckBox)linearLayout.findViewById(R.id.payment_upon_receipt))
                .setChecked(rl.isPayment_wr());

        linearLayout.findViewById(R.id.layout_shipping_home_free).setVisibility(View.GONE);
    }

    private void addTotalPriceItem(String _p){
        mAddTotalPriceItems.addTotalPriceItem( _p);
    }

    //
    public void collapse(){
        UtilitiesMethods.collapse(holder);
    }

    public ShippingInformation getShippingInformation() {
        return shippingInformation;
    }

    public boolean checkQuantity() {
        for(String item_basket_id : editTexts_quantity.keySet()){
            if(editTexts_quantity.get(item_basket_id) == null || editTexts_quantity.get(item_basket_id).getText() == null
                    || editTexts_quantity.get(item_basket_id).getText().length()==0 || !editTexts_quantity.get(item_basket_id).isEnabled() ||
                    Integer.parseInt(editTexts_quantity.get(item_basket_id).getText().toString()) == 0){
                return false;
            }
        }
        return true;
    }

    public int getQuantity(String item_basket_id) {
        if(editTexts_quantity.get(item_basket_id) == null || editTexts_quantity.get(item_basket_id).getText() == null
                || editTexts_quantity.get(item_basket_id).getText().length()==0 || !editTexts_quantity.get(item_basket_id).isEnabled()){
            return 0;
        }
        else {
            return Integer.parseInt(editTexts_quantity.get(item_basket_id).getText().toString());
        }
    }

    public BigDecimal getTotalItems(){
        BigDecimal totalPriceItems = PaymentsUtil.microsToString("0.0");
        for(int i=0; i<itemsBasket.size(); i++) {
            ItemBasket itemBasket = itemsBasket.get(i);

            String item_basket_id = itemBasket.getItem_basket_id();
            int qty = 0;
            if(editTexts_quantity.get(item_basket_id) == null || editTexts_quantity.get(item_basket_id).getText() == null
                    || editTexts_quantity.get(item_basket_id).getText().length()==0 || !editTexts_quantity.get(item_basket_id).isEnabled()){
                qty = 0;
            }
            else {
                qty = Integer.valueOf(editTexts_quantity.get(itemBasket.getItem_basket_id()).getText().toString());
            }

            BigDecimal bigDecimal = PaymentsUtil.calculatingTotalPriceItem(itemBasket.getPrice(),
                    qty,
                    "0.0");

            totalPriceItems = totalPriceItems.add(bigDecimal);
        }


        String string = PaymentsUtil.print(totalPriceItems);

        if(shippingPrices.equals(ShippingPriceLeft)){
            total_price_item.setText(string + " " + currencyName +"\n"+ mContext.getString(R.string.shipping_price_left));

        }else {
            totalPriceItems = totalPriceItems.add(PaymentsUtil.microsToString(shippingPrices));

            string = PaymentsUtil.print(totalPriceItems);
            total_price_item.setText(string + " " + currencyName);
        }

        return totalPriceItems;
    }

    public String getShippingPrices() {
        return shippingPrices;
    }

    public void collectItemOrders(HashMap<String, ItemOrder> itemOrders, String entagePageName){
        for(int i=0; i<itemsBasket.size(); i++) {
            ItemBasket itemBasket = itemsBasket.get(i);
            String  item_basket_id  = itemBasket.getItem_basket_id();

            itemOrders.put(item_basket_id, new ItemOrder("", itemId, itemBasket.getItem_number(), item_basket_id, entagePageName,
                    itemName, itemBasket.getOptions(), Integer.parseInt(editTexts_quantity.get(item_basket_id).getText().toString()),
                    getShippingMethod() instanceof AreaShippingAvailable? (AreaShippingAvailable)getShippingMethod() : null,
                    getShippingMethod() instanceof ReceivingLocation? (ReceivingLocation)getShippingMethod() : null,
                    itemBasket.getPrice(),
                    shippingPrices));
        }

    }

    public Object getShippingMethod() {
        return shippingMethod;
    }

    public String getPaymentsMethod() {
        return paymentsMethod;
    }

    private RadioButton radioButton(String text){
        RadioButton radioButton = new RadioButton(mContext);
        radioButton.setText(text);
        radioButton.setTextSize(16);
        radioButton.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,20,0,12);
        radioButton.setLayoutParams(lp);
        return radioButton;
    }
}
