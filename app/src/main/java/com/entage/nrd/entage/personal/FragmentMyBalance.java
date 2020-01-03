package com.entage.nrd.entage.personal;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.TotalAmounts;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.PaymentInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterPaymentsOperations;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;

public class FragmentMyBalance extends Fragment {
    private static final String TAG = "FragmentMyBalance";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private OnActivityListener mOnActivityListener;

    private View view;
    private Context mContext;

    private TextView total_amount_text, load_more, transfer_request;
    private ProgressBar progress_load_more;
    private RelativeLayout layout_load_more, layout_balance, layout_payment_operations;

    private final int HITS_PER_PAGE = 10;
    private AdapterPaymentsOperations adapterPaymentsOperations;
    private ArrayList<PaymentInformation> paymentInformation;
    private String amount_type;
    private long lastKey;
    private ArrayList<String> keys;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_balance, container , false);
        mContext = getActivity();

        getIncomingBundle();
        setupFirebaseAuth();
        inti();

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                amount_type =  bundle.getString("amount_type");
            }
            if(amount_type == null){
                Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
            Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
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

        transfer_request = view.findViewById(R.id.transfer_request);
        total_amount_text = view.findViewById(R.id.total);

        ((TextView) view.findViewById(R.id.text1)).setText(amount_type.equals(mContext.getString(R.string.dbname_pending_amount))?
                mContext.getString(R.string.pending_amount): mContext.getString(R.string.available_amount));

        view.findViewById(R.id.layout_).setBackgroundColor(amount_type.equals(mContext.getString(R.string.dbname_pending_amount))?
                mContext.getColor(R.color.gray0): mContext.getColor(R.color.entage_blue_1));

        layout_payment_operations = view.findViewById(R.id.layout_payment_operations);
        layout_balance = view.findViewById(R.id.layout_balance);

        layout_load_more = view.findViewById(R.id.layout_load_more);
        layout_load_more.setVisibility(View.GONE);
        progress_load_more = view.findViewById(R.id.progress_load_more);
        load_more = view.findViewById(R.id.text);
        load_more.setText(mContext.getString(R.string.load_more_operations));

        if(amount_type.equals(mContext.getString(R.string.dbname_available_amount))){
            transfer_request.setVisibility(View.VISIBLE);
        }

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
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

        layout_load_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_load_more.setClickable(false);
                load_more.setVisibility(View.GONE);
                progress_load_more.setVisibility(View.VISIBLE);

                fetchOperations(true);
            }
        });

        if(amount_type.equals(mContext.getString(R.string.dbname_available_amount))){
            transfer_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnActivityListener.onActivityListener(new FragmentTransfersRequestList());
                }
            });
        }
    }

    private void setupAdapter(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        keys = new ArrayList<>();
        paymentInformation = new ArrayList<>();

        adapterPaymentsOperations = new AdapterPaymentsOperations(mContext, recyclerView, paymentInformation, user_id);
        recyclerView.setAdapter(adapterPaymentsOperations);
    }

    private void fetchOperations(final boolean loadMore){
        Query query = null;
        if(loadMore){
            Log.d(TAG, "onDataChange: " + lastKey);
            query = myRef.orderByChild("timestamp")
                    .endAt(lastKey)
                    .limitToLast(HITS_PER_PAGE+1); // first one is duplicate
        }else {
            query = myRef
                    .orderByChild("timestamp")
                    .limitToLast(HITS_PER_PAGE);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(paymentInformation.size() == 0){ // remove progress bar
                                view.findViewById(R.id.progressBar_lastOperations).setVisibility(View.GONE);
                                view.findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
                            }

                            lastKey = 0; // cause we want first one
                            int index = paymentInformation.size();
                            for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                PaymentInformation pi = singleSnapshot.getValue(PaymentInformation.class);
                                if(lastKey == 0){ // cause we want first one
                                    lastKey = (long) pi.getTimestamp();
                                }

                                String key = singleSnapshot.getKey();
                                if(!keys.contains(key)){
                                    keys.add(key);
                                    paymentInformation.add(index, pi);
                                    adapterPaymentsOperations.notifyItemInserted(index);
                                }
                            }
                        }
                        if(paymentInformation.size() == 0){
                            view.findViewById(R.id.progressBar_lastOperations).setVisibility(View.GONE);
                            view.findViewById(R.id.no_payments_operations).setVisibility(View.VISIBLE);

                        }else {
                            if(paymentInformation.size()%HITS_PER_PAGE == 0){
                                layout_load_more.setClickable(true);
                            }else {
                                load_more.setText(mContext.getString(R.string.you_seeing_all_operations));
                            }
                            layout_load_more.setVisibility(View.VISIBLE);
                            load_more.setVisibility(View.VISIBLE);
                            progress_load_more.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void fetchCurrencyUSD_SAR() {
        // first get USD_SAR
        if(PaymentsUtil.PayPal_SAR_USD != null){

            setupAdapter();
            fetchTotal();
            fetchOperations(false);

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
                                fetchTotal();
                                fetchOperations(false);
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

    private void fetchTotal(){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(amount_type.equals(mContext.getString(R.string.dbname_pending_amount))?
                        mContext.getString(R.string.dbname_pending_amount_total):mContext.getString(R.string.dbname_available_amount_total))
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

                        total_amount_text.setText(PaymentsUtil.print(total_sar)+
                                " SAR  |  "+ PaymentsUtil.print(total_usd)+" USD");
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void setTotal(final float total) {
        if(isAdded() && mContext !=null){
            ValueAnimator animator = ValueAnimator.ofFloat(0, total);
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    total_amount_text.setText(animation.getAnimatedValue().toString() + " USD");
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
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        myRef = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(amount_type);

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

