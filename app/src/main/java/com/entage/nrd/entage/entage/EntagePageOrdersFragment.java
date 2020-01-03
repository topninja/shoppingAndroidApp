package com.entage.nrd.entage.entage;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.Utilities.SectionsPagerAdapter;
import com.entage.nrd.entage.basket.OrdersCancelledFragment;
import com.entage.nrd.entage.basket.OrdersCompletedFragment;
import com.entage.nrd.entage.basket.OrdersInitialFragment;
import com.entage.nrd.entage.basket.OrdersOngoingFragment;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EntagePageOrdersFragment extends Fragment {
    private static final String TAG = "EntagePageOrdersFragmen";

    private Context mContext ;
    private View view;
    private OnActivityListener mOnActivityListener;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference reference_dbname_order_conversation;
    private String user_id;

    private String entagePage_id, entagePageName;

    private TextView titlePage, textOngoing, textInitial, textCancelled, textCompleted;
    private ImageView more_options;
    private int countOngoing, countInitial, countCancelled, countCompleted;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_orders_entage, container , false);
            mContext = getActivity();

            getIncomingBundle();

        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        super.onAttach(context);
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePage_id =  bundle.getString("entagePageId");
                entagePageName =  bundle.getString("entagePageName");

                if(entagePage_id != null ){
                    setupFirebaseAuth();
                    init();
                }

            }else {
            }
        }
        catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void initWidgets() {
        ImageView backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        backArrow.setColorFilter(Color.rgb(255, 255, 255), PorterDuff.Mode.SRC_ATOP);

        titlePage = view.findViewById(R.id.titlePage);

        if(entagePageName == null){
            entagePageName = "";
            fetchEntagePageName();
        }
        titlePage.setText(mContext.getString(R.string.entageOrders)+ " " + mContext.getString(R.string.for_store) +" "+ entagePageName);
        titlePage.setTextColor(Color.rgb(255, 255, 255));

        more_options = view.findViewById(R.id.more_options);
    }

    private void onClickListener() {
        more_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMenu( v);
            }
        });
    }

    private void setupMenu(View v){
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
        arrayList.add(mContext.getString(R.string.reporting));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alert.dismiss();
                String string = adapter.getItem(position);
                if(string.equals(mContext.getString(R.string.reporting))){
                    FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.orders_problems));
                    fragmentInformProblem.setArguments(bundle);
                    mOnActivityListener.onActivityListener(fragmentInformProblem);
                }
                else {

                }
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void init() {
        initWidgets();
        onClickListener();

        checkUserIdAccess();
        fetchCurrencyUSD_SAR();

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter

        Bundle bundle = new Bundle();
        bundle.putString("entagePageId", entagePage_id);
        bundle.putString("user_id", user_id);

        OrdersOngoingFragment ordersOngoingFragment = new OrdersOngoingFragment();
        OrdersInitialFragment ordersInitialFragment = new OrdersInitialFragment();
        OrdersCancelledFragment ordersCancelledFragment = new OrdersCancelledFragment();
        OrdersCompletedFragment ordersCompletedFragment = new OrdersCompletedFragment();

        ordersOngoingFragment.setArguments(bundle);
        ordersInitialFragment.setArguments(bundle);
        ordersCancelledFragment.setArguments(bundle);
        ordersCompletedFragment.setArguments(bundle);

        adapter.addFragment(ordersOngoingFragment); // index 0
        adapter.addFragment(ordersInitialFragment); // index 1
        adapter.addFragment(ordersCancelledFragment); // index 2
        adapter.addFragment(ordersCompletedFragment); // index 3

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        view.findViewById(R.id.appBarLayout).setBackgroundColor(mContext.getColor(R.color.entage_blue));
        tabLayout.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        //tabLayout.setSelectedTabIndicatorColor(Color.rgb(255, 255, 255));

        tabLayout.getTabAt(0).setCustomView(R.layout.layout_tab);
        tabLayout.getTabAt(1).setCustomView(R.layout.layout_tab);
        tabLayout.getTabAt(2).setCustomView(R.layout.layout_tab);
        tabLayout.getTabAt(3).setCustomView(R.layout.layout_tab);

        ((TextView)tabLayout.getTabAt(0).getCustomView().findViewById(R.id.text)).setText(mContext.getString(R.string.text_order_ongoing));
        ((TextView)tabLayout.getTabAt(1).getCustomView().findViewById(R.id.text)).setText(mContext.getString(R.string.text_order_new));
        ((TextView)tabLayout.getTabAt(2).getCustomView().findViewById(R.id.text)).setText(mContext.getString(R.string.text_order_canceled));
        ((TextView)tabLayout.getTabAt(3).getCustomView().findViewById(R.id.text)).setText(mContext.getString(R.string.text_order_completed));

        textOngoing = tabLayout.getTabAt(0).getCustomView().findViewById(R.id.count);
        textInitial = tabLayout.getTabAt(1).getCustomView().findViewById(R.id.count);
        textCancelled = tabLayout.getTabAt(2).getCustomView().findViewById(R.id.count);
        textCompleted = tabLayout.getTabAt(3).getCustomView().findViewById(R.id.count);

        countOngoing = countInitial = countCancelled = countCompleted = 0;
    }

    public void setCountNewMsg(int count, String ordersType) {
        Log.d(TAG, "setCountNewMsg: " + count + ", " + ordersType);
        if(ordersType.equals("ongoing")){
            countOngoing += count;
            if(countOngoing < 0){
                countOngoing = 0;
            }
            textOngoing.setText(countOngoing+"");
            textOngoing.setVisibility(countOngoing>0? View.VISIBLE:View.GONE);
        }
        else if(ordersType.equals("initial")){
            countInitial += count;
            if(countInitial < 0){
                countInitial = 0;
            }
            textInitial.setText(countInitial+"");
            textInitial.setVisibility(countInitial>0? View.VISIBLE:View.GONE);
        }
        else if(ordersType.equals("cancelled")){
            countCancelled += count;
            if(countCancelled < 0){
                countCancelled = 0;
            }
            textCancelled.setText(countCancelled+"");
            textCancelled.setVisibility(countCancelled>0? View.VISIBLE:View.GONE);
        }
        else if(ordersType.equals("completed")){
            countCompleted += count;
            if(countCompleted < 0){
                countCompleted = 0;
            }
            textCompleted.setText(countCompleted+"");
            textCompleted.setVisibility(countCompleted>0? View.VISIBLE:View.GONE);
        }
    }

    private void fetchCurrencyUSD_SAR() {
        // first get USD_SAR
        if(PaymentsUtil.getPayPal_SAR_USD() == null){
            FirebaseMethods.fetchCurrencyUSD_SAR(mContext);
        }
    }

    private void fetchEntagePageName() {
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entaji_pages_names))
                .child(entagePage_id)
                .child("name_entage_page")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(isAdded() && mContext!=null){
                                entagePageName = (String) dataSnapshot.getValue();
                                titlePage.setText(mContext.getString(R.string.entageOrders)+ " " + mContext.getString(R.string.for_store) +" "+ entagePageName);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void checkUserIdAccess(){
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_access))
                .child(user_id)
                .child(entagePage_id)
                .child("authorization_access")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists() && dataSnapshot.getValue(Boolean.class)){

                        }else {
                            // user doesn has access to this page
                            if(mContext != null && getActivity()!=null){
                                Toast.makeText(mContext, mContext.getString(R.string._dot_have_permission_access_this_page),
                                        Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // user doesn has access to this page
                        if(mContext != null && getActivity()!=null){
                            Toast.makeText(mContext, mContext.getString(R.string._dot_have_permission_access_this_page),
                                    Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                        }
                    }
                });
    }

    private void presence(){
        final DatabaseReference firebaseDatabase = reference_dbname_order_conversation
                .child("status");

        final DatabaseReference lastOnlineRef = reference_dbname_order_conversation
                .child("last_online_entage_page");

        firebaseDatabase.setValue(true);

        // When this device disconnects, set value false
        firebaseDatabase.onDisconnect().setValue(false);

        // When I disconnect, update the last time I was seen online
        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
        /*final DatabaseReference connectedRef = mFirebaseDatabase.getReference(".info/connected");
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

    private void setStatus(boolean boo){
        reference_dbname_order_conversation
                .child("status")
                .setValue(boo);
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        reference_dbname_order_conversation = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_order_conversation_status))
                .child(entagePage_id);

        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            checkUserIdAccess();
        }else {
            Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
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
                        presence();
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
        super.onStart();
        setStatus(true);
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        setStatus(false);
        if( mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
