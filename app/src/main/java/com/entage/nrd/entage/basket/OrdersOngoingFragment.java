package com.entage.nrd.entage.basket;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterOrders;
import com.entage.nrd.entage.adapters.AdapterOrdersEntagePage;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static android.content.Context.MODE_PRIVATE;

public class OrdersOngoingFragment extends Fragment {
    private static final String TAG = "EntagePageOrdersFragmen";

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private Spinner spinnerSorting;
    private MessageId mMessageId;

    private OnActivityOrderListener mOnActivityOrderListener;
    private OnActivityListener mOnActivityListener;

    private HashMap<String, Query> dbQueriesChats;
    private HashMap<String, ChildEventListener> childEventListeners;
    private HashMap<String, String> lastKeys;

    private DatabaseReference referenceChatsConversation;
    private Query queryOrdersList;
    private ValueEventListener listenerOrdersList;

    private View view;
    private Context mContext;
    private RecyclerView recyclerView;
    private TextView orders_count;
    private int ordersCount = 0;


    private String entagePage_id, entagePageName, ordersType;

    private AdapterOrdersEntagePage adapterOrdersEntagePage;
    private AdapterOrders adapterOrders;
    private ArrayList<Order> orders;
    private HashMap<String, Date> dateLastMessageInOrders;
    private HashMap<String, Order> itemOrderByOrderId;
    private ArrayList<String> ordersIds;

