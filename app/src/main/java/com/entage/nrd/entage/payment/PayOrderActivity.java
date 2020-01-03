package com.entage.nrd.entage.payment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.BraintreeError;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.ConfigurationListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.Configuration;
import com.braintreepayments.api.models.PayPalAccountNonce;
import com.braintreepayments.api.models.PayPalRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.entage.nrd.entage.Models.ConvertCurrency;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.PaymentClaim;
import com.entage.nrd.entage.Models.PaymentConfirm;
import com.entage.nrd.entage.Models.TotalAmounts;
import com.entage.nrd.entage.Models.UserAccountSettings;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.home.ActivityForOpenFragments;
import com.entage.nrd.entage.adapters.AdapterOrderSummery;
import com.entage.nrd.entage.utilities_1.Currencylayer;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
/*
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
*/

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class PayOrderActivity extends AppCompatActivity  implements PaymentMethodNonceCreatedListener, ConfigurationListener,
        BraintreeCancelListener, BraintreeErrorListener {
    private static final String TAG = "PayOrderActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference  ref_payments_processes, ref_paymentsSucceed, ref_paymentsError;
    private ValueEventListener listener_paymentsSucceed, listener_paymentsError;
    private String user_id;

    public static final String GENERATE_CLIENT_TOKEN_URL = "https://us-central1-entage-1994.cloudfunctions.net/generateClientTokenId";

    public final String PAYMENT_FOR = "payments_orders";

    private Context mContext ;
    private TextView purchase_total_text, shipping_price_text, total_SAR_text, have_problem_pay, total_USD_text;
    private AppCompatButton pay_paypal, pay_mastercard;
    private RelativeLayout pay_whitMyWallet;

    private String currencyPrice, currencyName;
    private Order order;
    //private ArrayList<ItemOrder> itemOrders;
    //private ArrayList<String> itemsIds;
    private PaymentClaim paymentClaim;
    private boolean any_error = false;
    private boolean cancelPayment = false;
    private BigDecimal final_total_USD, total_SAR, total_USD, items_total_SAR, items_total_USD, shipping_total, transaction_fee_usd,
    available_total_usd, available_total_sar;
    private UserAccountSettings userAccount;

    private ArrayList<AdapterOrderSummery.OrderSummery> ordersSummery;
    private AdapterOrderSummery adapterOrderSummery;
    private GlobalVariable globalVariable;
    private String order_id, message_id, entage_page_id, payment_id, clientToken, tokenId_entagePage_user, entagePage_userId;
    //private HashMap<String, String>  itemsName;
    private HashMap<String, BigDecimal> items_prices_USD;

    private AlertDialog alertCancelled, alertInit, alertConfirmingPayment;
    private MessageDialog messageDialog = new MessageDialog();

    private OkHttpClient client;
    private BraintreeFragment mBraintreeFragment;
    private String payments_status;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_order);
        mContext = PayOrderActivity.this;

        backArrow();

        order_id = getIntent().getStringExtra("order_id");
        message_id = getIntent().getStringExtra("message_id");
        entage_page_id = getIntent().getStringExtra("entage_page_id");

        if(order_id != null && message_id != null && entage_page_id !=null){
            setupFirebaseAuth();

            init();
        }
        else {

        }
    }

    private void backArrow(){
        ((TextView)findViewById(R.id.titlePage)).setText(mContext.getString(R.string.pay_order_activity));
        ImageView mBackArrow = findViewById(R.id.back);
        mBackArrow.setVisibility(View.VISIBLE);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
    }

    private void init(){
/*      NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("ar", "sa"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(5);
        format.setRoundingMode(RoundingMode.HALF_EVEN);
        Log.d(TAG, "init: " + format.format(3050.5));*/

        initWidgets();

        setupAdapter();

        fetchCurrencyUSD_SAR();
        fetchEntagePage_userId();
        fetchUserData();
        checkEntagePageStatus();
        fetchPaymentClaim();
        fetchCurrencyPage();
    }

    private void initWidgets() {
        purchase_total_text = findViewById(R.id.items_amount_total);
        shipping_price_text = findViewById(R.id.shipping_price);
        ((TextView)((RelativeLayout)shipping_price_text.getParent()).getChildAt(0)).setText(getString(R.string.shipping_price)+":");
        total_SAR_text = findViewById(R.id.the_total);
        total_USD_text  = findViewById(R.id.the_total_usd);

        pay_paypal = findViewById(R.id.pay_paypal);
        pay_mastercard = findViewById(R.id.pay_mastercard);
        pay_whitMyWallet  = findViewById(R.id.pay_whit_my_wallet);

        have_problem_pay = findViewById(R.id.have_problem_pay);
        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

    }

    private void setupAdapter() {
        RecyclerView recyclerView =  findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        ordersSummery = new ArrayList<>();
        ordersSummery.add(new AdapterOrderSummery.OrderSummery("",0,"0"));

        adapterOrderSummery = new AdapterOrderSummery(mContext, ordersSummery);
        recyclerView.setAdapter(adapterOrderSummery);
    }

    private void checkEntagePageStatus() {
        // check status of the store
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_status))
                .child(entage_page_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String status_item = dataSnapshot.child(mContext.getString(R.string.field_status_item)).getValue(String.class);
                    if(status_item.equals(mContext.getString(R.string.item_authorized))){

                    }else {
                        if(status_item.equals(mContext.getString(R.string.item_unauthorized))){
                            databaseError(null, mContext.getString(R.string.status_store_unauthorized));
                        }
                        else if(status_item.equals(mContext.getString(R.string.item_pending))){
                            databaseError(null, mContext.getString(R.string.status_store_pending));
                        }
                        else if(status_item.equals(mContext.getString(R.string.store_off))){
                            databaseError(null, mContext.getString(R.string.status_store_off));
                        }
                    }
                }else {
                    databaseError(null, mContext.getString(R.string.status_store_unauthorized));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError, null);
            }
        });
    }

    private void fetchEntagePage_userId() {
        // check status of the store
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entage_page_id)
                .child("users_ids")
                .child("0")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            entagePage_userId = (String) dataSnapshot.getValue();

                            /*mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_users_token))
                                    .child(entagePage_userId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                tokenId_entagePage_user = (String) dataSnapshot.getValue();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });*/
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void fetchUserData() {
        // check status of the store
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .child(user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            userAccount = dataSnapshot.getValue(UserAccountSettings.class);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void fetchCurrencyUSD_SAR() {
        if(PaymentsUtil.getPayPal_SAR_USD() == null){
            FirebaseMethods.fetchCurrencyUSD_SAR(mContext);
        }
    }

    private void fetchPaymentClaim() {
        Query query =  mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_requests_payment_claim))
                .child(entage_page_id)
                .child(order_id)
                .child(message_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    paymentClaim = dataSnapshot.getValue(PaymentClaim.class);

                }else {
                    any_error = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError, null);
            }
        });
    }

    private void fetchCurrencyPage() {
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entage_page_id)
                .child(mContext.getString(R.string.field_currency_entage_page))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currencyPrice = (String) dataSnapshot.getValue();
                    currencyName = Currency.getInstance(currencyPrice).getDisplayName();

                    if(globalVariable.getCurrency() == null || globalVariable.getCurrency().toString().equals(currencyPrice)){
                        //view.findViewById(R.id.layout_price).setVisibility(View.GONE);
                    }
                }
                else {
                    any_error = true;
                }

                fetchOrder();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError, null);
            }
        });
    }

    private void fetchOrder() {
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .child(order_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            order = dataSnapshot.getValue(Order.class);

                            ((TextView)findViewById(R.id.number_order)).setText(getString(R.string.number_order)+" "+order.getOrder_number());
                            
                            setupOrderData();

                        }else {
                            any_error = true;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        databaseError(databaseError, null);
                    }
                });
    }

    // setup data in widgets
    private void setupOrderData(){
        if(!any_error){
            findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);
            /*new CountDownTimer(500, 1000) {
                public void onTick(long millisUntilFinished) { }
                public void onFinish() {
                    if(!isDestroyed())
                        findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE); }
            }.start();*/
            setupOkHttpClient();

            if(PaymentsUtil.getPayPal_SAR_USD() == null){
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                return;
            }

            total_SAR = PaymentsUtil.microsToString("0.00");
            total_USD = PaymentsUtil.microsToString("0.00");

            items_total_SAR = PaymentsUtil.microsToString("0.00");
            items_prices_USD = new HashMap<>();

            shipping_total = PaymentsUtil.microsToString(paymentClaim.getShipping_price());

            total_SAR = total_SAR.add(shipping_total);
            //total_USD = total_USD.add(PaymentsUtil.divide(shipping_total, USD_SAR));

            ordersSummery.remove(0);
            adapterOrderSummery.notifyDataSetChanged();
            if(order != null){
                for(ItemOrder _itemOrder : order.getItem_orders().values()){
                    //
                    BigDecimal item_price = PaymentsUtil.microsToString(_itemOrder.getItem_price());
                    BigDecimal item_subtotal = PaymentsUtil.calculatingTotalPriceItem(item_price, _itemOrder.getQuantity(), "0.0");

                    items_prices_USD.put(_itemOrder.getItem_basket_id(), PaymentsUtil.converter_SAR_USD(item_price));

                    items_total_SAR = items_total_SAR.add(item_subtotal);

                    // adapter
                    int index = ordersSummery.size();
                    ordersSummery.add(index,
                            new AdapterOrderSummery.OrderSummery(
                                    _itemOrder.getItem_name()+StringManipulation.printArrayListItemsOrder(_itemOrder.getOptions())
                                    , _itemOrder.getQuantity(),
                                    PaymentsUtil.print(item_subtotal)));
                    adapterOrderSummery.notifyItemInserted(index);
                }

                items_total_USD = PaymentsUtil.converter_SAR_USD(items_total_SAR);
                total_SAR = total_SAR.add(items_total_SAR);
                total_USD = PaymentsUtil.converter_SAR_USD(total_SAR);

            }
            else {
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                return;
            }

            /* new ConverterCurrency(new ConvertCurrency(Currency.getInstance(currencyPrice), Currency.getInstance("USD"), total,
            null, null, null, null, null));*/

            shipping_price_text.setText(shipping_total.doubleValue()==0.0? getString(R.string.free) : PaymentsUtil.print(shipping_total));
            purchase_total_text.setText(PaymentsUtil.print(items_total_SAR));
            total_SAR_text.setText(PaymentsUtil.print(total_SAR)+" "+currencyName);

            total_USD_text.setText(PaymentsUtil.print(total_USD) + " " + Currency.getInstance("USD").getDisplayName());
            total_USD_text.setVisibility(View.VISIBLE);

            transaction_fee_usd = PaymentsUtil.converter_SAR_USD(PaymentsUtil.TRANSACTION_FEE);

            final_total_USD = PaymentsUtil.microsToString(total_USD.add(transaction_fee_usd).toPlainString());

            Log.d(TAG, "ClientToken: items_total: " + items_total_SAR.toString() );
            Log.d(TAG, "ClientToken: total_USD: " + total_USD.toString() );
            Log.d(TAG, "ClientToken: transaction_fee: " + transaction_fee_usd.toString());
            Log.d(TAG, "ClientToken: final_total_USD: " + final_total_USD.toString());

            setOnClickListener();
            have_problem_pay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =  new Intent(mContext, ActivityForOpenFragments.class);
                    intent.putExtra("notification_flag", "problem");
                    intent.putExtra("typeProblem", mContext.getString(R.string.pay_problems));
                    startActivity(intent);
                }
            });

        }
        else { databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
            return;
        }
    }

    private void openInitDialog(){
        Log.d(TAG, "openInitDialog: ");

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.pay_purchase_amount_preparing));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                openCancelingDialog();
            }
        });
        alertInit = builder.create();
        alertInit.setCanceledOnTouchOutside(false);
        alertInit.setCancelable(false);

        cancelPayment = false;
        fetchClientToken("PayPal");

        alertInit.show();
    }

    private void openInitDialog_myWallet(){
        Log.d(TAG, "openInitDialog: ");

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.checking_wallet_balance));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                openCancelingDialog();
            }
        });
        alertInit = builder.create();
        alertInit.setCanceledOnTouchOutside(false);
        alertInit.setCancelable(false);

        cancelPayment = false;
        alertInit.show();

        // checking_wallet_balance
        checking_balance();
    }

    private void openCancelingDialog(){
        Log.d(TAG, "openCancelingDialog: ");

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.wait_canceling));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alertCancelled = builder.create();
        alertCancelled.setCanceledOnTouchOutside(false);
        alertCancelled.setCancelable(false);
        alertCancelled.show();

        cancelPayment = true;
    }

    private void cancelPaymentProcess(){
        Log.d(TAG, "cancelPaymentProcess: ");

        pay_paypal.setClickable(true);
        pay_mastercard.setClickable(true);
        pay_whitMyWallet.setClickable(true);

        any_error = false;

        if(alertInit != null){
            alertInit.dismiss();
        }
        if(alertCancelled != null){
            alertCancelled.dismiss();
        }
        if(alertConfirmingPayment != null){
            alertConfirmingPayment.dismiss();
        }

        if(ref_paymentsSucceed != null){
            ref_paymentsSucceed.removeEventListener(listener_paymentsSucceed);
        }
        if(ref_paymentsError != null){
            ref_paymentsError.removeEventListener(listener_paymentsError);
        }
        if(mBraintreeFragment != null){
            mBraintreeFragment.removeListener(this);
        }
    }

    private void setupOkHttpClient(){
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { trustManager }, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory, trustManager);

            client = builder.build();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private void setOnClickListener(){
        pay_paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!any_error){
                    pay_paypal.setClickable(false);
                    pay_mastercard.setClickable(false);
                    pay_whitMyWallet.setClickable(false);

                    findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);
                    openInitDialog();

                }else {
                    messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                }
            }
        });

        pay_mastercard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!any_error){
                    messageDialog.errorMessage(mContext, getString(R.string.master_card_not_available));
                }else {
                    messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                }
            }
        });

        pay_whitMyWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!any_error){
                    pay_paypal.setClickable(false);
                    pay_mastercard.setClickable(false);
                    pay_whitMyWallet.setClickable(false);

                    findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);
                    openInitDialog_myWallet();

                }else {
                    messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                }
            }
        });
    }

    // step one
    private void checking_balance(){
        // available
        //availableAmountTotal = PaymentsUtil.microsToString("0.00");
        available_total_sar = PaymentsUtil.microsToString("0.00");
        available_total_usd = PaymentsUtil.microsToString("0.00");

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount_total))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                TotalAmounts total = singleSnapshot.getValue(TotalAmounts.class);

                                available_total_sar = available_total_sar.add(PaymentsUtil.microsToString(total.getTotal_sar()));
                                available_total_usd = available_total_usd.add(PaymentsUtil.microsToString(total.getTotal_usd()));
                            }
                        }
                        cancelPaymentProcess();

                        String msg = "";
                        if(available_total_sar.compareTo(total_SAR) <= 0){
                            msg = mContext.getString(R.string.no_balance_for_payment);
                        }

                        if(mContext!=null){
                            //View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_wait, null);

                            //((TextView)_view.findViewById(R.id.text_view1)).setTextSize(16);
                            //((TextView)_view.findViewById(R.id.text_view1)).setText();

                            String m = mContext.getString(R.string.your_available_balance_is)+ "\n\n"+
                                    PaymentsUtil.print(available_total_sar) + " SAR |  " +
                                    PaymentsUtil.print(available_total_usd) + " USD";

                           // _view.findViewById(R.id.progressBar).setVisibility(View.GONE);

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
                            if(msg.length() > 0){
                                builder.setTitle(mContext.getString(R.string.you_cant_do_this_operation));
                                builder.setMessage(msg+"\n"+m);
                            }
                            //builder.setView(_view);
                            builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();

                                }
                            });

                            if(msg.length() == 0){
                                builder.setTitle(mContext.getString(R.string.pay_the_amount));
                                builder.setMessage(m);
                                builder.setPositiveButton(mContext.getString(R.string.pay_the_amount) , new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        fetchClientToken("MyWallet");
                                    }
                                });
                            }

                            AlertDialog alertInit = builder.create();
                            alertInit.setCanceledOnTouchOutside(false);
                            alertInit.setCancelable(false);
                            alertInit.show();
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                        cancelPaymentProcess();
                    }
                });
    }

    private void fetchClientToken(String checkout_by){
        Log.d(TAG, "fetchClientToken: " + clientToken);

        payment_id = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_payments_processes))
                .push().getKey();
        if (payment_id != null && user_id!=null && order_id!=null && entagePage_userId != null){
           if(checkout_by.equals("PayPal")){
               if(clientToken != null){
                   setupBraintreeAndStartExpressCheckout();

               }
               else {
                   new GenerateClientToken(GENERATE_CLIENT_TOKEN_URL);


                /*AsyncHttpClient client = new AsyncHttpClient();
                client.get("https://us-central1-entage-1994.cloudfunctions.net/generateClientTokenId", new TextHttpResponseHandler() {
                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString, Throwable throwable) {
                        setErrorToDatabase("Error in fetching client token", "e: "+ throwable);
                        databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                        cancelPaymentProcess();
                    }

                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String responseString) {
                        try {
                            Log.d(TAG, "fetchClientToken: " + responseString);
                            JSONObject response = new JSONObject(responseString);
                            if(response.get("clientToken")!=null){
                                clientToken = (String) response.get("clientToken");
                                setupBraintreeAndStartExpressCheckout();

                            }else {
                                setErrorToDatabase("Error in fetching client token", "e: "+ responseString);
                                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                                cancelPaymentProcess();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
               }
           }else if(checkout_by.equals("MyWallet")){
               postNonceToServer(null, null, "MyWallet");
           }
        }
        else {
            databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
            cancelPaymentProcess();
        }

    }
    // END

    // step two
    private void setupBraintreeAndStartExpressCheckout(){
        Log.d(TAG, "ClientToken: " + PaymentsUtil.print(final_total_USD));
/*
        for(int i=0; i<itemOrders.size(); i++){
            ItemOrder itemOrder = itemOrders.get(i);

            LineItem lineItem = new LineItem();
            // BigDecimal item_price_usd =  PaymentsUtil.microsToString(itemOrder.getItem_price()/usd_sar);
            lineItem.setUnitAmount(items_prices_USD.get(itemOrder.getItem_id()).stripTrailingZeros().toPlainString());
            lineItem.setTotalAmount(items_prices_USD.get(itemOrder.getItem_id())
                    .multiply(PaymentsUtil.microsToString(Integer.toString(itemOrder.getQuantity())))
                    .stripTrailingZeros().toPlainString());
            Log.d(TAG, "ClientToken: " + lineItem.getTotalAmount()+", " + lineItem.getUnitAmount());
        }
        Log.d(TAG, "ClientToken: " + (transaction_fee.add(PaymentsUtil.converter_SAR_USD(shipping_total))));
*/


        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, clientToken);
            // mBraintreeFragment is ready to use!
            PayPalRequest request = new PayPalRequest(PaymentsUtil.print(final_total_USD))
                    .currencyCode("USD")
                    .intent(PayPalRequest.INTENT_SALE);
                    //.offerCredit(true); // Offer PayPal Credit;
            mBraintreeFragment.addListener(this);

            if(!cancelPayment){
                PayPal.requestOneTimePayment(mBraintreeFragment, request);

            }else {
                cancelPaymentProcess();
            }

        } catch (InvalidArgumentException e) {
            // There was an issue with your authorization string.
            Log.d(TAG, "setupBraintreeAndStartExpressCheckout: There was an issue with your authorization string.");
            setErrorToDatabase("setupBraintreeAndStartExpressCheckout", "There was an issue with your authorization string.");
            databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
            cancelPaymentProcess();
        }
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        Log.d(TAG, "onPaymentMethodNonceCreated: ");
        // Send nonce to server
        String nonce = paymentMethodNonce.getNonce();
        if (paymentMethodNonce instanceof PayPalAccountNonce) {

            PayPalAccountNonce payPalAccountNonce = (PayPalAccountNonce)paymentMethodNonce;

            /*// Access additional information
            String email = payPalAccountNonce.getEmail();
            String firstName = payPalAccountNonce.getFirstName();
            String lastName = payPalAccountNonce.getLastName();
            String phone = payPalAccountNonce.getPhone();

            // See PostalAddress.java for details
            PostalAddress billingAddress = payPalAccountNonce.getBillingAddress();
            PostalAddress shippingAddress = payPalAccountNonce.getShippingAddress();*/

            // more information: https://developers.braintreepayments.com/reference/request/transaction/sale/node#line_items.description

            postNonceToServer(paymentMethodNonce, nonce, "PayPal");
            //postNonceToServer(paymentSendNonce);
        }else {
            databaseError(null, "");
            cancelPaymentProcess();
        }
    }

    @Override
    public void onConfigurationFetched(Configuration configuration) {
        Log.d(TAG, "onConfigurationFetched: " + configuration.toString());
    }

    @Override
    public void onCancel(int requestCode) {
        Log.d(TAG, "onCancel: ");
        // Use this to handle a canceled activity, if the given requestCode is important.
        // You may want to use this callback to hide loading indicators, and prepare your UI for input
        cancelPaymentProcess();
    }

    @Override
    public void onError(Exception error) {
        Log.d(TAG, "onError: " + error.getMessage());
        String msg = error.getMessage();
        if (error instanceof ErrorWithResponse) {
            ErrorWithResponse errorWithResponse = (ErrorWithResponse) error;
            BraintreeError cardErrors = errorWithResponse.errorFor("creditCard");
            if (cardErrors != null) {
                // There is an issue with the credit card.
                BraintreeError expirationMonthError = cardErrors.errorFor("expirationMonth");
                if (expirationMonthError != null) {
                    // There is an issue with the expiration month.
                    Log.d(TAG, "onError: " + expirationMonthError.getMessage());
                    msg = expirationMonthError.getMessage();
                }
            }
        }
        setErrorToDatabase("braintree_error", msg);
        databaseError(null, msg);
        cancelPaymentProcess();
    }
    // END

    // Step three
    private void postNonceToServer(PaymentMethodNonce paymentMethodNonce, String nonce, String checkout_by){
        Log.d(TAG, "postNonceToServer: ");
        openConfirmingPaymentDialog();

        List<LineItem> items_information = new ArrayList<>();

        //TransactionLineItemRequest lineItemRequest = new TransactionLineItemRequest();

        for(ItemOrder _itemOrder : order.getItem_orders().values()){
            LineItem lineItem = new LineItem();
           // BigDecimal item_price_usd =  PaymentsUtil.microsToString(itemOrder.getItem_price()/usd_sar);
            
            String item_basket_id = _itemOrder.getItem_basket_id();
            lineItem.setName(_itemOrder.getItem_name()+StringManipulation.printArrayListItemsOrder(_itemOrder.getOptions()));
            lineItem.setQuantity(_itemOrder.getQuantity()+"");
            lineItem.setUnitAmount(PaymentsUtil.print(items_prices_USD.get(item_basket_id)));
            lineItem.setTotalAmount(PaymentsUtil.print(
                    PaymentsUtil.multiply(items_prices_USD.get(item_basket_id), 
                            PaymentsUtil.microsToString(Integer.toString(_itemOrder.getQuantity())))));
            lineItem.setTaxAmount("0.0");
            lineItem.setDiscountAmount("0.0");
            lineItem.setProductCode(_itemOrder.getItem_number()!=null? _itemOrder.getItem_number() :_itemOrder.getItem_id());
            lineItem.setKind("debit");

            items_information.add(lineItem);
        }


        String email, firstName, lastName ,phone;
        email= firstName= lastName= phone = " ";

        if(paymentMethodNonce != null){
            PayPalAccountNonce payPalAccountNonce = (PayPalAccountNonce)paymentMethodNonce;
            email = payPalAccountNonce.getEmail()!=null?payPalAccountNonce.getEmail() : "0";
            firstName = payPalAccountNonce.getFirstName()!=null?payPalAccountNonce.getFirstName() : "0";
            lastName = payPalAccountNonce.getLastName()!=null?payPalAccountNonce.getLastName() : "0";
            phone = payPalAccountNonce.getPhone()!=null?payPalAccountNonce.getPhone() : "0";
        }

        PaymentConfirm paymentConfirm = new PaymentConfirm();
        //paymentConfirm.setPayment_method_nonce(paymentMethodNonce);
        paymentConfirm.setNonce(nonce);
        paymentConfirm.setPurchase_total(PaymentsUtil.print(total_USD));
        paymentConfirm.setPurchase_total_sar(PaymentsUtil.print(total_SAR));

        paymentConfirm.setItems_total(PaymentsUtil.print(items_total_USD));
        paymentConfirm.setItems_total_sar(PaymentsUtil.print(items_total_SAR));

        if(checkout_by.equals("MyWallet")){
            paymentConfirm.setAmount(PaymentsUtil.print(total_USD));
            paymentConfirm.setAmount_sar(PaymentsUtil.print(total_SAR));
            paymentConfirm.setTransaction_fee("0.0");
            paymentConfirm.setTransaction_fee_sar("0.0");

        }else {
            paymentConfirm.setAmount(PaymentsUtil.print(final_total_USD));
            paymentConfirm.setAmount_sar(PaymentsUtil.print(total_SAR.add(PaymentsUtil.microsToString(PaymentsUtil.TRANSACTION_FEE))));
            paymentConfirm.setTransaction_fee(PaymentsUtil.print(transaction_fee_usd));
            paymentConfirm.setTransaction_fee_sar(PaymentsUtil.TRANSACTION_FEE);
        }

        paymentConfirm.setConverter_currency("sar_usd");
        paymentConfirm.setCurrency(PaymentsUtil.print(PaymentsUtil.getPayPal_SAR_USD()));

        paymentConfirm.setUser_id(user_id);
        paymentConfirm.setUser_name(userAccount.getUsername());
        paymentConfirm.setFull_user_name(firstName + " " +lastName);
        paymentConfirm.setEmail(email);
        paymentConfirm.setPhone(phone);
        paymentConfirm.setUser_country(userAccount.getLocation_information().getCountry_code());
        paymentConfirm.setUser_city(userAccount.getLocation_information().getCity_name());
        paymentConfirm.setEntage_page_user_id(entagePage_userId);
        paymentConfirm.setEntage_page_id(entage_page_id);
        paymentConfirm.setOrder_id(order_id);
        paymentConfirm.setMessage_id(message_id);
        paymentConfirm.setPayment_id(payment_id);
        paymentConfirm.setOrder_number(order.getOrder_number()+"");
        paymentConfirm.setShipping_amount(PaymentsUtil.converter_SAR_USD_print(shipping_total));
        paymentConfirm.setItems_count(order.getItem_orders().size());
        paymentConfirm.setItems_information(items_information);
        paymentConfirm.setCheckout_by(checkout_by);
        paymentConfirm.setTime(DateTime.getTimestamp());
        paymentConfirm.setPayment_number(ServerValue.TIMESTAMP);
        paymentConfirm.setPayment_for(PAYMENT_FOR);



        ref_payments_processes
                .child(payment_id)
                .setValue(paymentConfirm)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    ref_paymentsSucceed = mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_payments_succeed))
                            .child(PAYMENT_FOR)
                            .child(user_id)
                            .child(payment_id);
                    listener_paymentsSucceed = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                //String clientTokenId = (String) dataSnapshot.getValue();
                                Log.d(TAG, "NONCE Succeed: " + dataSnapshot.getValue());
                                payments_status = getString(R.string.dbname_payments_succeed);
                                openPaymentSucceedDialog();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    };
                    ref_paymentsSucceed.addValueEventListener(listener_paymentsSucceed);

                    ref_paymentsError = mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_payments_failed))
                            .child(PAYMENT_FOR)
                            .child(user_id)
                            .child(order_id)
                            .child(payment_id);
                    listener_paymentsError = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String message = mContext.getString(R.string.happened_wrong_try_again);
                                if(dataSnapshot.child("message").exists()){
                                    message = (String) dataSnapshot.child("message").getValue();
                                    if(message.equals("PayPal Validation Failed")){
                                        message = mContext.getString(R.string.payPal_validation_failed);
                                    }
                                    else if(message.equals("Funding Instrument In The PayPal Account Was Declined By The Processor Or Bank, Or It Can't Be Used For This Payment")){
                                        message = mContext.getString(R.string.payPal_account_declined);
                                    }
                                    else if(message.equals("payment claim is cancelled")){
                                        message = mContext.getString(R.string.payment_claim_is_cancelled);
                                    }
                                    else if(message.equals("no_balance_for_payment")){
                                        message = mContext.getString(R.string.no_balance_for_payment);
                                    }
                                }
                                payments_status = getString(R.string.dbname_payments_succeed);
                                databaseError(null, message);
                                cancelPaymentProcess();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    };
                    ref_paymentsError.addValueEventListener(listener_paymentsError);

                }else {
                    if(task.getException().getMessage().contains("Firebase Database error: Permission denied")){
                        databaseError(null, mContext.getString(R.string.user_unauthorized_to_pay));
                    }
                    else {
                        databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                    }
                    cancelPaymentProcess();
                }
            }
        });
    }

    private void openConfirmingPaymentDialog() {
        if(alertInit != null){
            alertInit.dismiss();
        }
        if(alertCancelled != null){
            alertCancelled.dismiss();
        }

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.wait_confirming_payment));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        /*builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });*/
        alertConfirmingPayment = builder.create();
        alertConfirmingPayment.setCanceledOnTouchOutside(false);
        alertConfirmingPayment.setCancelable(false);
        alertConfirmingPayment.show();
    }
    // END

    private void openPaymentSucceedDialog() {
        if(alertInit != null){
            alertInit.dismiss();
        }
        if(alertCancelled != null){
            alertCancelled.dismiss();
        }
        if(alertConfirmingPayment != null){
            alertConfirmingPayment.dismiss();
        }

        if(ref_paymentsSucceed != null){
            ref_paymentsSucceed.removeEventListener(listener_paymentsSucceed);
        }
        if(ref_paymentsError != null){
            ref_paymentsError.removeEventListener(listener_paymentsError);
        }

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.payment_succeed));
        _view.findViewById(R.id.progressBar).setVisibility(View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                onBackPressed();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");

        Intent intent = new Intent();
        intent.putExtra(getString(R.string.dbname_payments_processes), payments_status);
        intent.putExtra("message_id", message_id);
        setResult(99, intent);

        super.onBackPressed();
    }

    private void databaseError(DatabaseError databaseError, String error) {
        any_error = true;

        if(alertInit != null){
            alertInit.dismiss();
        }
        if(alertCancelled != null){
            alertCancelled.dismiss();
        }
        String msg = databaseError!=null? databaseError.getMessage() : error;
        findViewById(R.id.wait_order_data_preparing).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.wait_order_data_preparing)).getChildAt(0).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.error_1)).setText(getString(R.string.happened_wrong_title)+"\n"+msg);
        ((TextView)findViewById(R.id.error_1)).setTextColor(getColor(R.color.red));

        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_title)+"\n"+msg);
    }

    private void setErrorToDatabase(String title, String msg){
        Log.d(TAG, "setErrorToDatabase: ");
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_failed))
                .child(PAYMENT_FOR)
                .child(user_id)
                .child(order_id)
                .child(payment_id)
                .child(title)
                .setValue(new ErrorReport(title, msg, DateTime.getTimestamp()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(ref_paymentsSucceed != null){
            ref_paymentsSucceed.removeEventListener(listener_paymentsSucceed);
        }
        if(ref_paymentsError != null){
            ref_paymentsError.removeEventListener(listener_paymentsError);
        }
        if(mBraintreeFragment != null){
            mBraintreeFragment.removeListener(this);
        }
    }

    // database setup
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        user_id = mAuth.getCurrentUser().getUid();
        ref_payments_processes = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_processes))
                .child(PAYMENT_FOR)
                .child(user_id)
                .child(order_id);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user == null || user.isAnonymous()){
                    Toast.makeText(mContext, mContext.getString(R.string.error_operation),
                            Toast.LENGTH_SHORT).show();
                    finish();
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

    public class GenerateClientToken extends AsyncTask<String, String, String> {
        /**
         * Params : The type of the parameters sent to the task upon execution
         * Progress : The type of the progress units published during the background computation
         * Result : The type of the result of the background computation
         *
         * doInBackground() : This method contains the code which needs to be executed in background. In this method we can
         * send results multiple times to the UI thread by publishProgress() method. To notify that the background processing
         * has been completed we just need to use the return statements
         *
         * onPreExecute() : This method contains the code which is executed before the background processing starts
         *
         * onPostExecute() : This method is called after doInBackground method completes processing. Result from doInBackground
         * is passed to this method
         *
         * onProgressUpdate() : This method receives progress updates from doInBackground method, which is published via
         * publishProgress method, and this method can use this progress update to update the UI thread
         */

        GenerateClientToken(String url) {
            //this.execute(url);
            this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }

        /**  **/
        @Override
        protected String doInBackground(String... strings) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()

            //String token = gateway.clientToken().generate();
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorToDatabase("Error in fetching client token", "e: "+ response);
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                cancelPaymentProcess();
            }

            try {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                setErrorToDatabase("Error in fetching client token", "e: "+ response);
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                cancelPaymentProcess();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response)
        {
            //clientToken = searchResult;
            JSONObject mResponse = null;
            try {
                mResponse = new JSONObject(response);
                if(mResponse.get("clientToken")!=null){
                    if(!isDestroyed() && mContext!=null){
                        clientToken = (String) mResponse.get("clientToken");
                        setupBraintreeAndStartExpressCheckout();
                    }
                }else {
                    setErrorToDatabase("Error in fetching client token", "e: "+ response);
                    databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                    cancelPaymentProcess();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                setErrorToDatabase("Error in fetching client token", "e: "+ response);
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                cancelPaymentProcess();
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            if(!isDestroyed() && !isFinishing() && mContext!=null){
                setErrorToDatabase("Error in fetching client token cancelled", "Error "+s);
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                cancelPaymentProcess();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if(!isDestroyed() && mContext!=null){
                setErrorToDatabase("Error in fetching client token cancelled", "Error in fetching client token cancelled");
                databaseError(null, mContext.getString(R.string.happened_wrong_try_again));
                cancelPaymentProcess();
            }
        }
    }

    public class ConverterCurrency extends AsyncTask<ConvertCurrency, String, ConvertCurrency>  {
        /**
         * Params : The type of the parameters sent to the task upon execution
         * Progress : The type of the progress units published during the background computation
         * Result : The type of the result of the background computation
         *
         * doInBackground() : This method contains the code which needs to be executed in background. In this method we can
         * send results multiple times to the UI thread by publishProgress() method. To notify that the background processing
         * has been completed we just need to use the return statements
         *
         * onPreExecute() : This method contains the code which is executed before the background processing starts
         *
         * onPostExecute() : This method is called after doInBackground method completes processing. Result from doInBackground
         * is passed to this method
         *
         * onProgressUpdate() : This method receives progress updates from doInBackground method, which is published via
         * publishProgress method, and this method can use this progress update to update the UI thread
         */

        public ConverterCurrency(ConvertCurrency convertCurrency) {
            this.execute(convertCurrency);
        }

        @Override
        protected ConvertCurrency doInBackground(ConvertCurrency... convertCurrencies) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()

            HttpGet get = new HttpGet(Currencylayer.BASE_URL + Currencylayer.LIVE_ENDPOINT + "?access_key=" + Currencylayer.ACCESS_KEY
                    + "&currencies="
                    + convertCurrencies[0].getToCurrency().getCurrencyCode() + "&source=" +
                    convertCurrencies[0].getFromCurrency() + "&amount=" + 1);
            Log.d(TAG, "doInBackground: " + get.toString());

            try {
                // this object is used for executing requests to the (REST) API
                convertCurrencies[0].setHttpClient(HttpClients.createDefault());

                convertCurrencies[0].setResponse(convertCurrencies[0].getHttpClient().execute(get));

            }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return convertCurrencies[0];
        }

        @Override
        protected void onPostExecute(ConvertCurrency convertCurrencies) {
          /*  try {
                if(convertCurrencies.getResponse() != null){
                    HttpEntity entity = convertCurrencies.getResponse().getEntity();

                    // the following line converts the JSON Response to an equivalent Java Object
                    JSONObject exchangeRates = new JSONObject(EntityUtils.toString(entity));

                    double quotes = exchangeRates.getJSONObject("quotes").getDouble(convertCurrencies.getQuotes());
                    Log.d(TAG, "onPostExecute: " + quotes);
                    double finalExchange = convertCurrencies.getAmount() * quotes;
                    final_total_USD = PaymentsUtil.microsToString(finalExchange,5);
                    *//*String price = new BigDecimal(finalExchange+"").setScale(2, BigDecimal.ROUND_HALF_EVEN)
                            .stripTrailingZeros().toPlainString();*//*

                    total_USD_text.setText(final_total_USD.doubleValue() + " " + convertCurrencies.getToCurrency().getDisplayName());

                    convertCurrencies.getResponse().close();
                    convertCurrencies.getHttpClient().close();
                }

            }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onProgressUpdate(String... text) { }
    }

    public static class ErrorReport {

        String title, message, time;

        public ErrorReport() {
        }

        public ErrorReport(String title, String message, String time) {
            this.title = title;
            this.message = message;
            this.time = time;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

}
