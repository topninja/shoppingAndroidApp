package com.entage.nrd.entage.personal;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.TotalAmounts;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.PaymentInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.DepositFundsActivity;
import com.entage.nrd.entage.adapters.AdapterPaymentsOperations;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class FragmentMyWallet extends Fragment {
    private static final String TAG = "FragmentMyWallet";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, refAvailableAmount, refPendingAmount;
    private String user_id;

    private OnActivityListener mOnActivityListener;
    private View view;
    private Context mContext;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView  available_amount, pending_amount, transfer_request, deposit_request;
    private RelativeLayout layout_available, layout_pending, layout_balance, layout_payment_operations;
    private GlobalVariable globalVariable;

    private AdapterPaymentsOperations adapterPaymentsOperations;
    private ArrayList<PaymentInformation> paymentInformation;
    private Map<Date, PaymentInformation> orderPaymentInfo;

    private MessageDialog messageDialog = new MessageDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_my_wallet, container , false);
            mContext = getActivity();

            setupFirebaseAuth();
            inti();
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

    private void inti(){
        initWidgets();
        onClickListener();

        fetchCurrencyUSD_SAR();
    }

    private void initWidgets(){
        TextView titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.my_wallet));

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setEnabled(false);

        available_amount = view.findViewById(R.id.available_amount);
        pending_amount = view.findViewById(R.id.pending_amount);

        transfer_request = view.findViewById(R.id.transfer_request);
        deposit_request = view.findViewById(R.id.deposit_request);

        layout_available = view.findViewById(R.id.layout_available);
        layout_pending = view.findViewById(R.id.layout_pending);

        layout_payment_operations = view.findViewById(R.id.layout_payment_operations);
        layout_balance = view.findViewById(R.id.layout_balance);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        if(globalVariable.getCurrency() != null){
            available_amount.setText("0 " + "USD"/* globalVariable.getCurrency().getCurrencyCode()*/);
            pending_amount.setText("0 " + "USD"/* globalVariable.getCurrency().getCurrencyCode()*/);
        }
    }

    private void onClickListener(){
        ImageView backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        transfer_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityListener.onActivityListener(new FragmentTransfersRequestList());
            }
        });

        deposit_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(mContext, DepositFundsActivity.class);
                mContext.startActivity(intent);
            }
        });

        ImageView more_options = view.findViewById(R.id.more_options);
        more_options.setVisibility(View.VISIBLE);
        more_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMenu();
            }
        });

        layout_balance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        layout_payment_operations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        layout_available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("amount_type", mContext.getString(R.string.dbname_available_amount));
                mOnActivityListener.onActivityListener(new FragmentMyBalance(), bundle);
            }
        });
        layout_pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("amount_type", mContext.getString(R.string.dbname_pending_amount));
                mOnActivityListener.onActivityListener(new FragmentMyBalance(), bundle);
            }
        });

        mSwipeRefreshLayout.setColorScheme(R.color.entage_blue, R.color.entage_blue_1);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new CountDownTimer(1000, 1000) {
                    public void onTick(long millisUntilFinished) { }
                    public void onFinish() {
                        mOnActivityListener.onActivityListener_noStuck(new FragmentMyWallet());
                    }
                }.start();
            }
        });
    }

    private void setupAdapter(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        orderPaymentInfo =  new TreeMap<>(Collections.reverseOrder());
        paymentInformation = new ArrayList<>();

        adapterPaymentsOperations = new AdapterPaymentsOperations(mContext, recyclerView, paymentInformation, user_id);
        recyclerView.setAdapter(adapterPaymentsOperations);
    }

    private void fetchLastTwoOperations(){
        // fetch last two operations of available_amount and pending_amount
        refAvailableAmount
                .orderByChild("timestamp")
                .limitToLast(3)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        PaymentInformation pi = singleSnapshot.getValue(PaymentInformation.class);
                        orderPaymentInfo.put( DateTime.getTimestamp(pi.getTime()), pi);
                    }
                }

                refPendingAmount
                        .orderByChild("timestamp")
                        .limitToLast(3)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                        PaymentInformation pi = singleSnapshot.getValue(PaymentInformation.class);
                                        orderPaymentInfo.put( DateTime.getTimestamp(pi.getTime()), pi);
                                    }
                                }


                                Log.d(TAG, "onDataChange: " + new ArrayList<>(orderPaymentInfo.values()).toString());
                                if(orderPaymentInfo.size()>3){
                                    paymentInformation.addAll(new ArrayList<>(orderPaymentInfo.values()).subList(0,3)) ;
                                }else {
                                    paymentInformation.addAll(orderPaymentInfo.values());
                                }

                                view.findViewById(R.id.progressBar_lastOperations).setVisibility(View.GONE);
                                view.findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
                                adapterPaymentsOperations.notifyDataSetChanged();

                                if(paymentInformation.size()==0){
                                    view.findViewById(R.id.recyclerView).setVisibility(View.GONE);
                                    view.findViewById(R.id.no_payments_operations).setVisibility(View.VISIBLE);
                                }

                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void fetchCurrencyUSD_SAR() {
        // first get USD_SAR
        if(PaymentsUtil.PayPal_SAR_USD != null){

            mSwipeRefreshLayout.setEnabled(true);
            setupAdapter();
            fetchTotals();
            fetchLastTwoOperations();

        }else {
            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_app_data))
                    .child("usd_sar")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                PaymentsUtil.setPayPal_SAR_USD((String) dataSnapshot.getValue());


                                mSwipeRefreshLayout.setEnabled(true);
                                setupAdapter();
                                fetchTotals();
                                fetchLastTwoOperations();

                            }else {
                                view.findViewById(R.id.progressBar_loading).setVisibility(View.GONE);
                                messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            view.findViewById(R.id.progressBar_loading).setVisibility(View.GONE);
                            messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                        }
                    });
        }
    }

    private void fetchTotals(){
        // available
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount_total))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        BigDecimal total_sar = PaymentsUtil.microsToString("0.00");
                        BigDecimal total_usd = PaymentsUtil.microsToString("0.00");

                        if(dataSnapshot.exists()){
                            for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                TotalAmounts total = singleSnapshot.getValue(TotalAmounts.class);

                                total_sar = total_sar.add(PaymentsUtil.microsToString(total.getTotal_sar()));
                                total_usd = total_usd.add(PaymentsUtil.microsToString(total.getTotal_usd()));
                            }
                        }

                        available_amount.setText(PaymentsUtil.print(total_sar)+
                                " SAR  |  "+ PaymentsUtil.print(total_usd)+" USD");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

        // pending
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_pending_amount_total))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        BigDecimal total_sar = PaymentsUtil.microsToString("0.00");
                        BigDecimal total_usd = PaymentsUtil.microsToString("0.00");

                        if(dataSnapshot.exists()){
                            for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                TotalAmounts total = singleSnapshot.getValue(TotalAmounts.class);

                                total_sar = total_sar.add(PaymentsUtil.microsToString(total.getTotal_sar()));
                                total_usd = total_usd.add(PaymentsUtil.microsToString(total.getTotal_usd()));
                            }
                        }

                        pending_amount.setText(PaymentsUtil.print(total_sar)+
                                " SAR  |  "+ PaymentsUtil.print(total_usd)+" USD");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private void setTotal(final TextView textView, float total) {
        if(isAdded() && mContext !=null){
            ValueAnimator animator = ValueAnimator.ofFloat(0, total);
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    textView.setText(animation.getAnimatedValue().toString() + " USD");
                }
            });
            animator.start();
        }
    }

    private void setupMenu(){
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
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.pay_problems));
                    mOnActivityListener.onActivityListener(new FragmentInformProblem(), bundle);

                }else {

                }
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }


    /*
   -------------------------------Firebase-------------------------------------------------------
   */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        user_id = mAuth.getCurrentUser().getUid();

        refAvailableAmount = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount));

        refPendingAmount = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_pending_amount));

/*
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount))
        .child("123333")
        .setValue(new PaymentInformation(true, "deposit", "available", "sell_items",
                new Ids(user_id, "0000", "0000", "0000", "0000",
                        "0000", "0000", "0000"),
                "100", "0000", "0000", DateTime.getTimestamp(), ServerValue.TIMESTAMP));

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount_total))
                .child("123333")
                .child("total").setValue("100");*/



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user == null || user.isAnonymous()){
                    Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if( mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