    private boolean firstSetupDone = false; // for comparing between orders

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_orders_page, container , false);
            mContext = getActivity();

            getIncomingBundle();

            setupFirebaseAuth();

            init();
        }

        else {

        }

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePage_id =  bundle.getString("entagePageId");
                entagePageName = bundle.getString("entagePageName");
                user_id = bundle.getString("user_id");
            }
        }
        catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnActivityOrderListener = (OnActivityOrderListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        try{
            mMessageId = (MessageId) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityOrderListener = null;
        mOnActivityListener = null;
    }

    private void init() {
        initWidgets();
        onClickListener();

        setupAdapter();
        //setupSpinner();

        if(entagePage_id != null || user_id !=null){
            setupOrdersListListener();
        }
    }

    private void initWidgets(){
        spinnerSorting = view.findViewById(R.id.spinnerSorting);

        dbQueriesChats = new HashMap<>();
        childEventListeners = new HashMap<>();
        lastKeys = new HashMap<>();

        ordersType = mContext.getString(R.string.dbname_orders_ongoing);

        ((TextView)view.findViewById(R.id.orders_count_text)).setText(mContext.getString(R.string.number_orders)+
                " " +mContext.getString(R.string.text_the_order_ongoing));
        orders_count = view.findViewById(R.id.orders_count);
    }

    private void onClickListener(){
    }

    private void setupAdapter(){
        orders = new ArrayList<>();
        ordersIds = new ArrayList<>();
        dateLastMessageInOrders = new HashMap<>();
        itemOrderByOrderId = new HashMap<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        orders.add(null);orders.add(null);orders.add(null);
        orders.add(null);orders.add(null);orders.add(null);
        orders.add(null);orders.add(null);orders.add(null);

        if(entagePage_id != null){
            adapterOrdersEntagePage = new AdapterOrdersEntagePage(mContext, recyclerView, orders, dateLastMessageInOrders, itemOrderByOrderId,
                    user_id, mOnActivityOrderListener, mOnActivityListener);
            recyclerView.setAdapter(adapterOrdersEntagePage);
        }
        else {
            adapterOrders = new AdapterOrders(mContext, recyclerView, orders, dateLastMessageInOrders, itemOrderByOrderId, user_id, mOnActivityOrderListener, mOnActivityListener);
            recyclerView.setAdapter(adapterOrders);
        }
    }

    private void setupSpinner(){
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(mContext.getString(R.string.sorting_default));
        //arrayList.add(mContext.getString(R.string.sorting_order_date));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSorting.setAdapter(adapter);
        spinnerSorting.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    // fetch Ids Orders and keep listen
    private void setupOrdersListListener(){
        if(entagePage_id != null){
            queryOrdersList = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_entage_page_orders))
                    .child(entagePage_id)
                    .child(ordersType);
        }
        else {
            queryOrdersList = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_users_orders))
                    .child(user_id)
                    .child(ordersType);
        }

        listenerOrdersList = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> temp = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String orderId = snapshot.getKey();
                        if (!ordersIds.contains(orderId)) {
                            ordersIds.add(orderId);
                            fetchOrder(orderId);
                        }
                        temp.add(orderId);
                    }
                }
                else {
                    removeProgressBar();
                    ((RelativeLayout)orders_count.getParent()).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    ((TextView)view.findViewById(R.id.basket_empty)).setText(mContext.getString(R.string.order_list_empty)+
                            " " +mContext.getString(R.string.text_order_ongoing));
                    view.findViewById(R.id.basket_empty).setVisibility(View.VISIBLE);
                }

                // this in case user cancel order in conversation page
                for(String orderId : ordersIds){
                    if(!temp.contains(orderId)){ // this order remove from ongoing list
                        deleteOrder(orderId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                removeProgressBar();
                ((TextView)view.findViewById(R.id.basket_empty)).setText(mContext.getString(R.string.happened_wrong_try_again));
                ((TextView)view.findViewById(R.id.basket_empty)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                view.findViewById(R.id.basket_empty).setVisibility(View.VISIBLE);
            }
        };
    }

    private void fetchOrder(String orderId){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_orders))
                .child(ordersType)
                .child(orderId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Order order = dataSnapshot.getValue(Order.class);

                    // remove notification if exist
                    UtilitiesMethods.removeNotification(mContext, order.getOrder_id());

                    // getUnreadMessages
                    getUnreadMessages(order);

                    // getDateLastMessage and start listen from last msg
                    getDateLastMessage(order);

                    ordersCount++;
                    orders_count.setText(ordersCount+"");

                    if(recyclerView.getVisibility() == View.GONE){
                        ((RelativeLayout)orders_count.getParent()).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.basket_empty).setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    removeProgressBar();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUnreadMessages(final Order order){
        final String orderId = order.getOrder_id();

        Query query1 = myRef
                .child(mContext.getString(R.string.dbname_order_conversation))
                .child(order.getEntage_page_id())
                .child(orderId)
                .child("chats")
                .orderByChild("is_read")
                .equalTo(false);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                removeProgressBar();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String writerMessageId = (String) snapshot.child("user_id").getValue();
                        if (!writerMessageId.equals(user_id)) {
                            String message_id = (String) snapshot.child("message_id").getValue();

                            int count = 0;
                            if(adapterOrdersEntagePage != null){
                                count = adapterOrdersEntagePage.addCuntUnreadMessage_withoutComparingDate(orderId, message_id);
                            }
                            if(adapterOrders != null){
                                count = adapterOrders.addCuntUnreadMessage_withoutComparingDate(orderId, message_id);
                            }

                            if(count == 1){
                                if(mOnActivityOrderListener != null){
                                    mOnActivityOrderListener.setCountNewMsg(+1, "ongoing");
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getDateLastMessage(final Order order){
        Query query1 = myRef
                .child(mContext.getString(R.string.dbname_order_conversation))
                .child(order.getEntage_page_id())
                .child(order.getOrder_id())
                .child("chats")
                .orderByKey()
                .limitToLast(1);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                removeProgressBar();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String dateString = (String) snapshot.child("date").getValue();
                        if(dateString != null){
                            Date date = DateTime.getTimestamp(dateString);
                            dateLastMessageInOrders.put(order.getOrder_id(), date);

                            lastKeys.put(order.getOrder_id(), snapshot.getKey());
                            compareDate(order);
                        }
                    }
                }
               /* else { // new order
                    Date date = DateTime.getTimestamp(order.getTime_order());
                    dateLastMessageInOrders.put(order.getOrder_id(), date);
                    compareDate(order);
                }*/
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void compareDate(Order order){
        removeProgressBar();

        int index = -1;
        Date date1 = dateLastMessageInOrders.get(order.getOrder_id());
        for(int i=0; i<orders.size(); i++){
            Date date2 = dateLastMessageInOrders.get(orders.get(i).getOrder_id());
            if(date1.compareTo(date2) > 0){
                index = i;
                break;
            }
        }

        if(index != -1){
            addOrder(index, order);
        }
        else {
            addOrder(orders.size(), order);
        }
    }

    private void addOrder(int index, Order order){
        itemOrderByOrderId.put(order.getOrder_id(), order);
        orders.add(index, order);

        if(adapterOrdersEntagePage != null){
            adapterOrdersEntagePage.notifyDataSetChanged();
        }
        if(adapterOrders != null){
            adapterOrders.notifyDataSetChanged();
        }

        chatsListener_FromLastMsg(order);
    }

    private void chatsListener_FromLastMsg(final Order order){
        final String orderId = order.getOrder_id();

        final DatabaseReference reference = referenceChatsConversation
                .child(order.getEntage_page_id())
                .child(order.getOrder_id())
                .child("chats");

        ChildEventListener childEventListenerChats = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Message _message = dataSnapshot.getValue(Message.class);
                String writerMessageId = (String) dataSnapshot.child("user_id").getValue();
                boolean isRead = (boolean) dataSnapshot.child("is_read").getValue();
                String message_id = (String) dataSnapshot.child("message_id").getValue();

                lastKeys.put(orderId, message_id);

                if (writerMessageId != null ) {
                    // if this not your message
                    //Log.d(TAG, "onChildAdded: " + isRead + ", " + dataSnapshot.child("message").getValue() );
                    if (!writerMessageId.equals(user_id) && !isRead) {
                        String messageText = (String) dataSnapshot.child("message").getValue();
                        String date = (String) dataSnapshot.child("date").getValue();

                        int count = 0;
                        if(adapterOrdersEntagePage != null){
                            count = adapterOrdersEntagePage.addCuntUnreadMessage(orderId, message_id, date);
                        }
                        if(adapterOrders != null){
                            count = adapterOrders.addCuntUnreadMessage(orderId, message_id, date);
                        }
                        if(mOnActivityOrderListener != null){
                            mOnActivityOrderListener.setCountNewMsg(count, "ongoing");
                        }

                    }
                    else if (firstSetupDone){ // for comparing between orders
                        String date = (String) dataSnapshot.child("date").getValue();
                        if(dateLastMessageInOrders.containsKey(orderId) && DateTime.getTimestamp(date)
                                .compareTo(dateLastMessageInOrders.get(orderId)) > 0){
                            if(adapterOrdersEntagePage != null){
                                adapterOrdersEntagePage.compare(orderId, date);
                            }
                            if(adapterOrders != null){
                                adapterOrders.compare(orderId, date);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Message _message = dataSnapshot.getValue(Message.class);
                /*String writerMessageId = (String) dataSnapshot.child("user_id").getValue();

                // if this not your message
                if (writerMessageId != null && !writerMessageId.equals(user_id)) {
                    boolean isRead = (boolean) dataSnapshot.child("is_read").getValue();
                    // if did not read the message ?
                    if(!isRead){
                        boolean isDeleted = (boolean) dataSnapshot.child("is_deleted").getValue();
                        if(isDeleted){

                        }
                    }
                }*/
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved: " );
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        dbQueriesChats.put(orderId, reference.orderByKey().startAt(lastKeys.get(orderId)));
        childEventListeners.put(orderId, childEventListenerChats);

        dbQueriesChats.get(orderId).addChildEventListener(childEventListenerChats);
    }

    private void removeProgressBar(){
        if(orders.size() > 0 && orders.get(0) == null){
            orders.clear();
            if(adapterOrdersEntagePage != null){
                adapterOrdersEntagePage.notifyDataSetChanged();
            }
            if(adapterOrders != null){
                adapterOrders.notifyDataSetChanged();
            }

        }
    }

    private void deleteOrder(String orderId) {
        if(adapterOrders != null || adapterOrdersEntagePage != null){
            if(adapterOrders != null) {
                adapterOrders.deleteOrder(orderId);
            }
            if(adapterOrdersEntagePage != null) {
                adapterOrdersEntagePage.deleteOrder(orderId);
            }

            if(mOnActivityOrderListener != null){
                mOnActivityOrderListener.setCountNewMsg(-1, "ongoing");
                ordersCount--;
                orders_count.setText(ordersCount+"");
            }

            if(dbQueriesChats.containsKey(orderId)){
                dbQueriesChats.get(orderId).removeEventListener(childEventListeners.get(orderId));
                dbQueriesChats.remove(orderId);
            }

            if(orders.size() == 0){
                ((RelativeLayout)orders_count.getParent()).setVisibility(View.GONE);
                view.findViewById(R.id.recyclerView).setVisibility(View.GONE);
                ((TextView)view.findViewById(R.id.basket_empty)).setText(mContext.getString(R.string.order_list_empty));
                view.findViewById(R.id.basket_empty).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(adapterOrdersEntagePage != null){
            adapterOrdersEntagePage.removeListeners();
        }
        if(adapterOrders != null){
            adapterOrders.removeListeners();
        }
    }

/*    @Override
    public void onDestroyView() {
        if (view != null) {
            ViewGroup parentViewGroup = (ViewGroup) view.getParent();
            if(parentViewGroup!=null){
                parentViewGroup.removeAllViews();
            }
        }
        super.onDestroyView();
    }*/

    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        referenceChatsConversation = myRef.child(mContext.getString(R.string.dbname_order_conversation));
    }

    @Override
    public void onStart() {
        super.onStart();
        if(queryOrdersList != null){
            queryOrdersList.addValueEventListener(listenerOrdersList);
        }

        if(dbQueriesChats != null){
            for(String orderId : ordersIds){
                firstSetupDone = true;
                if(dbQueriesChats.containsKey(orderId)){
                    // update to keep starting listen from last key in case onStop
                    dbQueriesChats.put(orderId, referenceChatsConversation
                            .child(itemOrderByOrderId.get(orderId).getEntage_page_id())
                            .child(orderId)
                            .child("chats").orderByKey().startAt(lastKeys.get(orderId)));

                    dbQueriesChats.get(orderId).addChildEventListener(childEventListeners.get(orderId));
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(queryOrdersList != null){
            queryOrdersList.removeEventListener(listenerOrdersList);
        }

        if(dbQueriesChats != null){
            for(String orderId : ordersIds){
                if(dbQueriesChats.containsKey(orderId)){
                    dbQueriesChats.get(orderId).removeEventListener(childEventListeners.get(orderId));
                }
            }
        }
    }

}
