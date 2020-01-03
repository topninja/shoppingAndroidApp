package com.entage.nrd.entage.basket;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ItemBasket;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterFAQ;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.LayoutViewItemInitOrder;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.NotificationsPriority;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.ShippingCompanies;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.ViewOptionsPrices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class FragmentInitOrder extends Fragment {
    private static final String TAG = "FragmentInitOrder";

    private Context mContext;
    private View view;
    private MessageId mMessageId;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference referenceOrders;

    private LinearLayout container;
    private TextView send_my_order, name_store, status_store, total_price, currency;

    private String user_id;
    private ArrayList<ItemBasket> itemBaskets;
    private String entagePageName, entagePageId;
    private HashMap<String, String> itemsNames;

    private HashMap<String, LayoutViewItemInitOrder> itemsInitOrder;

    private GlobalVariable globalVariable;
    private String textPayment_bs, textPayment_wr;
    private ViewOptionsPrices viewOptionsPrices;

    private MessageDialog messageDialog = new MessageDialog();
    private String  notificationKey, currencyPrice, currencyName;
    private final String ShippingPriceLeft = "-1";
    private HashMap<String, ArrayList<ItemBasket>> itemsBasketByItemId;

    private boolean isThisMyEntagePage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_init_order, container, false);
        mContext = getActivity();

        getIncomingBundle(); //  setupFirebaseAuth(); init();

        return view;
    }

    public void getIncomingBundle() {
        try {
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                entagePageId = bundle.getString(mContext.getString(R.string.field_entage_page_id));
                itemBaskets = bundle.getParcelableArrayList("ItemBasket");
                entagePageName = bundle.getString(mContext.getString(R.string.field_name_entage_page));

                itemsNames = (HashMap<String, String>) bundle.getSerializable("itemsNames");

                if (entagePageId != null && itemBaskets != null && entagePageName != null && itemsNames != null) {
                    setupFirebaseAuth();
                    init();

                    fetchEntagePageCurrency_checkIfUserOwnThisEntagePage();
                }
                else {
                    view.findViewById(R.id.progressBar_1).setVisibility(View.GONE);
                    ((TextView)view.findViewById(R.id.text_view_note)).setText(mContext.getString(R.string.happened_wrong_try_again));
                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                }
            }
        }
        catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException;" + e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mMessageId = (MessageId) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    private void init() {
        backArrow();
        initWidgets();
        onClickListener();
    }

    private void initWidgets() {
        itemsInitOrder = new HashMap<>();
        container = view.findViewById(R.id.container);

        currency = view.findViewById(R.id.currency);
        total_price = view.findViewById(R.id.total_price);
        send_my_order = view.findViewById(R.id.send_my_order);
        name_store = view.findViewById(R.id.name_store);
        status_store = view.findViewById(R.id.status_store);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.the_store).replace(":",""));

        textPayment_bs = mContext.getString(R.string.payment_method_bs);
        textPayment_wr = mContext.getString(R.string.payment_method_wr);

        ShippingCompanies.init(mContext);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        itemsBasketByItemId = new HashMap<>();
        for(ItemBasket itemBasket : itemBaskets){
            if(!itemsBasketByItemId.containsKey(itemBasket.getItem_id())){
                itemsBasketByItemId.put(itemBasket.getItem_id(), new ArrayList<ItemBasket>());
            }
            itemsBasketByItemId.get(itemBasket.getItem_id()).add(itemBasket);
        }

        viewOptionsPrices = new ViewOptionsPrices(mContext, null, (RelativeLayout) view.findViewById(R.id.layout_price), 16,
                mContext.getColor(R.color.entage_blue), globalVariable);
    }

    private void onClickListener() {
        send_my_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isThisMyEntagePage){
                    sendOrders_checking();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
                    builder.setMessage(mContext.getString(R.string.this_items_belong_to_you));
                    builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    private void backArrow() {
        TextView mTextBack = view.findViewById(R.id.titlePage);
        mTextBack.setText(mContext.getString(R.string.init_order));
        ImageView back = view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void fetchEntagePageCurrency_checkIfUserOwnThisEntagePage() {
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entagePageId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currencyPrice = (String) dataSnapshot.child(mContext.getString(R.string.field_currency_entage_page)).getValue();
                    currencyName = Currency.getInstance(currencyPrice).getDisplayName();

                    ArrayList<String> users_ids = (ArrayList<String>) dataSnapshot.child("users_ids").getValue();
                    isThisMyEntagePage = users_ids.contains(user_id);

                    ((TextView)view.findViewById(R.id.currency_entage_page)).setText(currencyName);

                    viewOptionsPrices.setCurrencyPrice(currencyPrice);

                    if(globalVariable.getCurrency() == null || globalVariable.getCurrency().toString().equals(currencyPrice)){
                        view.findViewById(R.id.layout_price).setVisibility(View.GONE);
                    }

                    checkEntagePageStatus();
                    initLayout();
                }else {
                    view.findViewById(R.id.progressBar_1).setVisibility(View.GONE);
                    ((TextView)view.findViewById(R.id.text_view_note)).setText(mContext.getString(R.string.happened_wrong_try_again));
                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.findViewById(R.id.progressBar_1).setVisibility(View.GONE);
                ((TextView)view.findViewById(R.id.text_view_note)).setText(mContext.getString(R.string.happened_wrong_try_again));
                view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
            }
        });
    }

    private void checkEntagePageStatus() {
        name_store.setText(entagePageName);

        // check status of the store
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_status))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_status_item));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status_item = (String) dataSnapshot.getValue();
                    if(status_item.equals(mContext.getString(R.string.item_authorized))){
                        status_store.setText(mContext.getString(R.string.working));

                    }else {
                        send_my_order.setOnClickListener(null);
                        status_store.setTextColor(mContext.getColor(R.color.red));
                        status_store.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        if(status_item.equals(mContext.getString(R.string.item_unauthorized))){
                            status_store.setText(mContext.getString(R.string.status_store_unauthorized));
                        }
                        else if(status_item.equals(mContext.getString(R.string.item_pending))){
                            status_store.setText(mContext.getString(R.string.status_store_pending));
                        }
                        else if(status_item.equals(mContext.getString(R.string.store_off))){
                            status_store.setText(mContext.getString(R.string.status_store_off));
                        }
                    }
                }
                else {
                    send_my_order.setOnClickListener(null);
                    status_store.setTextColor(mContext.getColor(R.color.red));
                    status_store.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    status_store.setText(mContext.getString(R.string.status_store_unauthorized));
                }

                view.findViewById(R.id.progressBar_1).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void initLayout() {
        for(Map.Entry<String, ArrayList<ItemBasket>> map : itemsBasketByItemId.entrySet()){

            if (isAdded() && mContext != null) { // to avid this exp : cannot be executed until the Fragment is attached to the FragmentManager.
                final LinearLayout mainLayout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                        .inflate(R.layout.layout_init_order_item, null, false);

                container.addView(mainLayout);

                itemsInitOrder.put(map.getKey(),  new LayoutViewItemInitOrder(mContext, map.getValue(),
                        itemsNames.get(map.getValue().get(0).getItem_basket_id()), mainLayout, currencyName));
            }
        }
    }

    public void addTotalPriceItem(String _p){
        BigDecimal total = PaymentsUtil.microsToString("0.0");
        for(LayoutViewItemInitOrder ito : itemsInitOrder.values()){
            total = total .add(ito.getTotalItems());
        }

        total_price.setText(PaymentsUtil.print(total));
        currency.setText(currencyName);
        viewOptionsPrices.setupPrice(PaymentsUtil.print(total));

        if(_p.equals(ShippingPriceLeft)) {
            view.findViewById(R.id.priece_not).setVisibility(View.VISIBLE);
        }
        else {
            view.findViewById(R.id.priece_not).setVisibility(View.GONE);
        }
    }

    private void sendOrders_checking(){
       sendOrderButton(false);

       // check user select the method of sending order and select the method of payment
        for(String itemId : itemsBasketByItemId.keySet()){
            if(!itemsInitOrder.containsKey(itemId) || itemsInitOrder.get(itemId).getShippingMethod() == null){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.select_method_sending_order));
                sendOrderButton(true);
                return;
            }

            if(itemsInitOrder.get(itemId).getPaymentsMethod() == null){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.select_method_payments));
                sendOrderButton(true);
                return;
            }
        }

        // check all items have same method of sending order (shipping or receiving location)
        // check all items have same area shipping
        int i= -1; // i=0 for AreaShippingAvailable, i=1 for ReceivingLocation
        AreaShippingAvailable area = null;
        ReceivingLocation rece = null;
        for(String itemId : itemsBasketByItemId.keySet()){
            Object ob = itemsInitOrder.get(itemId).getShippingMethod();
            if(ob instanceof AreaShippingAvailable){
                if(i==1){
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.items_must_have_same_method));
                    sendOrderButton(true);
                    return;
                }
                i=0;

                if(area!=null){
                    if(!area.getId_area().equals(((AreaShippingAvailable)ob).getId_area())){
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.items_must_have_same_area_shipping));
                        sendOrderButton(true);
                        return;
                    }
                }
                area = (AreaShippingAvailable) ob;
            }
            if (ob instanceof ReceivingLocation){
                if(i==0){
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.items_must_have_same_method));
                    sendOrderButton(true);
                    return;
                }
                i=1;
                rece = (ReceivingLocation) ob;
            }
        }

        // check all items has same method of payment
        int o= -1; // o=0 for textPayment_bs, o=1 for textPayment_wr
        for(String itemId : itemsBasketByItemId.keySet()){
          Object ob = itemsInitOrder.get(itemId).getPaymentsMethod();
            if(ob.equals(textPayment_bs)){
                if(o==1){
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.items_must_have_same_method_payment));
                    sendOrderButton(true);
                    return;
                }
                o=0;
            }
            if (ob.equals(textPayment_wr)){
                if(o==0){
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.items_must_have_same_method_payment));
                    sendOrderButton(true);
                    return;
                }
                o=1;
            }
        }

        // check all items quantities
        for(String itemId : itemsBasketByItemId.keySet()){
            if(!itemsInitOrder.get(itemId).checkQuantity()){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.enter_quantity_for_all_items));
                sendOrderButton(true);
                return;
            }
        }

        // collect data
        HashMap<String, ItemOrder> itemOrders = new HashMap<>();
        Order order = new Order(user_id, entagePageId, ServerValue.TIMESTAMP,
                null, entagePageName, DateTime.getTimestamp(),
                i==0? mContext.getString(R.string.method_shipping):mContext.getString(R.string.method_receiving_location),
                o==0? textPayment_bs:textPayment_wr, mContext.getString(R.string.status_order_initial));


        for(String itemId : itemsBasketByItemId.keySet()){
            itemsInitOrder.get(itemId).collectItemOrders(itemOrders, entagePageName);
            itemsInitOrder.get(itemId).collapse();
        }

        order.setItem_orders(itemOrders);
        order.setArea_selected(area);
        order.setLocation_selected(rece);

        Log.d(TAG, "sendOrders_checking: " + order);
        sendOrders(order);
    }

    private void sendOrders(final Order order){

        final DatabaseReference refEntagePageOrders = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_orders_initial));
        final DatabaseReference refUserOrders = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_orders))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_orders_initial));
        final DatabaseReference refUserBaskets = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_basket)).child(user_id);
        final DatabaseReference refItems_on_order = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items_on_order));
        final DatabaseReference refItems_on_basket = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items_on_basket));

        // good
        referenceOrders = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_orders));
        final String orderKey = referenceOrders.push().getKey();
        if(orderKey == null){
            sendOrderButton(true);
            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));

        }
        else {
            order.setOrder_id(orderKey);

            referenceOrders
                    .child(mContext.getString(R.string.dbname_orders_initial))
                    .child(orderKey)
                    .setValue(order)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mMessageId.setOrderId(orderKey, "fetch");

                                refEntagePageOrders.child(orderKey).setValue(user_id); // set  here user id;
                                refUserOrders.child(orderKey)
                                        .setValue(entagePageId); // set  here entage page id;

                                //delete from basket and add it to order
                                for(ItemOrder itemOrder : order.getItem_orders().values()){
                                    refItems_on_order.child(itemOrder.getItem_id()).child(orderKey).setValue(orderKey);
                                    refItems_on_basket.child(itemOrder.getItem_id()).child(user_id).removeValue();
                                }
                                for(String id : order.getItem_orders().keySet()){
                                    refUserBaskets.child(id).removeValue();
                                }

                                sendNotification(orderKey);

                                // update Count Basket
                                int countBasket = mContext.getSharedPreferences("entaji_app",
                                        MODE_PRIVATE).getInt("countBasket", -1);
                                int i =  countBasket==-1? -1 : (countBasket-order.getItem_orders().size());
                                mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                                        .putInt("countBasket", i).apply();

                                done();
                            }else {
                                sendOrderButton(true);
                                if(task.getException().getMessage().equals("Firebase Database error: Permission denied")){
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.status_store_unauthorized));
                                }else {
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                }
                            }
                        }
                    });
        }
    }

    private void done() {
        itemBaskets.clear();
        itemsNames.clear();
        entagePageId = null;
        itemsInitOrder.clear();

        setupSendSuccessfulLayout();

        Transition transition = new Fade();
        transition.setDuration(500);
        transition.addTarget(view.findViewById(R.id.layout_main));
        TransitionManager.beginDelayedTransition((LinearLayout)view.findViewById(R.id.layout_main), transition);
        view.findViewById(R.id.layout_main).setVisibility(View.GONE);

        Transition transition1 = new Fade();
        transition1.setDuration(500);
        transition1.addTarget(view.findViewById(R.id.layout_successfully_send));
        TransitionManager.beginDelayedTransition((LinearLayout)view.findViewById(R.id.layout_successfully_send), transition1);
        view.findViewById(R.id.layout_successfully_send).setVisibility(View.VISIBLE);
    }

    private void sendOrderButton(boolean t){
        send_my_order.setVisibility(t? View.VISIBLE:View.INVISIBLE);
        send_my_order.setClickable(t);
    }

    private void sendNotification(String orderId){
        // Notification
        if(notificationKey == null){
            StringBuilder body = new StringBuilder();
            for(String name : itemsNames.values()){
                body.append("- ").append(name).append('\n');
            }

            notificationKey = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_notifications)).push().getKey();
            String topic = Topics.getTopicsCustomersInEntagePage(entagePageId);
            String title = NotificationsTitles.addItemToBasket_Title(mContext);

            NotificationOnApp notificationOnApp = new NotificationOnApp(entagePageId,
                    orderId, null, null,
                    title, body.toString().trim(),
                    mContext.getString(R.string.notif_flag_new_order), user_id,
                    null, null,
                    DateTime.getTimestamp(),
                    NotificationsPriority.addItemToBasket(),
                    false);


            Notification notification = new Notification(entagePageId,
                    orderId,
                    "-1", topic, title, body.toString(),
                    mContext.getString(R.string.notif_flag_new_order), user_id,
                    "topic", "-1", "-1", orderId);

            mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_page_email_notifications))
                    .child(entagePageId)
                    .child(orderId)
                    .setValue(notificationOnApp);

            if(notificationKey != null){
                mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_notifications))
                        .child(mContext.getString(R.string.field_notification_to_topic))
                        .child(notificationKey)
                        .setValue(notification);
            }
        }
    }

    private void setupSendSuccessfulLayout(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(true);

        String[][] afq = new String[7][2];

        afq[0][0] = mContext.getString(R.string.send_order_qu_1);
        afq[0][1] = mContext.getString(R.string.send_order_an_1);
        afq[1][0] = mContext.getString(R.string.send_order_qu_2);
        afq[1][1] = mContext.getString(R.string.send_order_an_2);
        afq[2][0] = mContext.getString(R.string.send_order_qu_3);
        afq[2][1] = mContext.getString(R.string.send_order_an_3);
        afq[3][0] = mContext.getString(R.string.send_order_qu_4);
        afq[3][1] = mContext.getString(R.string.send_order_an_4);
        afq[4][0] = mContext.getString(R.string.send_order_qu_5);
        afq[4][1] = mContext.getString(R.string.send_order_an_5);
        afq[5][0] = mContext.getString(R.string.send_order_qu_6);
        afq[5][1] = mContext.getString(R.string.send_order_an_6);
        afq[6][0] = mContext.getString(R.string.send_order_qu_7);
        afq[6][1] = mContext.getString(R.string.send_order_an_7);

        AdapterFAQ basketAdapter = new AdapterFAQ(mContext, recyclerView, afq);
        recyclerView.setAdapter(basketAdapter);

        view.findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isAnonymous()) {
            user_id = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                    Toast.LENGTH_LONG).show();
            getActivity().onBackPressed();
        }
    }

}

