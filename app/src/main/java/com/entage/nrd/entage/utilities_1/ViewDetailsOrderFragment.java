package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ItemBasket;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.newItem.OnActivityDataItemListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class ViewDetailsOrderFragment extends Fragment {
    private static final String TAG = "ViewDetailsOrderFrag";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view;

    private FirebaseDatabase mFirebaseDatabase;

    private TextView name_store;
    private LinearLayout container;

    private Order order;
    private String orderId, currencyName;
    private BigDecimal totalPrice;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_view_details_order, container, false);
        mContext = getContext();

        //setupFirebaseAuth();

        init();
        getIncomingBundle();

        return view;
    }

    public void getIncomingBundle() {
        try {
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                orderId = bundle.getString("order_id");
                order = bundle.getParcelable("order");

                if (order != null) {
                    setupLayouts();
                }
            }
        }
        catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException;" + e.getMessage());
        }
    }

    private void init() {
        backArrow();
        initWidgets();
    }

    private void backArrow() {
        TextView mTextBack = view.findViewById(R.id.titlePage);
        mTextBack.setText(mContext.getString(R.string.details_order).replace(":",""));
        ImageView back = view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void initWidgets() {
        container = view.findViewById(R.id.container);

        name_store = view.findViewById(R.id.name_store);

        ((RelativeLayout)view.findViewById(R.id.send_my_order).getParent()).setVisibility(View.GONE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.the_store).replace(":",""));


        ShippingCompanies.init(mContext);

        totalPrice = PaymentsUtil.microsToString("0.0");

    }

    private void setupLayouts(){
        view.findViewById(R.id.progressBar_1).setVisibility(View.GONE);

        name_store.setText(order.getStore_name());
        currencyName = Currency.getInstance("SAR").getDisplayName();
        ((TextView)view.findViewById(R.id.currency_entage_page)).setText(currencyName);
        ((LinearLayout)view.findViewById(R.id.status_store).getParent()).setVisibility(View.GONE);

        if(order.getArea_selected()!=null){
            ((RadioButton)view.findViewById(R.id.areas_shipping)).setChecked(true);
            ((TextView) view.findViewById(R.id.shippinf_or_receiving_locations))
                    .setText(mContext.getString(R.string.selected_area_shipping));
            setAreaShippingData(order.getArea_selected(), view.findViewById(R.id.linearLayout_adapter_view_shipping_info), false,
                    (RadioGroup) view.findViewById(R.id.radioGrp_payment_methods));

        }else if(order.getLocation_selected()!=null){
            ((RadioButton)view.findViewById(R.id.receiving_location)).setChecked(true);
            ((TextView) view.findViewById(R.id.shippinf_or_receiving_locations))
                    .setText(mContext.getString(R.string.selected_receiving_location));
            setReceiving_locationData(order.getLocation_selected(),   view.findViewById(R.id.linearLayout_adapter_view_shipping_info),
                    false, (RadioGroup) view.findViewById(R.id.radioGrp_payment_methods));
        }
        view.findViewById(R.id.areas_shipping).setEnabled(true);
        view.findViewById(R.id.receiving_location).setEnabled(true);
        view.findViewById(R.id.areas_shipping).setClickable(false);
        view.findViewById(R.id.receiving_location).setClickable(false);
        view.findViewById(R.id.payment_bs).setClickable(false);
        view.findViewById(R.id.payment_wr).setClickable(false);

        TextView total_usd = view.findViewById(R.id.price_item);

        if(order.getItem_orders() != null){
            HashMap<String, ArrayList<ItemOrder>> itemsBasketByItemId = new HashMap<>();
            for(ItemOrder itemOrder : order.getItem_orders().values()){
                if(!itemsBasketByItemId.containsKey(itemOrder.getItem_id())){
                    itemsBasketByItemId.put(itemOrder.getItem_id(), new ArrayList<ItemOrder>());
                }
                itemsBasketByItemId.get(itemOrder.getItem_id()).add(itemOrder);
            }

            for(Map.Entry<String, ArrayList<ItemOrder>> map : itemsBasketByItemId.entrySet()){
                final LinearLayout mainLayout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                        .inflate(R.layout.layout_init_order_item, null, false);

                setData(map.getValue(),  map.getValue().get(0).getItem_name(),  mainLayout);

                container.addView(mainLayout);
            }

            ((TextView)view.findViewById(R.id.total_price)).setText(PaymentsUtil.print(totalPrice));
            ((TextView)view.findViewById(R.id.currency_1)).setText(currencyName);

            total_usd.setText(PaymentsUtil.converter_SAR_USD_print(totalPrice));
            ((TextView)view.findViewById(R.id.currency)).setText(Currency.getInstance("USD").getDisplayName());
        }
    }

    public void setData(ArrayList<ItemOrder> itemOrders, String itemName, final LinearLayout mainLayout) {

        String itemId = itemOrders.get(0).getItem_id();
        ArrayList<String> items_basket_id = new ArrayList<>();
        for(int i=0; i<itemOrders.size(); i++){
            items_basket_id.add(itemOrders.get(i).getItem_basket_id());
        }

        if(itemName.contains("(")){
            itemName = itemName.substring(0, itemName.indexOf("("));
        }

        ((TextView) mainLayout.findViewById(R.id.layout_item_text)).setText(itemName);

        ((TextView) mainLayout.findViewById(R.id.number_item)).setText(mContext.getString(R.string.number_item) +": "+ itemOrders.get(0).getItem_number());
        ((TextView) mainLayout.findViewById(R.id.item_name)).setText(mContext.getString(R.string.name_item) +": "+itemName);

        LinearLayout container_qty = mainLayout.findViewById(R.id.container_qty);

        BigDecimal subtotal = PaymentsUtil.microsToString("0.0");
        for(int i=0; i<itemOrders.size(); i++){
            ItemOrder itemOrder = itemOrders.get(i);

            LinearLayout layout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.layout_init_order_qty, null, false);

            ((TextView) layout.findViewById(R.id.options_item)).setText(itemOrder.getOptions() != null ?
                    itemOrder.getOptions().toString() : " - ");

            TextView quantity = layout.findViewById(R.id.quantity);

            TextView price_item = layout.findViewById(R.id.price_item);

            String itemPrice = itemOrder.getItem_price();
            price_item.setText(mContext.getString(R.string.price_item) +" "+itemPrice +" "+currencyName);

            //quantity.setEnabled(false);
            quantity.setFocusable(false);
            quantity.setText(itemOrder.getQuantity()+"");
            quantity.setBackground(null);

            subtotal = subtotal.add(PaymentsUtil.calculatingTotalPriceItem(itemPrice,
                    itemOrder.getQuantity(),
                    "0.0"));

            container_qty.addView(layout);

            if(itemOrder.getShipping_price()!=null && itemOrder.getShipping_price().equals("-1")){
                view.findViewById(R.id.priece_not).setVisibility(View.VISIBLE);
            }
        }

        mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info).setVisibility(View.GONE);
        final LinearLayout holder = mainLayout.findViewById(R.id.holder);
        UtilitiesMethods.collapse(holder);

        final ImageView arrow = mainLayout.findViewById(R.id.img);
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

        mainLayout.findViewById(R.id.info_receiving_order).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.line_info_receiving_order).setVisibility(View.GONE);

