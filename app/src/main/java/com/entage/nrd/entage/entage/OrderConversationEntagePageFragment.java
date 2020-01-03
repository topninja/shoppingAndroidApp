package com.entage.nrd.entage.entage;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.basket.MessageId;
import com.entage.nrd.entage.Models.EditDataOrder;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.Models.Message;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.PaymentClaim;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.Models.UserAccountSettings;
import com.entage.nrd.entage.utilities_1.SharingLocation;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.FragmentMyAddresses;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterChats;
import com.entage.nrd.entage.adapters.AdapterConversationMessages;
import com.entage.nrd.entage.utilities_1.ConversationMessages;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.NotificationsPriority;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.ShippingCompanies;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewDetailsOrderFragment;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class OrderConversationEntagePageFragment extends Fragment {
    private static final String TAG = "OrderConversatio";

    private OnActivityOrderListener mOnActivityOrderListener;
    private OnActivityListener mOnActivityListener;

    private MessageId mMessageId;
    private View view;
    private Context mContext;
    private final static int PLACE_PICKER_REQUEST = 999;
    private final int LOCATION_REQUEST_CODE = 350;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRefOrder, myRefOrderConversation, myRefOnlinePage,  myRefStatus, myRefOnlineOrder; // myRef
    private ChildEventListener childEventListenerConversation;
    private ValueEventListener valueEventListenerOnlinePage, valueEventListenerOnlineOrder;
    private String userTokenId;

    private RelativeLayout layout_new_msg;
    private ImageView  send, location;
    private TextView new_msg_count;
    private EditText editText;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView username, nameUser,  online_page, online_order;
    private ImageView options, arrow, optionsTextBar;

    private Order order;
    private String orderId, entagePageName, entagePageId, userOrderId;

    private AdapterChats adapterChats;
    private AlertDialog alertMessages;

    private RelativeLayout relLayout_order, label_order, payment_clim, orderCompleted, orderCancelling;
    private LinearLayout labelOptionsTextBar;


    private String user_id, lung, currencyName;

    private boolean onlinePage, onlineOrder;

    private ArrayList<String> messagesId, unreadMsg;
    private HashMap<String, Message> textMessages;

    private boolean addListener = false;
    private BigDecimal totalOrderAmount;

    private ArrayList<MyAddress> myAddresses;

    private MessageDialog messageDialog = new MessageDialog();

    private String payment_id;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       if(view == null){
           view = inflater.inflate(R.layout.fragment_order_conversation , container , false);
           mContext = getActivity();

           ImageView mBackArrow = view.findViewById(R.id.back);
           mBackArrow.setVisibility(View.VISIBLE);
           mBackArrow.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   getActivity().onBackPressed(); }});


           getIncomingBundle();
       }

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                orderId =  bundle.getString("orderId");
                order = bundle.getParcelable("order");
                entagePageId = bundle.getString("entagePageId");

                if(orderId != null){
                    if(order != null){
                        entagePageId = order.getEntage_page_id();
                        userOrderId = order.getUser_id();
                    }
                    setupFirebaseAuth();
                }else {
                    getActivity().onBackPressed();
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnActivityOrderListener = (OnActivityOrderListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }


        try{
            mMessageId = (MessageId) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode:" + requestCode + ", resultCode: " + resultCode +", data: " + data);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(data != null && data.hasExtra("lat_lng")){
                    String lanLat = data.getStringExtra("lat_lng");
                    Address address =  data.getParcelableExtra("address");

                    if(lanLat != null){
                        mMessageId.setMessage("receiving_order_address");

                        if(address != null){
                            mMessageId.setAddress(new MyAddress(address.getAddressLine(0), lanLat,  address.getAddressLine(0),
                                    address.getCountryCode(),
                                    new LocationInformation( user_id, address.getSubAdminArea(), address.getPostalCode(),
                                            address.getAdminArea(), null,  address.getCountryCode(),
                                            null, address.getPostalCode())
                                    ,  null,  null,  null));
                        }
                        mMessageId.setExtraText1(lanLat);

                        editText.setText(ConversationMessages.getMessageText(mMessageId.getMessage()) + "\n" + convertAddress(mMessageId.getAddress()));
                    }
                }
            }
        }

    }

    private void init() {
        // remove notification if exist
        UtilitiesMethods.removeNotification(mContext, order.getOrder_id());


        initWidgets();
        onClickListener();

        setOrderData();
        //setupOrder();
    }

    private void initFunctions() {
        getUserTokenId();
        init();
        getUsername();
        setupChatRecyclerView();
        setupConversationMessages();
        setupOrdersEventListeners();
        setupStatusEventListeners();
    }

    private void initWidgets(){
        editText =  view.findViewById(R.id.text_question);
        editText.setEnabled(false);
        send =  view.findViewById(R.id.send);

        arrow = view.findViewById(R.id.arrow);
        label_order = view.findViewById(R.id.label_order);
        relLayout_order = view.findViewById(R.id.relLayout_order);
        orderCompleted = view.findViewById(R.id.order_completed);
        orderCancelling = view.findViewById(R.id.order_cancelled);
        location = view.findViewById(R.id.location);

        optionsTextBar = view.findViewById(R.id.options_text_bar);
        labelOptionsTextBar =  view.findViewById(R.id.label_options_text_bar);
        payment_clim =  view.findViewById(R.id.payment_clim);
        payment_clim.setVisibility(View.VISIBLE);

        layout_new_msg = view.findViewById(R.id.layout_new_msg);
        new_msg_count = view.findViewById(R.id.new_msg_count);

        username =  view.findViewById(R.id.username);
        nameUser =  view.findViewById(R.id.nameUser);
        online_page =  view.findViewById(R.id.online_page);
        online_order =  view.findViewById(R.id.online_order);
        options =  view.findViewById(R.id.options);

        unreadMsg = new ArrayList<>();

        GlobalVariable globalVariable  = ((GlobalVariable)mContext.getApplicationContext());
        lung = globalVariable.getLanguage()!=null? globalVariable.getLanguage() : Locale.getDefault().getLanguage();

        ShippingCompanies.init(mContext);
    }

    private void onClickListener(){
        if(order.getStatus().equals(mContext.getString(R.string.status_order_cancelled)) ||
                order.getStatus().equals(mContext.getString(R.string.status_order_cancelled_on_chatting)) ){

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation),
                            mContext.getString(R.string.order_has_cancelled));
                }
            };

            send.setOnClickListener(onClickListener);
            editText.setOnClickListener(onClickListener);
            location.setOnClickListener(onClickListener);
            payment_clim.setOnClickListener(onClickListener);
            orderCompleted.setOnClickListener(onClickListener);
            orderCancelling.setOnClickListener(onClickListener);

        }
        else if(order.getStatus().equals(mContext.getString(R.string.status_order_completed))){
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation),
                            mContext.getString(R.string.order_has_completed));
                }
            };

            send.setOnClickListener(onClickListener);
            editText.setOnClickListener(onClickListener);
            location.setOnClickListener(onClickListener);
            payment_clim.setOnClickListener(onClickListener);
            orderCompleted.setOnClickListener(onClickListener);
            orderCancelling.setOnClickListener(onClickListener);
        }
        else {
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    if(arrow.getRotationX() == 180){
                        arrow.setRotationX(0);
                        UtilitiesMethods.collapse(relLayout_order);
                    }
                    if(mMessageId.getMessage() != null){
                        sendMessage(mMessageId.getMessage(), null);
                    }
                }
            });

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    if(arrow.getRotationX() == 180){
                        arrow.setRotationX(0);
                        UtilitiesMethods.collapse(relLayout_order);
                    }
                    if(alertMessages != null){
                        alertMessages.show();
                    }
                }
            });

            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    sendMyLocation();
                }
            });

            payment_clim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(order != null){ // may be order is refreshing
                        if(order.getPayment_method().equals(mContext.getString(R.string.payment_method_wr))){
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation),
                                    mContext.getString(R.string.you_cant_claiming_payment));
                        }else {
                            payment_clim.setClickable(false);
                            view.findViewById(R.id.img_payment_clim).setVisibility(View.GONE);
                            view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.VISIBLE);

                            checkingPaymentClaiming();
                        }
                    }
                    else {
                        Toast.makeText(mContext, mContext.getString(R.string.waite_to_loading), Toast.LENGTH_SHORT).show();
                    }
                }
            });


            orderCompleted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderCompleted();
                }
            });

            orderCancelling.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orderCancelled();
                }
            });

        }

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMenuOptions();
            }
        });

        layout_new_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToPosition();
            }
        });

        optionsTextBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                    UtilitiesMethods.collapse(labelOptionsTextBar);
                    optionsTextBar.setImageResource(R.drawable.ic_three_circles);

                }else {
                    if(arrow.getRotationX() == 180){
                        arrow.setRotationX(0);
                        UtilitiesMethods.collapse(relLayout_order);
                    }

                    UtilitiesMethods.expand(labelOptionsTextBar);
                    optionsTextBar.setImageResource(R.drawable.ic_x);
                }
            }
        });
    }

    //
    private void fetchOrder(final boolean isRefreshing){
        //  we dont know where order is
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            order = dataSnapshot.getValue(Order.class);
                            orderFetched(isRefreshing);

                            // these function not work in case order no exist
                            presence();
                            setStatus(true);
                        }
                        else {
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_orders))
                                    .child(mContext.getString(R.string.dbname_orders_cancelled))
                                    .child(orderId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                order = dataSnapshot.getValue(Order.class);
                                                orderFetched(isRefreshing);
                                            }
                                            else {
                                                mFirebaseDatabase.getReference()
                                                        .child(mContext.getString(R.string.dbname_orders))
                                                        .child(mContext.getString(R.string.dbname_orders_completed))
                                                        .child(orderId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.exists()){
                                                                    order = dataSnapshot.getValue(Order.class);
                                                                    orderFetched(isRefreshing);
                                                                }
                                                                else {
                                                                    view.findViewById(R.id.progressChats).setVisibility(View.GONE);
                                                                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                                                                }
                                                            }
                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                view.findViewById(R.id.progressChats).setVisibility(View.GONE);
                                                            }
                                                        });
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            view.findViewById(R.id.progressChats).setVisibility(View.GONE);
                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        view.findViewById(R.id.progressChats).setVisibility(View.GONE);
                    }
                });
    }

    private void orderFetched(boolean isRefreshing){
        orderId = order.getOrder_id();
        orderId = order.getOrder_id();
        entagePageId = order.getEntage_page_id();
        userOrderId = order.getUser_id();
        if(!isRefreshing){
            fetchCurrencyEntagePage();
        }
    }

    private void checkingPaymentClaiming(){
        // check if user already paid
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(orderId)
                .child("is_paid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && (boolean)dataSnapshot.getValue()){
                    payment_clim.setClickable(true);
                    view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                    view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                    messageDialog.errorMessage(mContext,  mContext.getString(R.string.you_cant_do_this_operation)
                            ,mContext.getString(R.string.payment_claim_paid));
                }
                else {
                    // check if there is no requests edit order
                    mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_requests_edit_order))
                            .child(entagePageId)
                            .child(orderId)
                            .orderByChild("refused")
                            .equalTo(false)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            EditDataOrder editDataOrder = snapshot.getValue(EditDataOrder.class);

                                            messageDialog.errorMessage(mContext,  mContext.getString(R.string.you_cant_do_this_operation)
                                                    ,mContext.getString(R.string.replay_for_edit_order_first));
                                            int i= messagesId.indexOf(editDataOrder.getMessage_id());
                                            if(i!=-1){
                                                recyclerView.smoothScrollToPosition(i);
                                            }
                                            payment_clim.setClickable(true);
                                            view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                                            view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                                            break;
                                        }

                                    }
                                    else {
                                        mFirebaseDatabase.getReference()
                                                .child(mContext.getString(R.string.dbname_requests_payment_claim))
                                                .child(entagePageId)
                                                .child(orderId)
                                                .orderByChild("refused")
                                                .equalTo(false)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            payment_clim.setClickable(true);
                                                            view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                                                            view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);

                                                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                                PaymentClaim paymentClaim = snapshot.getValue(PaymentClaim.class);

                                                                messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation)
                                                                        ,mContext.getString(R.string.payment_claim_running));
                                                                int i= messagesId.indexOf(paymentClaim.getMessage_id());
                                                                if(i!=-1){
                                                                    recyclerView.smoothScrollToPosition(i);
                                                                }
                                                                break;
                                                            }

                                                        }else {
                                                            if(arrow.getRotationX() == 180){
                                                                arrow.setRotationX(0);
                                                                UtilitiesMethods.collapse(relLayout_order);
                                                            }
                                                            getOrderToPaymentClaim();
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        payment_clim.setClickable(true);
                                                        view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                                                        view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                                                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                                    }
                                                });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    payment_clim.setClickable(true);
                                    view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                                    view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                payment_clim.setClickable(true);
                view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
            }
        });

    }

    private void fetchCurrencyEntagePage() {
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(order.getEntage_page_id())
                .child(mContext.getString(R.string.field_currency_entage_page));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currencyPrice = (String) dataSnapshot.getValue();
                    currencyName = Currency.getInstance(currencyPrice).getDisplayName();
                }

                initFunctions();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.findViewById(R.id.progressChats).setVisibility(View.GONE);
            }
        });
    }

    //
    private void setOrderData(){
        entagePageName = order.getStore_name();

        ((TextView)view.findViewById(R.id.number_order)).setText(order.getOrder_number()+"");
        ((TextView)view.findViewById(R.id.the_store)).setText(entagePageName);
        ((TextView)view.findViewById(R.id.order_date)).setText(DateTime.convertToSimple(order.getTime_order())+"  "+
                DateTime.getTimeFromDate(order.getTime_order()));

        final StringBuilder body = new StringBuilder();
        final TextView items_names = view.findViewById(R.id.items_names);

        for(Map.Entry<String, ItemOrder> map : order.getItem_orders().entrySet()) {
            body.append("- ").append(map.getValue().getItem_name())
                    .append(StringManipulation.printArrayListItemsOrder(map.getValue().getOptions()))
                    .append('\n');
            items_names.setText(body.toString().trim());
        }

        view.findViewById(R.id.details_order).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("order_id", order.getOrder_id());
                bundle.putParcelable("order", order);
                mOnActivityListener.onActivityListener(new ViewDetailsOrderFragment(), bundle);
            }
        });

        UtilitiesMethods.collapse(relLayout_order);

        label_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(relLayout_order.getVisibility() == View.VISIBLE){
                    UtilitiesMethods.collapse(relLayout_order);
                    arrow.setRotationX(0);
                }else {
                    if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    UtilitiesMethods.expand(relLayout_order);
                    arrow.setRotationX(180);
                }
            }
        });

    }

    // getUsername
    private void getUsername(){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userOrderId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    (view.findViewById(R.id.dash)).setVisibility(View.VISIBLE);
                    UserAccountSettings userAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);
                    username.setVisibility(View.VISIBLE);
                    username.setText(userAccountSettings.getUsername());
                    nameUser.setText(userAccountSettings.getFirst_name()+" "+
                            (userAccountSettings.getLast_name()!=null? userAccountSettings.getLast_name():""));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // setupChatList
    private void setupChatRecyclerView(){
        ConversationMessages.init(mContext);
        recyclerView =  view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messagesId = new ArrayList<>();
        textMessages = new HashMap<>();

        HashMap<String, String> itemsNames = new HashMap<>();
        for(ItemOrder io : order.getItem_orders().values()){
            itemsNames.put(io.getItem_basket_id(), io.getItem_name());
        }

        adapterChats = new AdapterChats(mContext, messagesId, textMessages, user_id, entagePageId, orderId,
                unreadMsg, layout_new_msg, entagePageName, currencyName, order.getPayment_method(), order.isIs_paid(), order.getStatus(),
                itemsNames, mOnActivityOrderListener, mMessageId);

        recyclerView.setAdapter(adapterChats);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
                } else {
                    // Scrolling down
                }
                if(arrow.getRotationX() == 180){
                    arrow.setRotationX(0);
                    UtilitiesMethods.collapse(relLayout_order);
                }

            }
            /*@Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
                } else {
                    // Do something
                }
            }*/
        });
    }

    // setupListMessages
    private void setupConversationMessages(){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_conversation_messages, null);

        // this first,
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(mContext.getString(R.string.suggestion_adding_new_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("typeProblem", mContext.getString(R.string.suggestion_adding_new_text_problems));
                bundle.putString("id", orderId);
                mOnActivityListener.onActivityListener(new FragmentInformProblem(), bundle);
            }
        });
        alertMessages = builder.create();

        RecyclerView recyclerView = _view.findViewById(R.id.recyclerView) ;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        final ArrayList<String> cm_ids = ConversationMessages.getMsgIds_entagePage();
        recyclerView.setAdapter(new AdapterConversationMessages(mContext,  cm_ids, editText, alertMessages));

        /*final AutoCompleteTextView autoCompleteTextView = _view.findViewById(R.id.autoCompleteText);
        ArrayAdapter<String> adapter =new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, new AbstractList<String>() {
            @Override
            public String get(int index) {
                return ConversationMessages.getMessageText(cm_ids.get(index));
            }

            @Override
            public int size() {
                return cm_ids.size();
            }
        });

        autoCompleteTextView.setAdapter(adapter);

        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                String item = String.valueOf(parent.getItemAtPosition(position));

                if(item.length()>0){
                    mMessageId.setMessageId(cm_ids.get(position));
                    editText.setText(item);
                    alertMessages.dismiss();
                }

                autoCompleteTextView.setText("");
            }
        });

        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                autoCompleteTextView.showDropDown();
                return false;
            }
        });*/



        editText.setEnabled(true);
        //alertMessages.setCancelable(false);
        //alertMessages.setCanceledOnTouchOutside(false);

        /*message = adapter.getItem(position);
        editText.setText(message);
        alertMessages.dismiss();*/
    }

    // setupOrdersEventListeners
    private void setupOrdersEventListeners(){
        //myRefOrderConversation

        childEventListenerConversation = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message _message = dataSnapshot.getValue(Message.class);

                if(_message != null){
                    if (!messagesId.contains(_message.getMessage_id())){
                        // init
                        int index = messagesId.size();
                        messagesId.add(index, _message.getMessage_id());
                        textMessages.put(_message.getMessage_id(), _message);
                        adapterChats.notifyItemInserted(index);

                        // if this not your message
                        if (!_message.getUser_id().equals(user_id)) {
                            // remove notification if exist
                            UtilitiesMethods.removeNotification(mContext, _message.getMessage_id());

                            // in case user cancel the order and user of entage page in orderPage
                            if(_message.getMessage().equals("customer_canceled_order") &&
                                    !order.getStatus().equals(mContext.getString(R.string.status_order_cancelled_on_chatting))){
                                order.setStatus(mContext.getString(R.string.status_order_cancelled_on_chatting));
                                onClickListener();
                            }

                            if(!_message.isIs_read()){
                                unreadMsg.add(_message.getMessage_id());

                                new_msg_count.setText(unreadMsg.size()+"");
                                int i = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                                if(index-1 == i){
                                    recyclerView.smoothScrollToPosition(index);
                                } else if(index-1 > i){
                                    layout_new_msg.setVisibility(View.VISIBLE);
                                }
                            }

                            if(_message.getMessage().equals("customer_order_completed")){
                                // in case this message send and user on this page
                                order.setStatus(mContext.getString(R.string.status_order_completed));
                                adapterChats.setOrder_status(mContext.getString(R.string.dbname_orders_completed));
                                // disable all button
                                onClickListener();
                            }
                        }

                    }
                    else{
                        // if this my message
                        if (_message.getUser_id().equals(user_id)) {
                            if(_message.isIs_read() && !textMessages.get(_message.getMessage_id()).isIs_read()){
                                textMessages.get(_message.getMessage_id()).setIs_read(true);

                                int index = messagesId.indexOf(_message.getMessage_id());
                                if (index != -1) {
                                    adapterChats.notifyItemChanged(index);
                                } else {
                                    adapterChats.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged: ");
                Message _message = dataSnapshot.getValue(Message.class);
                if (_message != null){
                    if(_message.isIs_deleted()){
                        adapterChats.messageDeleted(_message.getMessage_id());

                    }else {
                        // if this my message
                        if (_message.getUser_id().equals(user_id)) {
                            if(_message.isIs_read()){
                                adapterChats.messageRead(_message.getMessage_id());
                            }
                        }
                    }

                    // check is payment_claiming message
                    if(_message.getMessage().equals("payment_claiming")){
                        if(_message.getExtra_text_1() != null){ // if there data in extra_text_1, there is replay
                            if(_message.getExtra_text_1().equals("payments_succeed")){
                                adapterChats.setIs_paid(true);
                            }
                            adapterChats.updateMessagePaymentClim(_message);

                        }else {
                            int index = messagesId.indexOf(_message.getMessage_id());
                            if(index!=-1){
                                adapterChats.notifyItemChanged(index);
                            }
                            else {
                                adapterChats.notifyDataSetChanged();
                            }
                        }
                    }

                    // check is edit_data_order message
                    if(_message.getMessage().equals("edit_data_order")){
                        if(_message.getExtra_text_1() != null){ // if there data in extra_text_1, there is replay
                            adapterChats.updateMessageEditOrder(_message);
                        }else {
                            int index = messagesId.indexOf(_message.getMessage_id());
                            if(index!=-1){
                                adapterChats.notifyItemChanged(index);
                            }else {
                                adapterChats.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        view.findViewById(R.id.progressChats).setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        if(addListener){
            myRefOrderConversation.addChildEventListener(childEventListenerConversation);
        }
    }

    private void setMessageSent(String message_id){
        myRefOrderConversation
                .child(message_id)
                .child("is_sent")
                .setValue(true);
    }

    private void setMessageRead(String message_id){
        Log.d(TAG, "onChildChanged: READ");
        myRefOrderConversation
                .child(message_id)
                .child("is_read")
                .setValue(true);
    }

    // setupStatusEventListeners
    private void setupStatusEventListeners(){
        myRefOnlineOrder = myRefStatus
                .child("status_user");

        valueEventListenerOnlineOrder = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    boolean boo = (boolean) dataSnapshot.getValue();
                    onlineOrder = boo;
                    if(boo){
                        online_page.setVisibility(View.GONE);
                        online_order.setText(mContext.getString(R.string.online));

                    }else {
                        online_page.setVisibility(View.VISIBLE);
                        online_order.setText("");
                    }
                }else {
                    online_page.setVisibility(View.VISIBLE);
                    online_order.setText("");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                online_page.setVisibility(View.VISIBLE);
                online_order.setText("");
            }
        };

        myRefOnlinePage = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_status))
                .child("order_page")
                //.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userOrderId)
                .child("status");

        valueEventListenerOnlinePage = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    boolean boo = (boolean) dataSnapshot.getValue();
                    onlinePage = boo;
                    if(boo){
                        online_page.setVisibility(View.VISIBLE);
                        online_page.setText(mContext.getString(R.string.connect));
                    }else {
                        online_page.setText(mContext.getString(R.string.disconnect));
                    }
                }
                else {
                    online_page.setText(mContext.getString(R.string.disconnect));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                online_page.setText(mContext.getString(R.string.disconnect));
            }
        };

        if(addListener){
            myRefOnlineOrder.addValueEventListener(valueEventListenerOnlineOrder);
            myRefOnlinePage.addValueEventListener(valueEventListenerOnlinePage);
        }
    }

    private void setupMenuOptions(){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_options, null);
        ListView listView = _view.findViewById(R.id.listView);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNeutralButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();

        ArrayList<String> arrayList = new ArrayList<>();
        //arrayList.add(mContext.getString(R.string.suggestion_adding_new_text));
        arrayList.add(mContext.getString(R.string.reporting));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alert.dismiss();
                if(position == 0 || position == 1){
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.orders_problems));
                    bundle.putString("id", orderId);
                    mOnActivityListener.onActivityListener(new FragmentInformProblem(), bundle);
                }
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    public void sendMessage(final String text, String message_id){
        if(message_id == null){
            message_id = myRefOrderConversation.push().getKey();
        }


        if(message_id != null){
            final Message _message = new Message(message_id, user_id, text, mMessageId.getExtraText1(), mMessageId.getExtraText2(),
                    mMessageId.getAddress(), DateTime.getTimestamp(), lung , false, false, false, false);

            // clear
            mMessageId.setMessage(null);
            mMessageId.setExtraText1(null);
            mMessageId.setExtraText2(null);
            editText.setText("");

            final int index = messagesId.size();
            messagesId.add(index, _message.getMessage_id());
            textMessages.put(_message.getMessage_id(), _message);
            adapterChats.notifyItemInserted(index);
            recyclerView.smoothScrollToPosition(index);

            final String finalMessage_id = message_id;
            myRefOrderConversation
                    .child(message_id)
                    .setValue(_message)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            adapterChats.messageSent(index, _message.getMessage_id());
                            setMessageSent(_message.getMessage_id());

                            sendNotification(text, finalMessage_id);

                        }
                    })
                    .addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Log.d(TAG, "onFailure: Cancelled");
                            adapterChats.messageCanceled(index, _message.getMessage_id());

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            adapterChats.messageCanceled(index, _message.getMessage_id());

                        }
                    }) ;
        }
    }

    private void sendNotification(String message, String message_id){
        //Log.d(TAG, "sendNotification: tokenId: " +  tokenId + ", online: " +online );
        if(userTokenId != null && !onlineOrder && !onlinePage){
            String newKey = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_notifications)).push().getKey();
            String title = NotificationsTitles.newMessage(mContext);
            String body = message;

            Notification notification = null;

            notification = new Notification( entagePageId,
                    orderId,
                    userTokenId, "-1", title, body,
                    mContext.getString(R.string.notif_flag_new_message), user_id, userOrderId,
                    "entagePageToUser", "-1", message_id);
            /*
           notification = new Notification("",
                        itemOrder.getOrder_id(),
                        tokenId, "0", title, body,
                        mContext.getString(R.string.notif_flag_new_message));
             */

            if (newKey != null) {
                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_notifications))
                        .child(mContext.getString(R.string.field_notification_to_user))
                        .child(newKey)
                        .setValue(notification);
            }

        }

        if(userTokenId == null){
            getUserTokenId();
        }
    }

    private void presence(){
        if(order!= null && order.getStatus().equals(mContext.getString(R.string.status_order_confirm))){
            final DatabaseReference firebaseDatabase =  myRefStatus
                    .child("status_entage_page");

            final DatabaseReference lastOnlineRef = myRefStatus
                    .child("last_online_entage_page");

            firebaseDatabase.setValue(true);

            // When this device disconnects, set value false
            firebaseDatabase.onDisconnect().setValue(false);

            // When I disconnect, update the last time I was seen online
            lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
        }

       /* final DatabaseReference connectedRef = mFirebaseDatabase.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {

                    firebaseDatabase.setValue(true);

                    // When this device disconnects, set value false
                    firebaseDatabase.onDisconnect().setValue(false);

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });*/
    }

    private void getUserTokenId(){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_token))
                .child(userOrderId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userTokenId = dataSnapshot.getValue(String.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void setStatus(boolean boo){
        if(order!= null && order.getStatus().equals(mContext.getString(R.string.status_order_confirm))){
            // entage_page
            myRefStatus.child("status_entage_page").setValue(boo);
        }
    }

    private void scrollToPosition(){
        Log.d(TAG, "scrollToPosition: " + messagesId.size()+ ", "+ unreadMsg.size());
        if(unreadMsg.size() != 0){
            recyclerView.smoothScrollToPosition(messagesId.size()-unreadMsg.size());
        }
    }

    private void getOrderToPaymentClaim(){
        // this step because some cases order has edit, so we want last edit of order
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        payment_clim.setClickable(true);
                        view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                        view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);


                        if(dataSnapshot.exists()){
                            order = dataSnapshot.getValue(Order.class);

                            paymentClaimDialog(order);
                        }else {
                            payment_clim.setClickable(true);
                            view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                            view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        payment_clim.setClickable(true);
                        view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                        view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void paymentClaimDialog(Order _order) {
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_payment_claim, null);

        final TextView paymentClaim = _view.findViewById(R.id.payment_claim);
        TextView cancel = _view.findViewById(R.id.cancel);
        final ProgressBar progressBar =  _view.findViewById(R.id.progressBar);
        final TextView error = _view.findViewById(R.id.error);
        error.setText(mContext.getString(R.string.error_fill_all_blank));

        final TextView items_amount_total = _view.findViewById(R.id.items_amount_total);
        final TextView total = _view.findViewById(R.id.the_total);
        final EditText shippingCompany =  _view.findViewById(R.id.agreed_shipping_company);
        final EditText amountShipping =  _view.findViewById(R.id.amount_agreed);

        totalOrderAmount = PaymentsUtil.microsToString("0.0");
        for(Map.Entry<String, ItemOrder> map : _order.getItem_orders().entrySet()) {
            totalOrderAmount = totalOrderAmount.add(PaymentsUtil.multiply(map.getValue().getItem_price(),
                    Integer.toString( map.getValue().getQuantity())));
        }

        total.setText(mContext.getString(R.string.the_total)+ " " + PaymentsUtil.print(totalOrderAmount));
        items_amount_total.setText(mContext.getString(R.string.items_amount_total)+ " " + PaymentsUtil.print(totalOrderAmount) + " " + currencyName);
        amountShipping.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = amountShipping.getText().toString();
                String x = "0.0";
                if(text.length()>0 && !text.equals(".")){
                    x = text;
                }
                total.setText(mContext.getString(R.string.the_total)+ " " + totalOrderAmount.add(PaymentsUtil.microsToString(x)) + " " + currencyName);
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });

        if(_order.getArea_selected() != null){
            if(!_order.getArea_selected().getShipping_company().equals("other") &&
                    !_order.getArea_selected().getShipping_company().equals("announced_later") ){
                shippingCompany.setText(_order.getArea_selected().getShipping_company());
            }
            if(!_order.getArea_selected().isShipping_price_later()){
                //amountShipping.setText(order.getArea_selected().getShipping_price());
            }
            if(_order.getArea_selected().isFree_shipping()){
                //amountShipping.setText("0");
            }
        }

        //
        BigDecimal shipping_price = PaymentsUtil.microsToString("0.0");


        // adding all shipping_price of items without repeat if having two itemorder for same item
        ArrayList<String> temp = new ArrayList<>();

        StringBuilder text_1 = new StringBuilder();
        StringBuilder text_2 = new StringBuilder();
        for(ItemOrder _itemOrder: _order.getItem_orders().values()){
            if(!temp.contains(_itemOrder.getItem_id())){
                String itemOrder_shipping_price = _itemOrder.getShipping_price();
                if(_itemOrder.getShipping_price().equals("-1")){
                    itemOrder_shipping_price = mContext.getString(R.string.later);
                }else {
                    shipping_price = shipping_price.add(PaymentsUtil.microsToString(itemOrder_shipping_price));
                }

                String itemName = _itemOrder.getItem_name();
                text_1.append(itemName)
                        .append(": (").append(mContext.getString(R.string.shipping_price)).append(": ")
                        .append(itemOrder_shipping_price)
                        .append(") \n");

                if(_itemOrder.getArea_selected() != null){
                    text_2.append(itemName)
                            .append(": (").append(mContext.getString(R.string.shipping_company_name)).append(": ")
                            .append(ShippingCompanies.getCompanyShowText(_itemOrder.getArea_selected().getShipping_company()))
                            .append(") \n");
                }
                temp.add(_itemOrder.getItem_id());
            }
        }
        ((TextView)_view.findViewById(R.id.extra_amount_agreed)).setText(text_1.toString().trim());
        ((TextView)_view.findViewById(R.id.extra_shipping_company)).setText(text_2.toString().trim());
        amountShipping.setText(PaymentsUtil.print(shipping_price));


        // this first,
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        paymentClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                error.setVisibility(View.GONE);

                if(shippingCompany.getText() == null || shippingCompany.getText().length() < 2){
                    error.setVisibility(View.VISIBLE);
                    return;
                }

                String _amountShipping = amountShipping.getText().toString();
                if(_amountShipping.length() == 0 || _amountShipping.equals(".") || Double.valueOf(_amountShipping)<0){
                    error.setVisibility(View.VISIBLE);
                    return;
                }

                //
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
                builder.setTitle(mContext.getString(R.string.payment_claim_title));
                builder.setMessage("\n");
                builder.setPositiveButton(mContext.getString(R.string.payment_claim), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        dialog.dismiss();
                        paymentClaim(PaymentsUtil.print(totalOrderAmount), amountShipping.getText().toString(),
                                shippingCompany.getText().toString());
                    }
                });
                builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void paymentClaim(String totalOrderAmount, String shipping_price, String shipping_company) {
        payment_clim.setClickable(false);
        view.findViewById(R.id.img_payment_clim).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.VISIBLE);

        final String message_id = myRefOrderConversation.push().getKey();

        if(message_id != null){
            final PaymentClaim paymentClaim = new PaymentClaim(user_id, entagePageId,
                    orderId, userOrderId, orderId,
                    DateTime.getTimestamp(), PaymentsUtil.print(PaymentsUtil.microsToString(totalOrderAmount)),
                    PaymentsUtil.print(PaymentsUtil.microsToString(shipping_price)),
                    shipping_company,false ,false ,
                    message_id, "-1", "-1");

            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_requests_payment_claim))
                    .child(entagePageId)
                    .child(orderId)
                    .child(message_id)
                    .setValue(paymentClaim)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_orders))
                                        .child(mContext.getString(R.string.dbname_orders_ongoing))
                                        .child(orderId)
                                        .child("payment_claim")
                                        .setValue(paymentClaim);

                                adapterChats.setPaymentClaim(message_id, paymentClaim);

                                // send message
                                sendMessage("payment_claiming", message_id);

                                String title = NotificationsTitles.paymentClim(mContext);
                                NotificationOnApp notificationOnApp = new NotificationOnApp(entagePageId,
                                        orderId, null, null,
                                        title, title,
                                        mContext.getString(R.string.notif_flag_new_message), user_id,
                                        null, entagePageName,
                                        DateTime.getTimestamp(),
                                        NotificationsPriority.addItemToBasket(),
                                        onlineOrder);
                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_users_email_messages))
                                        .child(userOrderId)
                                        .child(message_id)
                                        .setValue(notificationOnApp);

                            }else {
                                // Firebase Database error: Permission denied
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            }

                            if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                            payment_clim.setClickable(true);
                            view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
                            view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
                        }
                    });
        }
        else {
            payment_clim.setClickable(true);
            view.findViewById(R.id.progressBar_payment_clim ).setVisibility(View.GONE);
            view.findViewById(R.id.img_payment_clim).setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
        }
    }

    private void orderCancelled(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.cancelling_order));
        builder.setMessage(mContext.getString(R.string.confirming_cancelling_order));

        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.cancel_my_order) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                orderCancelling.setClickable(false);
                view.findViewById(R.id.img_order_cancelled).setVisibility(View.GONE);
                view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.VISIBLE);

                // check if there is payment clime open
                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_requests_payment_claim))
                        .child(entagePageId)
                        .child(orderId)
                        .orderByChild("refused")
                        .equalTo(false)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                orderCancelling.setClickable(true);
                                view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.GONE);
                                view.findViewById(R.id.img_order_cancelled).setVisibility(View.VISIBLE);

                                if(dataSnapshot.exists()){
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation)
                                            ,mContext.getString(R.string.payment_claim_running));

                                }else {
                                    confirmCancelOrder();;
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                orderCancelling.setClickable(true);
                                view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.GONE);
                                view.findViewById(R.id.img_order_cancelled).setVisibility(View.VISIBLE);
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            }
                        });
            }
        });
        builder.show();
    }

    private void confirmCancelOrder(){
        final View dialogView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_status_order, null);

        ((TextView)dialogView.findViewById(R.id.title)).setText(mContext.getString(R.string.cancel_order));
        dialogView.findViewById(R.id.title).setVisibility(View.VISIBLE);

        ((TextView)dialogView.findViewById(R.id.text)).setText("\n" + mContext.getString(R.string.reason_for_cancellation));

        final EditText editText = dialogView.findViewById(R.id.edit_text);
        editText.setHint(mContext.getString(R.string.reason_for_cancellation));
        editText.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialogView);
        builder.setPositiveButton(mContext.getString(R.string.cancel_my_order) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelOrder(editText.getText() != null && editText.getText().length() > 0?
                        editText.getText().toString() : mContext.getString(R.string.no_reason));
            }
        });

        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private void cancelOrder(final String reason_for_cancellation){
        final DatabaseReference refItems_on_order = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items_on_order));

        Order _order = order.copy();
        _order.setCancelled_by_1(user_id);
        _order.setCancelled_by_2("entage_page");
        _order.setCancelled_date(DateTime.getTimestamp());
        _order.setReason_for_cancellation(reason_for_cancellation);
        _order.setStatus(mContext.getString(R.string.status_order_cancelled_on_chatting));

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .setValue(_order)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        orderCancelling.setClickable(true);
                        view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.GONE);
                        view.findViewById(R.id.img_order_cancelled).setVisibility(View.VISIBLE);

                        if(task.isSuccessful()){
                            moveOrderTo_Cancelled(orderId);

                            for(ItemOrder itemOrder : order.getItem_orders().values()){
                                refItems_on_order.child(itemOrder.getItem_id()).child(order.getOrder_id()).removeValue();
                            }

                            mMessageId.setOrderId(orderId, "update");

                            adapterChats.setOrder_status(mContext.getString(R.string.status_order_cancelled_on_chatting));
                            order.setStatus(mContext.getString(R.string.status_order_cancelled_on_chatting));

                            // disable all button
                            onClickListener();

                            // send message
                            mMessageId.setExtraText1(reason_for_cancellation);
                            sendMessage("entage_page_canceled_order", null);

                            // set order id to orders list --> very important step
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_orders))
                                    .child(mContext.getString(R.string.dbname_orders_list))
                                    .child(order.getOrder_id())
                                    .setValue("not_working");

                            if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                        }
                        else {
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                        }
                    }
                });
    }

    // this function same in adapterOrder (deleteOrderFromDb_CanceledByCustomer)
    private void moveOrderTo_Cancelled(String orderId){
        // remove order from ongoing list
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(order.getOrder_id())
                .removeValue();

        // set order id in cancelled for entage page
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(order.getEntage_page_id())
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .setValue(order.getUser_id());

        // set order id in cancelled for user
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_orders))
                .child(order.getUser_id())
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .setValue(order.getEntage_page_id());

        // remove order id from ongoing for entage page
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(order.getEntage_page_id())
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(order.getOrder_id())
                .removeValue();

        // remove order id from ongoing for user
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_orders))
                .child(order.getUser_id())
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(order.getOrder_id())
                .removeValue();
    }

    private void orderCompleted(){
        if(order.getPayment_method().equals(mContext.getString(R.string.payment_method_wr))){
            confirmOrderCompleted();
        }
        else if(order.getPayment_method().equals(mContext.getString(R.string.payment_method_bs))){
            checkIfUserPaid_Completed();
        }
    }

    private void checkIfUserPaid_Completed(){
        orderCompleted.setClickable(false);
        view.findViewById(R.id.img_order_completed).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar_order_completed).setVisibility(View.VISIBLE);

        // check if user has paid
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(orderId)
                .child("is_paid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderCompleted.setClickable(true);
                        view.findViewById(R.id.progressBar_order_completed).setVisibility(View.GONE);
                        view.findViewById(R.id.img_order_completed).setVisibility(View.VISIBLE);

                        if(dataSnapshot.exists() && (boolean)dataSnapshot.getValue()){
                            confirmOrderCompleted();
                        }else {
                            if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                            messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation),
                                    mContext.getString(R.string.customer_not_pay_order_amount));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        orderCompleted.setClickable(true);
                        view.findViewById(R.id.progressBar_order_completed).setVisibility(View.GONE);
                        view.findViewById(R.id.img_order_completed).setVisibility(View.VISIBLE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void confirmOrderCompleted(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.order_is_completed));
        builder.setMessage(mContext.getString(R.string.confirming_completing_order_1));

        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.order_is_completed) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                orderCompleted_done();
            }
        });
        builder.show();
    }

    private void orderCompleted_done(){
        orderCompleted.setClickable(false);
        view.findViewById(R.id.img_order_completed).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar_order_completed).setVisibility(View.VISIBLE);

        final DatabaseReference refItems_on_order = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items_on_order));

        Order _order = order.copy();
        _order.setCancelled_date(DateTime.getTimestamp());
        _order.setExtra_data(user_id); // this for who sender_order_completed
        _order.setStatus(mContext.getString(R.string.status_order_completed));

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_completed))
                .child(order.getOrder_id())
                .setValue(_order)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        orderCompleted.setClickable(true);
                        view.findViewById(R.id.progressBar_order_completed).setVisibility(View.GONE);
                        view.findViewById(R.id.img_order_completed).setVisibility(View.VISIBLE);

                        if(task.isSuccessful()){
                            // send message
                            sendMessage("entage_page_order_completed", null);

                            for(ItemOrder itemOrder : order.getItem_orders().values()){
                                refItems_on_order.child(itemOrder.getItem_id()).child(order.getOrder_id()).removeValue();
                            }

                            order.setStatus(mContext.getString(R.string.status_order_completed));
                            adapterChats.setOrder_status(mContext.getString(R.string.dbname_orders_completed));

                            // disable all button
                            onClickListener();

                            if(labelOptionsTextBar.getVisibility() == View.VISIBLE){
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                        }else {
                            orderCompleted.setClickable(true);
                            view.findViewById(R.id.progressBar_order_completed).setVisibility(View.GONE);
                            view.findViewById(R.id.img_order_completed).setVisibility(View.VISIBLE);
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                        }

                    }
                });
    }

    private void sendMyLocation(){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_get_my_location, null);

        final TextView no_addresses, my_address_1, my_address_2, manage_my_addresses;
        no_addresses = _view.findViewById(R.id.no_addresses);
        my_address_1 = _view.findViewById(R.id.my_address_1);
        my_address_2 = _view.findViewById(R.id.my_address_2);
        manage_my_addresses = _view.findViewById(R.id.manage_my_addresses);

        LinearLayout get_my_current_location = _view.findViewById(R.id.get_my_current_location);

        //
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        manage_my_addresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                myAddresses = null;
                mOnActivityListener.onActivityListener(new FragmentMyAddresses());
            }
        });

        get_my_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

                if(UtilitiesMethods.checkPermissionsLocations(mContext)){
                    if(isLocationEnabled(mContext)){
                        Intent intent = new Intent(mContext, SharingLocation.class);
                        startActivityForResult(intent, LOCATION_REQUEST_CODE);
                    }else {
                        openGPS();
                    }
                }
            }
        });

        my_address_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myAddresses != null && myAddresses.size() >0){
                    alertDialog.dismiss();

                    MyAddress _myAddress = myAddresses.get(0);
                    mMessageId.setMessage("my_address");
                    mMessageId.setAddress(_myAddress);

                    if(_myAddress.getLat_lng()!= null){
                        mMessageId.setExtraText1(_myAddress.getLat_lng());
                    }

                    editText.setText(ConversationMessages.getMessageText(mMessageId.getMessage()) + "\n" + convertAddress( _myAddress));
                }
            }
        });

        my_address_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myAddresses != null && myAddresses.size()>1){
                    alertDialog.dismiss();
                    MyAddress _myAddress = myAddresses.get(1);
                    mMessageId.setMessage("my_address");
                    mMessageId.setAddress(_myAddress);

                    if(_myAddress.getLat_lng()!= null){
                        mMessageId.setExtraText1(_myAddress.getLat_lng());
                    }

                    editText.setText(ConversationMessages.getMessageText(mMessageId.getMessage()) + "\n" + convertAddress( _myAddress));
                }
            }
        });

        if(myAddresses == null){
            // get my addresses
            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_addresses))
                    .child(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        myAddresses = new ArrayList<>();
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            myAddresses.add(snapshot.getValue(MyAddress.class));
                        }

                        ((RelativeLayout)no_addresses.getParent()).setVisibility(View.GONE);
                        if(mContext != null){
                            my_address_1.setText(myAddresses.get(0).getTitle());
                            my_address_1.setVisibility(View.VISIBLE);
                            if(myAddresses.size()>1){
                                my_address_2.setText(myAddresses.get(1).getTitle());
                                my_address_2.setVisibility(View.VISIBLE);
                            }
                        }else {
                            myAddresses = null;
                        }

                    }else {
                        ((RelativeLayout)no_addresses.getParent()).getChildAt(0).setVisibility(View.GONE);
                        no_addresses.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    ((RelativeLayout)no_addresses.getParent()).getChildAt(0).setVisibility(View.GONE);
                }
            });
        }else {
            ((RelativeLayout)no_addresses.getParent()).setVisibility(View.GONE);
            my_address_1.setText(myAddresses.get(0).getTitle());
            my_address_1.setVisibility(View.VISIBLE);
            if(myAddresses.size()>1){
                my_address_2.setText(myAddresses.get(1).getTitle());
                my_address_2.setVisibility(View.VISIBLE);
            }
        }

        _view.findViewById(R.id.layout_location_address_receiving_order).setVisibility(View.VISIBLE);
        if(order != null){ // may be order is refreshing
            ArrayList<ItemOrder> arrayList = new ArrayList<>(order.getItem_orders().values());
            if(order.getPayment_method().equals(mContext.getString(R.string.payment_method_wr))){
                final ReceivingLocation receivingLocation =  (order.getLocation_selected()!=null? order.getLocation_selected() :
                        arrayList.get(0).getLocation_selected());
                Log.d(TAG, "sendMyLocation: " + arrayList.get(0).toString());

                if(receivingLocation != null){
                    TextView order_address_1 = _view.findViewById(R.id.order_address_1);
                    order_address_1.setVisibility(View.VISIBLE);
                    order_address_1.setText(receivingLocation.getTitle());
                    order_address_1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();

                            MyAddress _myAddress = new MyAddress(receivingLocation.getTitle(), receivingLocation.getLat_lng(),
                                    receivingLocation.getLocation(), receivingLocation.getCountry().getName_area(),
                                    new LocationInformation(user_id, receivingLocation.getCity().getName_area(),
                                            receivingLocation.getCity().getId_area(), null, null,
                                            receivingLocation.getCountry().getCountry_code(),
                                            receivingLocation.getCountry().getId_area(), null),
                                    receivingLocation.getAddress(),  receivingLocation.getPhone_number_1(),  receivingLocation.getPhone_number_2());

                            if(receivingLocation.isBy_google_map() && receivingLocation.getLat_lng()!= null){
                                mMessageId.setExtraText1(receivingLocation.getLat_lng());
                            }

                            mMessageId.setMessage("receiving_order_address");
                            mMessageId.setAddress(_myAddress);

                            editText.setText(ConversationMessages.getMessageText(mMessageId.getMessage())+"\n"+convertAddress(_myAddress));
                        }
                    });
                }else {
                    _view.findViewById(R.id.no_address).setVisibility(View.VISIBLE);
                }
            }else {
                _view.findViewById(R.id.no_address).setVisibility(View.VISIBLE);
            }
        }

        alertDialog.show();
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    private void openGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.open_gps_to_send));
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.open_gps), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.create().show();
    }

    private String convertAddress(MyAddress _myAddress){
        return mContext.getString(R.string.select_country) + " " + _myAddress.getCountry() +
                "\n" + mContext.getString(R.string.the_city) + " " + _myAddress.getCity().getCity_name() +
                (_myAddress.getLocation() != null ?
                        "\n" + mContext.getString(R.string.the_location) + ": " + _myAddress.getLocation() : "") +
                (_myAddress.getAddress_home() != null ?
                        "\n" + mContext.getString(R.string.description_address_1) + ": " + _myAddress.getAddress_home() : "") +
                "\n" + mContext.getString(R.string.the_phone_number) + " " + _myAddress.getPhone_number();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(adapterChats != null){
            Log.d(TAG, "onDestroy: ");
            adapterChats.removeListeners();
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
        */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefOrder =  mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_order_conversation))
                .child(entagePageId)
                .child(orderId);
        myRefOrderConversation = myRefOrder
                .child("chats");
        myRefStatus = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_order_conversation_status))
                .child(entagePageId)
                .child(orderId);

        user_id = mAuth.getCurrentUser().getUid();

        if(mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isAnonymous()){
            if(order!= null){
                addListener = true;
                fetchCurrencyEntagePage();
            }
            else {
                addListener = true;
                fetchOrder(false);
            }
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "SignIn : Uid:  " + user.getUid());
                    if(user.isAnonymous()){
                        Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }else {
                        presence(); // we use this for some cases
                    }
                }else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    Log.d(TAG, "SignOut");
                }
            }
        };
    }

    @Override
    public void onStart() {
        setStatus(true);
        super.onStart();
        if( mAuthListener!= null){
            mAuth.addAuthStateListener(mAuthListener);
        }
        if(myRefOrderConversation != null && childEventListenerConversation!=null){
            myRefOrderConversation.addChildEventListener(childEventListenerConversation);
        }
        if(myRefOnlineOrder != null){
            myRefOnlineOrder.addValueEventListener(valueEventListenerOnlineOrder);
        }
        if(myRefOnlinePage != null){
            myRefOnlinePage.addValueEventListener(valueEventListenerOnlinePage);
        }
    }

    @Override
    public void onStop() {
        setStatus(false);
        super.onStop();
        if( mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if(myRefOrderConversation != null && childEventListenerConversation != null ){
            myRefOrderConversation.removeEventListener(childEventListenerConversation);
        }
        if(myRefOnlineOrder != null){
            myRefOnlineOrder.removeEventListener(valueEventListenerOnlineOrder);
        }
        if(myRefOnlinePage != null){
            myRefOnlinePage.removeEventListener(valueEventListenerOnlinePage);
        }
    }


}
