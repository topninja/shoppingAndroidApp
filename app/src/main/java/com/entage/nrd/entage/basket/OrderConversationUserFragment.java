package com.entage.nrd.entage.basket;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.text.Html;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.EditDataOrder;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Message;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.PaymentClaim;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.utilities_1.SharingLocation;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.FragmentMyAddresses;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterChats;
import com.entage.nrd.entage.adapters.AdapterConversationMessages;
import com.entage.nrd.entage.utilities_1.ConversationMessages;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewDetailsOrderFragment;
import com.entage.nrd.entage.utilities_1.ViewOptionsPrices;
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
import android.location.Address;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class OrderConversationUserFragment extends Fragment {
    private static final String TAG = "OrderConversatio";

    private OnActivityOrderListener mOnActivityOrderListener;
    private OnActivityListener mOnActivityListener;

    private MessageId mMessageId;
    private View view;
    private Context mContext;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final static int PLACE_PICKER_REQUEST = 999;
    private final int LOCATION_REQUEST_CODE = 350;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefOrder, myRefOrderConversation, myRefStatus, myRefOnlinePage, myRefOnlineOrder;
    private ChildEventListener childEventListenerConversation;
    private ValueEventListener valueEventListenerOnlinePage, valueEventListenerOnlineOrder;
    //private Query queryOrderConversation;
    private String lastMessageId;

    private String userTokenId;

    private RelativeLayout layout_new_msg, goToPaymentClim;
    private ImageView send, location;
    private EditText editText;

    private TextView online_page, online_order, new_msg_count;
    private ImageView options, arrow, optionsTextBar;

    private Item item;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private AdapterChats adapterChats;
    private AlertDialog alertMessages;

    private RelativeLayout relLayout_order, label_order, editOrder, orderCompleted, orderCancelling;
    private LinearLayout labelOptionsTextBar;

    private String user_id, entage_page_user_id, orderId, entagePageName, entagePageId, lung, currencyName;
    private Order order;

    private boolean onlinePage, onlineOrder;

    private ArrayList<String> messagesId, unreadMsg;
    private HashMap<String, Message> textMessages;

    private MessageDialog messageDialog = new MessageDialog();

    private ArrayList<MyAddress> myAddresses;
    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_order_conversation, container, false);
            mContext = getActivity();

            ImageView mBackArrow = view.findViewById(R.id.back);
            mBackArrow.setVisibility(View.VISIBLE);
            mBackArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

            getIncomingBundle();
        }

        return view;
    }

    private void getIncomingBundle() {
        try {
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                orderId = bundle.getString("orderId");
                order = bundle.getParcelable("order");
                entagePageName = bundle.getString("entagrPageName");
                entagePageId = bundle.getString("entagePageId");

                if (orderId != null) {
                    if (order != null) {
                        entagePageId = order.getEntage_page_id();
                    }
                    setupFirebaseAuth();
                } else {
                    getActivity().onBackPressed();
                }
            }
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException;" + e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnActivityOrderListener = (OnActivityOrderListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }

        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        try {
            mMessageId = (MessageId) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode:" + requestCode + ", resultCode: " + resultCode +", data: " + data);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == 99) {
            String extra = data.getStringExtra(mContext.getString(R.string.dbname_payments_processes));
            if (extra != null && extra.equals(mContext.getString(R.string.dbname_payments_succeed))) {
                // update payment clime
                Log.d(TAG, "onActivityResult: " + requestCode + ", " + resultCode + ", " + data.getStringExtra("message_id"));
                String messageId = data.getStringExtra("message_id");
                if (messageId != null) {
                    adapterChats.updateMessage(messageId);
                }
            }
        }

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(data != null && data.hasExtra("lat_lng")){
                    String lanLat = data.getStringExtra("lat_lng");
                    Address address =  data.getParcelableExtra("address");

                    if(lanLat != null){
                        mMessageId.setMessage("my_address");

                        if(address != null){
                            mMessageId.setAddress(new MyAddress(address.getAddressLine(0), lanLat, address.getAddressLine(0),
                                    address.getCountryCode(),
                                    new LocationInformation( user_id, address.getSubAdminArea(), address.getPostalCode(),
                                            address.getAdminArea(), null,  address.getCountryCode(), null, address.getPostalCode())
                                    , null,  null,  null));
                        }
                        mMessageId.setExtraText1(lanLat);

                        editText.setText(ConversationMessages.getMessageText(mMessageId.getMessage()) + "\n" + convertAddress(mMessageId.getAddress()));
                    }
                }
            }
        }

    }

    private void initFunctions() {
        getUserTokenId();
        init();
        setupChatRecyclerView();
        setupConversationMessages();
        setupOrdersEventListeners();
        setupStatusEventListeners();

    }

    private void init() {
        // remove notification if exist
        UtilitiesMethods.removeNotification(mContext, order.getOrder_id());


        initWidgets();
        onClickListener();

        setOrderData();
    }

    private void initWidgets() {
        editText = view.findViewById(R.id.text_question);
        editText.setEnabled(false);
        send = view.findViewById(R.id.send);

        arrow = view.findViewById(R.id.arrow);
        label_order = view.findViewById(R.id.label_order);
        relLayout_order = view.findViewById(R.id.relLayout_order);
        orderCompleted = view.findViewById(R.id.order_completed);
        orderCancelling = view.findViewById(R.id.order_cancelled);
        location = view.findViewById(R.id.location);

        optionsTextBar = view.findViewById(R.id.options_text_bar);
        labelOptionsTextBar = view.findViewById(R.id.label_options_text_bar);
        editOrder = view.findViewById(R.id.edit_order);
        ;
        editOrder.setVisibility(View.VISIBLE);

        online_page = view.findViewById(R.id.online_page);
        online_order = view.findViewById(R.id.online_order);

        options = view.findViewById(R.id.options);

        layout_new_msg = view.findViewById(R.id.layout_new_msg);
        new_msg_count = view.findViewById(R.id.new_msg_count);
        unreadMsg = new ArrayList<>();

        goToPaymentClim = view.findViewById(R.id.layout_go_to_payment_clim);

        globalVariable = ((GlobalVariable) mContext.getApplicationContext());
        lung = globalVariable.getLanguage() != null ? globalVariable.getLanguage() : Locale.getDefault().getLanguage();


        /*editText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) { return false; }
        });
        editText.setLongClickable(false);
        editText.setFocusable(false);*/
    }

    private void onClickListener() {

        if (order.getStatus().equals(mContext.getString(R.string.status_order_cancelled)) ||
                order.getStatus().equals(mContext.getString(R.string.status_order_cancelled_on_chatting))) {

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
            editOrder.setOnClickListener(onClickListener);
            orderCompleted.setOnClickListener(onClickListener);
            orderCancelling.setOnClickListener(onClickListener);

        }
        else if (order.getStatus().equals(mContext.getString(R.string.status_order_completed))) {
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
            editOrder.setOnClickListener(onClickListener);
            orderCompleted.setOnClickListener(onClickListener);
            orderCancelling.setOnClickListener(onClickListener);
        }
        else {
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    if (arrow.getRotationX() == 180) {
                        arrow.setRotationX(0);
                        UtilitiesMethods.collapse(relLayout_order);
                    }
                    if (mMessageId.getMessage() != null) {
                        sendMessage(mMessageId.getMessage(), null);
                    }
                }
            });

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    if (arrow.getRotationX() == 180) {
                        arrow.setRotationX(0);
                        UtilitiesMethods.collapse(relLayout_order);
                    }
                    if (alertMessages != null) {
                        alertMessages.show();
                    }
                }
            });

            location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    sendMyLocation();
                }
            });

            editOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (order != null) {
                        editOrder.setClickable(false);
                        view.findViewById(R.id.img_edit_order).setVisibility(View.GONE);
                        view.findViewById(R.id.progressBar_edit_order).setVisibility(View.VISIBLE);
                        checkingEditOrder();
                    } else {
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

        optionsTextBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                    UtilitiesMethods.collapse(labelOptionsTextBar);
                    optionsTextBar.setImageResource(R.drawable.ic_three_circles);

                } else {
                    if (arrow.getRotationX() == 180) {
                        arrow.setRotationX(0);
                        UtilitiesMethods.collapse(relLayout_order);
                    }

                    UtilitiesMethods.expand(labelOptionsTextBar);
                    optionsTextBar.setImageResource(R.drawable.ic_x);
                }
            }
        });

        layout_new_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToPosition();
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMenuOptions();
            }
        });
    }

    //
    private void fetchOrder(final boolean isRefreshing) {
        //  we dont know where order is
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            order = dataSnapshot.getValue(Order.class);
                            orderFetched(isRefreshing);

                            // these function not work in case order == null
                            presence();
                            setStatus(true);
                        } else {
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_orders))
                                    .child(mContext.getString(R.string.dbname_orders_cancelled))
                                    .child(orderId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                order = dataSnapshot.getValue(Order.class);
                                                orderFetched(isRefreshing);
                                            } else {
                                                mFirebaseDatabase.getReference()
                                                        .child(mContext.getString(R.string.dbname_orders))
                                                        .child(mContext.getString(R.string.dbname_orders_completed))
                                                        .child(orderId)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {
                                                                    order = dataSnapshot.getValue(Order.class);
                                                                    orderFetched(isRefreshing);
                                                                } else {
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

    private void orderFetched(boolean isRefreshing) {
        orderId = order.getOrder_id();
        entagePageId = order.getEntage_page_id();
        if (!isRefreshing) {
            fetchCurrencyEntagePage();
        }
    }

    private void fetchCurrencyEntagePage() {
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entagePageId)
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

    private void checkingEditOrder() {
        // check if user already paid
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(orderId)
                .child("is_paid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && (boolean) dataSnapshot.getValue()) {
                            editOrder.setClickable(true);
                            view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                            view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation)
                                    , mContext.getString(R.string.payment_claim_paid));
                        } else {
                            // check if there is no requests payment claim
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_requests_payment_claim))
                                    .child(entagePageId)
                                    .child(orderId)
                                    .orderByChild("refused")
                                    .equalTo(false)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                    PaymentClaim paymentClaim = snapshot.getValue(PaymentClaim.class);

                                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation)
                                                            , mContext.getString(R.string.replay_for_payment_claim_first));
                                                    int i = messagesId.indexOf(paymentClaim.getMessage_id());
                                                    if (i != -1) {
                                                        recyclerView.smoothScrollToPosition(i);
                                                    }
                                                    editOrder.setClickable(true);
                                                    view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                                                    view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                                                    break;
                                                }

                                            } else {
                                                mFirebaseDatabase.getReference()
                                                        .child(mContext.getString(R.string.dbname_requests_edit_order))
                                                        .child(entagePageId)
                                                        .child(orderId)
                                                        .orderByChild("refused")
                                                        .equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                EditDataOrder editDataOrder = snapshot.getValue(EditDataOrder.class);

                                                                messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation)
                                                                        , mContext.getString(R.string.edit_order_running));
                                                                int i = messagesId.indexOf(editDataOrder.getMessage_id());
                                                                if (i != -1) {
                                                                    recyclerView.smoothScrollToPosition(i);
                                                                }
                                                                editOrder.setClickable(true);
                                                                view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                                                                view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                                                                break;
                                                            }

                                                        } else {
                                                            if (arrow.getRotationX() == 180) {
                                                                arrow.setRotationX(0);
                                                                UtilitiesMethods.collapse(relLayout_order);
                                                            }
                                                            fetchItems();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        editOrder.setClickable(true);
                                                        view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                                                        view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                                                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            editOrder.setClickable(true);
                                            view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                                            view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        editOrder.setClickable(true);
                        view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                        view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    //
    private void setOrderData() {
        entagePageName = order.getStore_name();

        ((TextView) view.findViewById(R.id.number_order)).setText(order.getOrder_number() + "");
        ((TextView) view.findViewById(R.id.the_store)).setText(entagePageName);
        ((TextView) view.findViewById(R.id.order_date)).setText(DateTime.convertToSimple(order.getTime_order()) + "  " +
                DateTime.getTimeFromDate(order.getTime_order()));

        final StringBuilder body = new StringBuilder();
        final TextView items_names = view.findViewById(R.id.items_names);

        for (Map.Entry<String, ItemOrder> map : order.getItem_orders().entrySet()) {
            body.append("- ").append(map.getValue().getItem_name())
                    .append(StringManipulation.printArrayListItemsOrder(map.getValue().getOptions()))
                    .append('\n');
            items_names.setText(body.toString().trim());
        }

        ((TextView) view.findViewById(R.id.username)).setText(mContext.getString(R.string.store) + " " + (entagePageName != null ? entagePageName : ""));

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
                if (relLayout_order.getVisibility() == View.VISIBLE) {
                    UtilitiesMethods.collapse(relLayout_order);
                    arrow.setRotationX(0);
                } else {
                    if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                        UtilitiesMethods.collapse(labelOptionsTextBar);
                        optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                    }

                    UtilitiesMethods.expand(relLayout_order);
                    arrow.setRotationX(180);
                }
            }
        });
    }

    // setupChatList
    private void setupChatRecyclerView() {
        ConversationMessages.init(mContext);
        recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messagesId = new ArrayList<>();
        textMessages = new HashMap<>();

        HashMap<String, String> itemsNames = new HashMap<>();
        for (ItemOrder io : order.getItem_orders().values()) {
            itemsNames.put(io.getItem_basket_id(), io.getItem_name());
        }

        adapterChats = new AdapterChats(mContext, messagesId, textMessages, user_id, order.getEntage_page_id(), order.getOrder_id(),
                unreadMsg, layout_new_msg, entagePageName, currencyName, order.getPayment_method(), order.isIs_paid(),
                order.getStatus(), itemsNames, mOnActivityOrderListener, mMessageId);
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
                if (arrow.getRotationX() == 180) {
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
    private void setupConversationMessages() {
        View _view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_conversation_messages, null);


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

        RecyclerView recyclerView = _view.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        final ArrayList<String> cm_ids = ConversationMessages.getMsgIds_user();
        recyclerView.setAdapter(new AdapterConversationMessages(mContext, cm_ids, editText, alertMessages));

        editText.setEnabled(true);
    }

    // setupOrdersEventListeners
    private void setupOrdersEventListeners() {
        childEventListenerConversation = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message _message = dataSnapshot.getValue(Message.class);

                //Log.d(TAG, "onChildAdded: " + _message.getMessage_id());
                if (_message != null) {
                    if (!messagesId.contains(_message.getMessage_id())) {
                        // init
                        int index = messagesId.size();
                        messagesId.add(index, _message.getMessage_id());
                        textMessages.put(_message.getMessage_id(), _message);
                        adapterChats.notifyItemInserted(index);

                        // if this not your message
                        if (!_message.getUser_id().equals(user_id)) {
                            // remove notification if exist
                            UtilitiesMethods.removeNotification(mContext, _message.getMessage_id());

                            if (!_message.isIs_read()) {
                                unreadMsg.add(_message.getMessage_id());

                                new_msg_count.setText(unreadMsg.size() + "");
                                int i = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                                if (index - 1 == i) {
                                    recyclerView.smoothScrollToPosition(index);
                                } else if (index - 1 > i) {
                                    layout_new_msg.setVisibility(View.VISIBLE);
                                }
                            }

                            if (_message.getMessage().equals("entage_page_order_completed")) {
                                // in case this message send and user on this page
                                order.setStatus(mContext.getString(R.string.status_order_completed));
                                adapterChats.setOrder_status(mContext.getString(R.string.dbname_orders_completed));
                                // disable all button
                                onClickListener();
                            }
                        }

                    } else {
                        // if this my message
                        if (_message.getUser_id().equals(user_id)) {
                            if (_message.isIs_read() && !textMessages.get(_message.getMessage_id()).isIs_read()) {
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

                    lastMessageId = _message.getMessage_id();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged: ");
                Message _message = dataSnapshot.getValue(Message.class);

                if (_message != null) {
                    if (_message.isIs_deleted()) {
                        adapterChats.messageDeleted(_message.getMessage_id());

                    } else {
                        // if this my message
                        if (_message.getUser_id().equals(user_id)) {
                            if (_message.isIs_read()) {
                                adapterChats.messageRead(_message.getMessage_id());
                            }
                        }
                    }

                    // check is payment_claiming message
                    if (_message.getMessage().equals("payment_claiming")) {
                        if (_message.getExtra_text_1() != null) { // if there data in extra_text_1, there is replay
                            adapterChats.updateMessagePaymentClim(_message);
                        } else {
                            int index = messagesId.indexOf(_message.getMessage_id());
                            if (index != -1) {
                                adapterChats.notifyItemChanged(index);
                            } else {
                                adapterChats.notifyDataSetChanged();
                            }
                        }
                    }

                    // check is edit_data_order message
                    if (_message.getMessage().equals("edit_data_order")) {
                        if (_message.getExtra_text_1() != null) { // if there data in extra_text_1, there is replay
                            if (_message.getExtra_text_1().equals("edit_data_order_confirmed")) {
                                refreshOrder();
                            }
                            adapterChats.updateMessageEditOrder(_message);
                        } else {
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

        //queryOrderConversation = myRefOrder.child("chats");
        myRefOrderConversation.addChildEventListener(childEventListenerConversation);
    }

    private void setMessageSent(String message_id) {
        myRefOrderConversation
                .child(message_id)
                .child("is_sent")
                .setValue(true);
    }

    // setupStatusEventListeners
    private void setupStatusEventListeners() {
        myRefOnlineOrder = myRefStatus
                .child("status_entage_page");

        valueEventListenerOnlineOrder = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean boo = (boolean) dataSnapshot.getValue();
                    onlineOrder = boo;
                    if (boo) {
                        online_page.setVisibility(View.GONE);
                        online_order.setText(mContext.getString(R.string.online));

                    } else {
                        online_page.setVisibility(View.VISIBLE);
                        online_order.setText("");
                    }
                } else {
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
                .child(mContext.getString(R.string.dbname_order_conversation_status))
                .child(entagePageId)
                .child("status");

        valueEventListenerOnlinePage = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean boo = (boolean) dataSnapshot.getValue();
                    onlinePage = boo;
                    if (boo) {
                        online_page.setVisibility(View.VISIBLE);
                        online_page.setText(mContext.getString(R.string.connect));
                    } else {
                        online_page.setText(mContext.getString(R.string.disconnect));
                    }
                } else {
                    online_page.setText(mContext.getString(R.string.disconnect));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                online_page.setText(mContext.getString(R.string.disconnect));
            }
        };

        myRefOnlineOrder.addValueEventListener(valueEventListenerOnlineOrder);
        myRefOnlinePage.addValueEventListener(valueEventListenerOnlinePage);
    }

    private void setupMenuOptions() {
        View _view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_options, null);
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
                if (position == 0 || position == 1) {
                    FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.orders_problems));
                    bundle.putString("id", orderId);
                    fragmentInformProblem.setArguments(bundle);
                    mOnActivityListener.onActivityListener(fragmentInformProblem);
                }
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    public void sendMessage(final String text, String message_id) {
        if (message_id == null) {
            message_id = myRefOrderConversation.push().getKey();
        }

        if (message_id != null) {
            final Message _message = new Message(message_id, user_id, text, mMessageId.getExtraText1(), mMessageId.getExtraText2(),
                    mMessageId.getAddress(), DateTime.getTimestamp(), lung, false, false, false, false);

            // clear
            mMessageId.setMessage(null);
            mMessageId.setExtraText1(null);
            mMessageId.setExtraText2(null);
            mMessageId.setAddress(null);
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
                    });
        }
    }

    private void sendNotification(String message, String message_id) {
        if (userTokenId != null && !onlineOrder && !onlinePage) {
            String newKey = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_notifications)).push().getKey();
            String title = NotificationsTitles.newMessage(mContext);
            String body = message;

            Notification notification = null;

            notification = new Notification(order.getEntage_page_id(),
                    order.getOrder_id(),
                    userTokenId, "-1", title, body,
                    mContext.getString(R.string.notif_flag_new_message),
                    user_id,
                    entage_page_user_id, "UserToEntagePage", "-1", message_id);

            if (newKey != null) {
                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_notifications))
                        .child(mContext.getString(R.string.field_notification_to_user))
                        .child(newKey)
                        .setValue(notification);
            }
        }

        if (userTokenId == null) {
            getUserTokenId();
        }
    }

    private void presence() {
        if (order != null && order.getStatus().equals(mContext.getString(R.string.status_order_confirm))) {
            final DatabaseReference firebaseDatabase = myRefStatus
                    .child("status_user");

            final DatabaseReference lastOnlineRef = myRefStatus
                    .child("last_online_user");

            firebaseDatabase.setValue(true);

            // When this device disconnects, set value false
            firebaseDatabase.onDisconnect().setValue(false);

            // When I disconnect, update the last time I was seen online
            lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
        }

        /*final DatabaseReference connectedRef = mFirebaseDatabase.getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "onDataChange: connected: " + connected);
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

    private void getUserTokenId() {
        // get id user from database
        Query query1 = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entagePageId)
                .child("users_ids");
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    entage_page_user_id = ((ArrayList<String>) dataSnapshot.getValue()).get(0);
                    if (entage_page_user_id != null) {
                        Query query = mFirebaseDatabase.getReference()
                                .child(mContext.getString(R.string.dbname_users_token))
                                .child(entage_page_user_id);

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    userTokenId = dataSnapshot.getValue(String.class);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: query cancelled");
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void setStatus(boolean boo) {
        if (order != null && order.getStatus().equals(mContext.getString(R.string.status_order_confirm))) {
            // user
            myRefStatus.child("status_user")
                    .setValue(boo);
        }
    }

    private void scrollToPosition() {
        //Log.d(TAG, "scrollToPosition: " + messagesId.size()+ ", "+ unreadMsg.size());
        if (unreadMsg.size() != 0) {
            recyclerView.smoothScrollToPosition(messagesId.size() - unreadMsg.size());
        }
    }

    //
    private void fetchItems() {
        final HashMap<String, ItemShortData> items = new HashMap<>();

        int i = 0;
        for (Map.Entry<String, ItemOrder> map : order.getItem_orders().entrySet()) {
            ItemOrder itemOrder = map.getValue();

            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_items))
                    .child(itemOrder.getItem_id());

            final int finalI = i;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        ItemShortData itemShortData = dataSnapshot.getValue(ItemShortData.class);

                        items.put(itemShortData.getItem_id(), itemShortData);
                    }

                    if (finalI + 1 == order.getItem_orders().size()) {
                        editOrder.setClickable(true);
                        view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                        view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);

                        editOrderDialog(items);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (finalI + 1 == order.getItem_orders().size()) {
                        editOrder.setClickable(true);
                        view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                        view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                }
            });
            i++;
        }
    }

    private void editOrderDialog(final HashMap<String, ItemShortData> items) {
        View dialog_view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_edit_order, null);
        LinearLayout container = dialog_view.findViewById(R.id.container);
        final TextView error = dialog_view.findViewById(R.id.error);
        final TextView edit_order = dialog_view.findViewById(R.id.edit_order);

        final HashMap<String, ItemOrder> itemOrders = new HashMap<>();
        final HashMap<String, EditText> quantities = new HashMap<>();
        final HashMap<String, ViewOptionsPrices> viewOptionsPrices = new HashMap<>();
        final HashMap<String, String> itemsNames = new HashMap<>();

        for (ItemOrder io : order.getItem_orders().values()) {
            itemsNames.put(io.getItem_basket_id(), io.getItem_name());
        }

        for (Map.Entry<String, ItemOrder> map : order.getItem_orders().entrySet()) {
            ItemOrder itemOrder = map.getValue();
            ItemShortData itemShortData = items.get(itemOrder.getItem_id());
            itemOrders.put(itemOrder.getItem_basket_id(), itemOrder);

            if (isAdded() && (Activity) mContext != null) { // to avid this exp : cannot be executed until the Fragment is attached to the FragmentManager.
                View _view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.layout_editing_order, null);

                ((TextView) _view.findViewById(R.id.itemName)).setText(itemOrder.getItem_name());
                final EditText quantity = _view.findViewById(R.id.quantity);
                quantity.setText(itemOrder.getQuantity() + "");
                quantities.put(itemOrder.getItem_basket_id(), quantity);

                if (itemOrder.getOptions() != null) {
                    if (itemShortData.getOptions_prices().getLinkingOptions() == null ||
                            !itemShortData.getOptions_prices().getLinkingOptions().contains(itemOrder.getOptions())) {
                        ((TextView) _view.findViewById(R.id.error_item_option)).setText(mContext.getString(R.string.data_item_had_changed_order));
                        _view.findViewById(R.id.error_item_option).setVisibility(View.VISIBLE);
                    }
                }

                viewOptionsPrices.put(itemOrder.getItem_basket_id(), new ViewOptionsPrices(mContext, itemShortData.getOptions_prices(),
                        (LinearLayout) _view.findViewById(R.id.container_options),
                        (RelativeLayout) _view.findViewById(R.id.layout_price), itemOrder.getOptions(), 14, 16,
                        mContext.getColor(R.color.entage_blue), globalVariable));

                container.addView(_view);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialog_view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);


        edit_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                error.setVisibility(View.GONE);

                //checking
                final ArrayList<String> item_editOrder = new ArrayList<>();
                for (Map.Entry<String, ItemOrder> map : order.getItem_orders().entrySet()) {
                    String id = map.getKey();
                    ItemOrder itemOrder = map.getValue();

                    //checking quantities
                    if (quantities.get(id).getText().length() == 0 || quantities.get(id).getText().charAt(0) == '0') {
                        error.setText(mContext.getString(R.string.error_quantity));
                        error.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        if (itemOrder.getQuantity() != Integer.parseInt(quantities.get(id).getText().toString())) {
                            item_editOrder.add(id);
                        }
                    }

                    //checking options
                    if (itemOrder.getOptions() != null && !itemOrder.getOptions()
                            .equals(viewOptionsPrices.get(id).getSelectedOptions())) {
                        if (!item_editOrder.contains(id)) {
                            item_editOrder.add(id);
                        }
                    }

                }

                if (item_editOrder.size() == 0) {
                    error.setText(mContext.getString(R.string.you_not_change));
                    error.setVisibility(View.VISIBLE);
                    return;
                }

                //
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
                builder.setTitle(mContext.getString(R.string.edit_order));

                String textOld = "<b> <font color=\"red\">" + mContext.getString(R.string.the_old_order) + "</font> </b>";
                String textNew = "<br><br> <b> <font color=\"blue\">" + mContext.getString(R.string.the_new_order) + "</font> </b>";
                for (String id : item_editOrder) {
                    textOld += "<br><br> - " + itemsNames.get(id) + " <br>" + mContext.getString(R.string.quantity) + ": " +
                            itemOrders.get(id).getQuantity() + "<br>" +
                            (itemOrders.get(id).getOptions() != null ? mContext.getString(R.string.options_item) + ": " +
                                    itemOrders.get(id).getOptions().toString() : "") + "<br>" +
                            mContext.getString(R.string.the_amount) + " " + PaymentsUtil.print(PaymentsUtil.multiply(
                            Integer.toString(itemOrders.get(id).getQuantity()), itemOrders.get(id).getItem_price()));

                    textNew += "<br><br> - " + itemsNames.get(id) + " <br>" + mContext.getString(R.string.quantity) + ": " +
                            quantities.get(id).getText() + "<br>" +
                            (viewOptionsPrices.get(id).getSelectedOptions() != null &&
                                    viewOptionsPrices.get(id).getSelectedOptions().size() > 0 ?
                                    mContext.getString(R.string.options_item) + ": " + viewOptionsPrices.get(id).getSelectedOptions().toString() :
                                    "") + "<br>" +
                            mContext.getString(R.string.the_amount) + " " + PaymentsUtil.print(PaymentsUtil.multiply(
                            quantities.get(id).getText().toString(), viewOptionsPrices.get(id).getmPrice()));
                }
                builder.setMessage(Html.fromHtml(textOld + textNew));

                builder.setPositiveButton(mContext.getString(R.string.edit_order), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                        dialog.dismiss();

                        HashMap<String, ItemOrder> old_item_orders = new HashMap<>();
                        HashMap<String, ItemOrder> new_item_orders = new HashMap<>();
                        for (String id : item_editOrder) {
                            ItemOrder itemOrder = itemOrders.get(id).copy();
                            itemOrder.setQuantity(Integer.parseInt(quantities.get(id).getText().toString()));
                            if (itemOrder.getOptions() != null) {
                                itemOrder.setOptions(viewOptionsPrices.get(id).getSelectedOptions());
                                itemOrder.setItem_price(viewOptionsPrices.get(id).getmPrice());
                            }
                            old_item_orders.put(id, itemOrders.get(id));
                            new_item_orders.put(id, itemOrder);
                        }
                        editOrder(new EditDataOrder(old_item_orders, new_item_orders, DateTime.getTimestamp(),
                                false, false, null, null, null));
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

        dialog_view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void editOrder(final EditDataOrder editDataOrder) {
        editOrder.setClickable(false);
        view.findViewById(R.id.progressBar_edit_order).setVisibility(View.VISIBLE);
        view.findViewById(R.id.img_edit_order).setVisibility(View.GONE);

        final String message_id = myRefOrderConversation.push().getKey();

        if (message_id != null) {
            editDataOrder.setMessage_id(message_id);

            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_requests_edit_order))
                    .child(entagePageId)
                    .child(orderId)
                    .child(message_id)
                    .setValue(editDataOrder)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                adapterChats.setEditDataOrder(message_id, editDataOrder);

                                // send message
                                sendMessage("edit_data_order", message_id);

                            } else {
                                // Firebase Database error: Permission denied
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            }

                            if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                            editOrder.setClickable(true);
                            view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
                            view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            editOrder.setClickable(true);
            view.findViewById(R.id.progressBar_edit_order).setVisibility(View.GONE);
            view.findViewById(R.id.img_edit_order).setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
        }
    }

    public void refreshOrder() {
        //order = null;
        fetchOrder(true);
    }

    // orderCompleted
    private void orderCompleted() {
        if (order.getPayment_method().equals(mContext.getString(R.string.payment_method_wr))) {
            confirmOrderCompleted();
        } else if (order.getPayment_method().equals(mContext.getString(R.string.payment_method_bs))) {
            checkIfUserPaid_Completed();
        }
    }

    private void checkIfUserPaid_Completed() {
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

                        if (dataSnapshot.exists() && (boolean) dataSnapshot.getValue()) {
                            confirmOrderCompleted();
                        } else {
                            if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                            messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation),
                                    mContext.getString(R.string.you_dont_pay_order_amount));
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

    private void confirmOrderCompleted() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.order_is_completed));
        builder.setMessage(mContext.getString(R.string.confirming_completing_order));

        builder.setNegativeButton(mContext.getString(R.string.exit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.order_is_completed), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                orderCompleted_done();
            }
        });
        builder.show();
    }

    private void orderCompleted_done() {
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

                        if (task.isSuccessful()) {
                            // send message
                            //mMessageId.setExtraText1("customer_order_completed");
                            sendMessage("customer_order_completed", null);

                            for (ItemOrder itemOrder : order.getItem_orders().values()) {
                                refItems_on_order.child(itemOrder.getItem_id()).child(order.getOrder_id()).removeValue();
                            }

                            order.setStatus(mContext.getString(R.string.status_order_completed));
                            adapterChats.setOrder_status(mContext.getString(R.string.dbname_orders_completed));
                            // disable all button
                            onClickListener();

                            if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                        } else {
                            orderCompleted.setClickable(true);
                            view.findViewById(R.id.progressBar_order_completed).setVisibility(View.GONE);
                            view.findViewById(R.id.img_order_completed).setVisibility(View.VISIBLE);
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                        }
                    }
                });
    }

    // orderCancelled
    private void orderCancelled() {
        if (order.getPayment_method().equals(mContext.getString(R.string.payment_method_wr))) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setTitle(mContext.getString(R.string.cancelling_order));
            builder.setMessage(mContext.getString(R.string.confirming_cancelling_order));

            builder.setNegativeButton(mContext.getString(R.string.exit), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.setPositiveButton(mContext.getString(R.string.cancel_my_order), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    confirmCancelOrder();
                }
            });
            builder.show();


        } else if (order.getPayment_method().equals(mContext.getString(R.string.payment_method_bs))) {
            orderCancelling.setClickable(false);
            view.findViewById(R.id.img_order_cancelled).setVisibility(View.GONE);
            view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.VISIBLE);

            //
            // check if user has paid
            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_requests_payment_claim))
                    .child(entagePageId)
                    .child(orderId)
                    .orderByChild("is_paid")
                    .equalTo(true)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            orderCancelling.setClickable(true);
                            view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.GONE);
                            view.findViewById(R.id.img_order_cancelled).setVisibility(View.VISIBLE);

                            if (dataSnapshot.exists()) {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation),
                                        mContext.getString(R.string.payment_claim_paid));
                                if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                                    UtilitiesMethods.collapse(labelOptionsTextBar);
                                    optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                                }

                            } else {
                                checkIfUserPaid_CancellingOrder();
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
    }

    private void checkIfUserPaid_CancellingOrder() {
        // check if there is no requests payment claim
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_requests_payment_claim))
                .child(entagePageId)
                .child(orderId)
                .orderByChild("refused")
                .equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            orderCancelling.setClickable(true);
                            view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.GONE);
                            view.findViewById(R.id.img_order_cancelled).setVisibility(View.VISIBLE);
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.you_cant_do_this_operation)
                                    , mContext.getString(R.string.replay_for_payment_claim_first_1));

                            if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                        } else {
                            confirmCancelOrder();
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

    private void confirmCancelOrder() {
        final View dialogView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_status_order, null);

        ((TextView) dialogView.findViewById(R.id.title)).setText(mContext.getString(R.string.cancel_order));
        dialogView.findViewById(R.id.title).setVisibility(View.VISIBLE);

        ((TextView) dialogView.findViewById(R.id.text)).setText("\n" + mContext.getString(R.string.reason_for_cancellation));

        final EditText editText = dialogView.findViewById(R.id.edit_text);
        editText.setHint(mContext.getString(R.string.reason_for_cancellation));
        editText.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialogView);
        builder.setPositiveButton(mContext.getString(R.string.cancel_my_order), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelOrder(editText.getText() != null && editText.getText().length() > 0 ?
                        editText.getText().toString() : mContext.getString(R.string.no_reason));
            }
        });

        builder.setNegativeButton(mContext.getString(R.string.exit), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private void cancelOrder(final String reason_for_cancellation) {
        orderCancelling.setClickable(false);
        view.findViewById(R.id.img_order_cancelled).setVisibility(View.GONE);
        view.findViewById(R.id.progressBar_order_cancelled).setVisibility(View.VISIBLE);

        final DatabaseReference refItems_on_order = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items_on_order));

        Order _order = order.copy();
        _order.setCancelled_by_1(user_id);
        _order.setCancelled_by_2("customer");
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

                        if (task.isSuccessful()) {
                            moveOrderTo_Cancelled(orderId);

                            for (ItemOrder itemOrder : order.getItem_orders().values()) {
                                refItems_on_order.child(itemOrder.getItem_id()).child(order.getOrder_id()).removeValue();
                            }

                            mMessageId.setOrderId(orderId, "update");

                            adapterChats.setOrder_status(mContext.getString(R.string.status_order_cancelled_on_chatting));
                            order.setStatus(mContext.getString(R.string.status_order_cancelled_on_chatting));

                            // disable all button
                            onClickListener();

                            // send message
                            mMessageId.setExtraText1(reason_for_cancellation);
                            sendMessage("customer_canceled_order", null);

                            // set order id to orders list --> very important step
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_orders))
                                    .child(mContext.getString(R.string.dbname_orders_list))
                                    .child(order.getOrder_id())
                                    .setValue("not_working");

                            if (labelOptionsTextBar.getVisibility() == View.VISIBLE) {
                                UtilitiesMethods.collapse(labelOptionsTextBar);
                                optionsTextBar.setImageResource(R.drawable.ic_three_circles);
                            }

                        } else {
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                        }
                    }
                });
    }

    // this function same in adapterOrder (deleteOrderFromDb_CanceledByCustomer)
    private void moveOrderTo_Cancelled(String orderId) {
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
                .setValue(user_id);

        // set order id in cancelled for user
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_orders))
                .child(user_id)
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
                .child(user_id)
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(order.getOrder_id())
                .removeValue();
    }

    private void sendMyLocation() {
        View _view = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_get_my_location, null);

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
                if (myAddresses != null && myAddresses.size() > 0) {
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
                if (myAddresses != null && myAddresses.size() > 1) {
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

        if (myAddresses == null) {
            // get my addresses
            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_addresses))
                    .child(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        myAddresses = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            myAddresses.add(snapshot.getValue(MyAddress.class));
                        }

                        ((RelativeLayout) no_addresses.getParent()).setVisibility(View.GONE);
                        if (mContext != null) {
                            my_address_1.setText(myAddresses.get(0).getTitle());
                            my_address_1.setVisibility(View.VISIBLE);
                            if (myAddresses.size() > 1) {
                                my_address_2.setText(myAddresses.get(1).getTitle());
                                my_address_2.setVisibility(View.VISIBLE);
                            }
                        } else {
                            myAddresses = null;
                        }

                    } else {
                        ((RelativeLayout) no_addresses.getParent()).getChildAt(0).setVisibility(View.GONE);
                        no_addresses.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    ((RelativeLayout) no_addresses.getParent()).getChildAt(0).setVisibility(View.GONE);
                }
            });
        } else {
            ((RelativeLayout) no_addresses.getParent()).setVisibility(View.GONE);
            my_address_1.setText(myAddresses.get(0).getTitle());
            my_address_1.setVisibility(View.VISIBLE);
            if (myAddresses.size() > 1) {
                my_address_2.setText(myAddresses.get(1).getTitle());
                my_address_2.setVisibility(View.VISIBLE);
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
        builder.setMessage(mContext.getString(R.string.open_gps_to_send));
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

    /*
        -------------------------------Firebase-------------------------------------------------------
         */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //myRef = mFirebaseDatabase.getReference();
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
                fetchCurrencyEntagePage();
            }
            else {
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
            /*// update to keep starting listen from last key in case onStop
            if(lastMessageId != null){
                queryOrderConversation = myRefOrder
                        .child("chats")
                        .orderByKey()
                        .startAt(lastMessageId);
            }*/
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
        if(mAuthListener!= null){
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