/*        if(order.getArea_selected()!=null){
            ((RadioButton)mainLayout.findViewById(R.id.areas_shipping)).setChecked(true);
            ((TextView) mainLayout.findViewById(R.id.shippinf_or_receiving_locations))
                    .setText(mContext.getString(R.string.selected_area_shipping));
            setAreaShippingData(order.getArea_selected(), mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info), false,
                    (RadioGroup) mainLayout.findViewById(R.id.radioGrp_payment_methods));

        }else if(order.getLocation_selected()!=null){
            ((RadioButton)mainLayout.findViewById(R.id.receiving_location)).setChecked(true);
            ((TextView) mainLayout.findViewById(R.id.shippinf_or_receiving_locations))
                    .setText(mContext.getString(R.string.selected_receiving_location));
            setReceiving_locationData(order.getLocation_selected(),   mainLayout.findViewById(R.id.linearLayout_adapter_view_shipping_info),
                    false, (RadioGroup) mainLayout.findViewById(R.id.radioGrp_payment_methods));
        }
        mainLayout.findViewById(R.id.areas_shipping).setEnabled(true);
        mainLayout.findViewById(R.id.receiving_location).setEnabled(true);
        mainLayout.findViewById(R.id.areas_shipping).setClickable(false);
        mainLayout.findViewById(R.id.receiving_location).setClickable(false);
        mainLayout.findViewById(R.id.payment_bs).setClickable(false);
        mainLayout.findViewById(R.id.payment_wr).setClickable(false);*/

        TextView total_price_item = mainLayout.findViewById(R.id.total_price_item);

        String shippingPrices = itemOrders.get(0).getShipping_price()!=null? itemOrders.get(0).getShipping_price():"0.0";

        if(shippingPrices.equals("-1")){
            total_price_item.setText(PaymentsUtil.print(subtotal) + " " + currencyName +"\n"+ mContext.getString(R.string.shipping_price_left));

        }else {
            subtotal = subtotal.add(PaymentsUtil.microsToString(shippingPrices));

            total_price_item.setText(PaymentsUtil.print(subtotal) + " " + currencyName);
        }

        totalPrice = totalPrice.add(subtotal);

        ((RelativeLayout)mainLayout.findViewById(R.id.img).getParent()).getChildAt(1).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.img).setVisibility(View.VISIBLE);
    }


    private void setAreaShippingData(AreaShippingAvailable areaShippingAvailable, View linearLayout, boolean goneTitle,
                                     RadioGroup payment_methods){
        linearLayout.setVisibility(View.VISIBLE);

        if(!goneTitle){
            ((RelativeLayout)linearLayout.findViewById(R.id.name_area).getParent()).setBackgroundColor(mContext.getColor(R.color.grayBG));
            ((TextView)linearLayout.findViewById(R.id.name_area)).setText(areaShippingAvailable.getName_area());
            linearLayout.findViewById(R.id.down_arrow).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.layout_payment_methods).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.line100).setVisibility(View.GONE);

            payment_methods.findViewById(R.id.payment_bs).setEnabled(areaShippingAvailable.isPaymentBs());
            payment_methods.findViewById(R.id.payment_wr).setEnabled(areaShippingAvailable.isPaymentWr());
            if(areaShippingAvailable.isPaymentBs()){
                ((RadioButton)payment_methods.findViewById(R.id.payment_bs)).setChecked(areaShippingAvailable.isPaymentBs());
            }else{
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
            ((RelativeLayout)linearLayout.findViewById(R.id.name_area).getParent()).setBackgroundColor(mContext.getColor(R.color.grayBG));
            ((TextView)linearLayout.findViewById(R.id.name_area)).setText(rl.getTitle() +","+ rl.getCity().getName_area());
            linearLayout.findViewById(R.id.down_arrow).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.layout_payment_methods).setVisibility(View.GONE);
            linearLayout.findViewById(R.id.line100).setVisibility(View.GONE);

            payment_methods.findViewById(R.id.payment_bs).setEnabled(rl.isPayment_bs());
            payment_methods.findViewById(R.id.payment_wr).setEnabled(rl.isPayment_wr());
            if(rl.isPayment_bs()){
                ((RadioButton)payment_methods.findViewById(R.id.payment_bs)).setChecked(rl.isPayment_bs());
            }else{
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



}
