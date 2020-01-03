package com.entage.nrd.entage.basket;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.newItem.AddNewItemActivity;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;


public class FragmentUserBasketContainer extends Fragment {
    private static final String TAG = "FragmentBasket";


    private Context mContext ;
    private View view;
    private OnActivityListener mOnActivityListener;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private String user_id;

    private TextView textOngoing, textInitial, textCancelled, textCompleted;
    private ImageView more_options;
    private int countOngoing, countInitial, countCancelled, countCompleted;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_orders_entage, container , false);
            mContext = getActivity();

            setupFirebaseAuth();

            init();
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

        TextView titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.myOrders));
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

        fetchCurrencyUSD_SAR();

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter

        Bundle bundle = new Bundle();
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

    private void presence(){
        final DatabaseReference firebaseDatabase = myRef
                .child(mContext.getString(R.string.dbname_users_status))
                .child("order_page")
                .child(user_id)
                .child("status");

        final DatabaseReference lastOnlineRef = myRef
                .child(mContext.getString(R.string.dbname_users_status))
                .child("order_page")
                .child(user_id)
                .child("last_online_user");

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
        myRef.child(mContext.getString(R.string.dbname_users_status))
                .child("order_page")
                .child(user_id)
                .child("status")
                .setValue(boo);
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "SignIn : Uid:  " + user.getUid());
                    if(!user.isAnonymous()){
                        presence();
                    }else {

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
