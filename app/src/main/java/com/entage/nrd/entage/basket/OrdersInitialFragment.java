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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class OrdersInitialFragment extends Fragment {
    private static final String TAG = "OrdersInitialFragment";
    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private Spinner spinnerSorting;

    private OnActivityOrderListener mOnActivityOrderListener;
    private OnActivityListener mOnActivityListener;

    private DatabaseReference referenceOrdersList;
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
        Log.d(TAG, "onAttach: ");
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

        ordersType = mContext.getString(R.string.dbname_orders_initial);

        ((TextView)view.findViewById(R.id.orders_count_text)).setText(mContext.getString(R.string.number_orders)+
                        " " +mContext.getString(R.string.text_the_order_new));
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
        }else {
            adapterOrders = new AdapterOrders(mContext, recyclerView, orders, dateLastMessageInOrders, itemOrderByOrderId,
                    user_id,mOnActivityOrderListener, mOnActivityListener);
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
            referenceOrdersList = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_entage_page_orders))
                    .child(entagePage_id)
                    .child(ordersType);
        }
        else {
            referenceOrdersList = mFirebaseDatabase.getReference()
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
                }else {
                    removeProgressBar();
                    ((RelativeLayout)orders_count.getParent()).setVisibility(View.GONE);
                    view.findViewById(R.id.recyclerView).setVisibility(View.GONE);
                    ((TextView)view.findViewById(R.id.basket_empty)).setText(mContext.getString(R.string.order_list_empty) +
                            " " +mContext.getString(R.string.text_order_new));
                    view.findViewById(R.id.basket_empty).setVisibility(View.VISIBLE);
                }

                // this in case order accept or user cancelled, order will delete from initial list
                for(String orderId : ordersIds){
                    if(!temp.contains(orderId)){ // this order remove from initial list
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

                    // sort by time order
                    Date date = DateTime.getTimestamp(order.getTime_order());
                    dateLastMessageInOrders.put(order.getOrder_id(), date);
                    compareDate(order);

                    if(mOnActivityOrderListener != null){
                        mOnActivityOrderListener.setCountNewMsg(+1, "initial");
                        ordersCount++;
                        orders_count.setText(ordersCount+"");
                    }

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

        if(entagePage_id != null){
            adapterOrdersEntagePage.notifyItemInserted(index);
        }else {
            adapterOrders.notifyItemInserted(index);
        }
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
        Log.d(TAG, "deleteOrder: " + orderId);
        if(adapterOrders != null || adapterOrdersEntagePage != null){
            if(adapterOrders != null) {
                adapterOrders.deleteOrder(orderId);
            }
            if(adapterOrdersEntagePage != null) {
                adapterOrdersEntagePage.deleteOrder(orderId);
            }

            if(mOnActivityOrderListener != null){
                mOnActivityOrderListener.setCountNewMsg(-1, "initial");
                ordersCount--;
                orders_count.setText(ordersCount+"");
            }

            if(orders.size() == 0){
                ((RelativeLayout)orders_count.getParent()).setVisibility(View.GONE);
                view.findViewById(R.id.recyclerView).setVisibility(View.GONE);
                ((TextView)view.findViewById(R.id.basket_empty)).setText(mContext.getString(R.string.order_list_empty) +
                        " " +mContext.getString(R.string.text_order_new));
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

    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(referenceOrdersList != null){
            referenceOrdersList.addValueEventListener(listenerOrdersList);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(referenceOrdersList != null){
            referenceOrdersList.removeEventListener(listenerOrdersList);
        }
    }




}
