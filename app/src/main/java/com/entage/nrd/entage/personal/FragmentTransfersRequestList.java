package com.entage.nrd.entage.personal;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.TransferMoneyRequest;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.adapters.AdapterTransferMoneyRequest;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentTransfersRequestList extends Fragment {
    private static final String TAG = "FragmentEditPe";

    private View view;
    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;
    private String user_id;

    private OnActivityListener mOnActivityListener;
    private ImageView backArrow;
    private TextView  titlePage, transfer_request;

    private AdapterTransferMoneyRequest adapterTransferMoneyRequest;
    private ArrayList<TransferMoneyRequest> requests;

    private MessageDialog messageDialog = new MessageDialog();
    private GlobalVariable globalVariable;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_transfers_request_list , container , false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

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
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.request_withdrawal_funds));

        transfer_request = view.findViewById(R.id.transfer_request);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        transfer_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                mOnActivityListener.onActivityListener(new FragmentTransferRequest());
            }
        });

    }

    private void fetchCurrencyUSD_SAR() {
        // first get USD_SAR
        if(PaymentsUtil.PayPal_SAR_USD != null){

            setupAdapter();
            fetchRequests();

        }else {
            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_app_data))
                    .child("usd_sar")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                PaymentsUtil.setPayPal_SAR_USD((String) dataSnapshot.getValue());

                                setupAdapter();
                                fetchRequests();
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

    private void setupAdapter() {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(true);

        requests = new ArrayList<>();

        adapterTransferMoneyRequest = new AdapterTransferMoneyRequest(mContext, recyclerView, requests);
        recyclerView.setAdapter(adapterTransferMoneyRequest);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void fetchRequests(){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_transfer_money_request))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_requests))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        view.findViewById(R.id.progressBar_1).setVisibility(View.GONE);
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                //int index = requests.size();
                                requests.add(0, snapshot.getValue(TransferMoneyRequest.class));
                                adapterTransferMoneyRequest.notifyItemInserted(0);
                            }

                        }else {
                            view.findViewById(R.id.text0).setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        view.findViewById(R.id.progressBar_loading).setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    }
