package com.entage.nrd.entage.payment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
import com.entage.nrd.entage.Models.PaymentConfirm;
import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.home.ActivityForOpenFragments;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.entage.nrd.entage.adapters.AdapterPackages.RB1_ID;
import static com.entage.nrd.entage.adapters.AdapterPackages.RB2_ID;
import static com.entage.nrd.entage.adapters.AdapterPackages.RB3_ID;

/*
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
*/


public class SubscribeActivity extends AppCompatActivity  implements PaymentMethodNonceCreatedListener, ConfigurationListener,
        BraintreeCancelListener, BraintreeErrorListener {
    private static final String TAG = "DepositFundsActivity";


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference  ref_payments_processes, ref_paymentsSucceed, ref_paymentsError;
    private ValueEventListener listener_paymentsSucceed, listener_paymentsError;
    private String user_id;
    private String entajiPagesId;

    public static final String GENERATE_CLIENT_TOKEN_URL = "https://us-central1-entage-1994.cloudfunctions.net/generateClientTokenId";
    public final String PAYMENT_FOR = "payments_subscribe";

    private Context mContext ;
    private AppCompatButton pay_paypal, pay_mastercard;
    private TextView  total_sar, total_usd;
    TextView package_name, subscribe_descr;
    private boolean any_error = false;
    private boolean cancelPayment = false;
    private BigDecimal purchase_total_SAR, purchase_total_USD, final_amount_SAR, final_amount_USD;

    private GlobalVariable globalVariable;
    private String  payment_id, clientToken;

    private AlertDialog alertCancelled, alertInit, alertConfirmingPayment;
    private MessageDialog messageDialog = new MessageDialog();

    private OkHttpClient client;
    private BraintreeFragment mBraintreeFragment;
    private String payments_status;

    private SubscriptionPackage mCurrentSubscriptionPackage;
    private SubscriptionPackage mPackageNewSubscriptionPackage;
    private int subscribe_type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_subscribe);
        mContext = SubscribeActivity.this;

        backArrow();
        getIncomingBundle();

    }

    private void getIncomingBundle(){
        entajiPagesId = getIntent().getStringExtra("entajiPagesId");
        mPackageNewSubscriptionPackage = getIntent().getParcelableExtra("package");
        subscribe_type = getIntent().getIntExtra("subscribe_type", -1);

        if(entajiPagesId !=null && mPackageNewSubscriptionPackage!=null && subscribe_type != -1){
            setupFirebaseAuth();
            init();

        }else {
            findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);

        }
    }

    private void backArrow(){
        ((TextView)findViewById(R.id.titlePage)).setText(mContext.getString(R.string.subscribe_enage_page_title));
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
        initWidgets();
        setupOkHttpClient();

        fetchCurrencyUSD_SAR();
    }

    private void initWidgets() {
        package_name = findViewById(R.id.package_name);
        subscribe_descr = findViewById(R.id.subscribe_descr);

        total_sar = findViewById(R.id.the_total);
        total_usd = findViewById(R.id.the_total_usd);

        pay_paypal = findViewById(R.id.pay_paypal);
        pay_mastercard = findViewById(R.id.pay_mastercard);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        findViewById(R.id.have_problem_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(mContext, ActivityForOpenFragments.class);
                intent.putExtra("notification_flag", "problem");
                intent.putExtra("typeProblem", mContext.getString(R.string.pay_problems));
                startActivity(intent);
            }
        });
    }

    private void getCurrentSubscription(){
        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_subscription))
                .child(entajiPagesId)
                .child(mContext.getString(R.string.dbname_current_subscription))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                           mCurrentSubscriptionPackage = dataSnapshot.getValue(SubscriptionPackage.class);

                            setupData();
                        }
                        findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void fetchCurrencyUSD_SAR() {
        // check status of the store
        // first get USD_SAR
        if(PaymentsUtil.PayPal_SAR_USD != null){

            getCurrentSubscription();

        }else{
            mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_app_data))
                .child("usd_sar")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            PaymentsUtil.setPayPal_SAR_USD((String) dataSnapshot.getValue());

                            getCurrentSubscription();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        findViewById(R.id.wait_order_data_preparing).setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                    }
                });
        }
    }

    private void setupData(){
        String price = null;
        if(subscribe_type == RB1_ID){
            subscribe_descr.setText(  mPackageNewSubscriptionPackage.getPrice_3() + " ر.س" + " / "+
                    "3 "+ mContext.getString(R.string.months));
            price = mPackageNewSubscriptionPackage.getPrice_3();
        }
        else if(subscribe_type == RB2_ID){
            subscribe_descr.setText(  mPackageNewSubscriptionPackage.getPrice_6() + " ر.س" + " / "+
                    "6 "+ mContext.getString(R.string.months));
            price = mPackageNewSubscriptionPackage.getPrice_6();
        }
        else if(subscribe_type == RB3_ID){
            subscribe_descr.setText(  mPackageNewSubscriptionPackage.getPrice_12() + " ر.س" + " / "+
                    "12 "+ mContext.getString(R.string.months));
            price = mPackageNewSubscriptionPackage.getPrice_12();

        }
        else {
            return;
        }
        package_name.setText(mContext.getString(R.string.the_package)+": "+mPackageNewSubscriptionPackage.getPackage_name());

        purchase_total_SAR =  PaymentsUtil.microsToString(price);
        purchase_total_USD = PaymentsUtil.converter_SAR_USD(purchase_total_SAR);

        total_sar.setText(PaymentsUtil.print(purchase_total_SAR) + " " + Currency.getInstance("SAR").getDisplayName());
        total_usd.setText(PaymentsUtil.print(purchase_total_USD) + " " + Currency.getInstance("USD").getDisplayName());
        total_usd.setVisibility(View.VISIBLE);

        BigDecimal fee = PaymentsUtil.microsToString(PaymentsUtil.TRANSACTION_FEE);
        final_amount_SAR = purchase_total_SAR.add(fee);
        final_amount_USD = PaymentsUtil.converter_SAR_USD(final_amount_SAR);

        setOnClickListener();
    }

    private void setOnClickListener(){
        pay_paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!any_error){

                    pay_paypal.setClickable(false);
                    pay_mastercard.setClickable(false);

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
        fetchClientToken();

        alertInit.show();
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

    // step one
    private void fetchClientToken(){
        Log.d(TAG, "fetchClientToken: ");

        payment_id = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_processes))
                .push().getKey();
        if (payment_id != null && user_id!=null){
            if(clientToken != null){
                setupBraintreeAndStartExpressCheckout();

            }else {
                new GenerateClientToken(GENERATE_CLIENT_TOKEN_URL);
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
        Log.d(TAG, "ClientToken: " + final_amount_USD.toString());
        try {
            mBraintreeFragment = BraintreeFragment.newInstance(this, clientToken);
            // mBraintreeFragment is ready to use!
            PayPalRequest request = new PayPalRequest(PaymentsUtil.print(final_amount_USD))
                    .currencyCode("USD")
                    .intent(PayPalRequest.INTENT_SALE)
                    .offerCredit(true); // Offer PayPal Credit;
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

            postNonceToServer(paymentMethodNonce, nonce);

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
    private void postNonceToServer(PaymentMethodNonce paymentMethodNonce, String nonce){
        Log.d(TAG, "postNonceToServer: ");
        openConfirmingPaymentDialog();

        PayPalAccountNonce payPalAccountNonce = (PayPalAccountNonce)paymentMethodNonce;
        String email = payPalAccountNonce.getEmail();
        String firstName = payPalAccountNonce.getFirstName();
        String lastName = payPalAccountNonce.getLastName();
        String phone = payPalAccountNonce.getPhone();


        String fName = firstName + " " +lastName;

        PaymentConfirm paymentConfirm = new PaymentConfirm();
        //paymentConfirm.setPayment_method_nonce(paymentMethodNonce);
        paymentConfirm.setNonce(nonce);
        paymentConfirm.setPurchase_total(PaymentsUtil.print(purchase_total_USD));
        paymentConfirm.setAmount(PaymentsUtil.print(final_amount_USD));
        paymentConfirm.setTransaction_fee(PaymentsUtil.converter_SAR_USD_print(PaymentsUtil.TRANSACTION_FEE));

        paymentConfirm.setPurchase_total_sar(PaymentsUtil.print(purchase_total_SAR));
        paymentConfirm.setAmount_sar(PaymentsUtil.print(final_amount_SAR));
        paymentConfirm.setTransaction_fee_sar(PaymentsUtil.TRANSACTION_FEE);

        paymentConfirm.setConverter_currency("sar_usd");
        paymentConfirm.setCurrency(PaymentsUtil.print(PaymentsUtil.getPayPal_SAR_USD()));

        paymentConfirm.setEntage_page_id(entajiPagesId);
        paymentConfirm.setSubscribe_type(subscribe_type);
        paymentConfirm.setCurrent_subscription(mCurrentSubscriptionPackage);
        paymentConfirm.setPackage_new_subscription(mPackageNewSubscriptionPackage);

        paymentConfirm.setUser_id(user_id);
        paymentConfirm.setFull_user_name(fName);
        paymentConfirm.setEmail(email);
        paymentConfirm.setPhone(phone);
        paymentConfirm.setPayment_id(payment_id);
        paymentConfirm.setCheckout_by("PayPal");
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

                    //
                    ref_paymentsError = mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_payments_failed))
                            .child(PAYMENT_FOR)
                            .child(user_id)
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
                                }
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
                    }else {
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
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.payment_subscription_succeed));
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

    private void databaseError(DatabaseError databaseError, String error) {
        any_error = true;

        if(alertInit != null){
            alertInit.dismiss();
        }
        if(alertCancelled != null){
            alertCancelled.dismiss();
        }

        String msg = databaseError!=null? databaseError.getMessage() : error;

        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_title)+"\n"+msg);
    }

    private void setErrorToDatabase(String title, String msg){
        Log.d(TAG, "setErrorToDatabase: ");
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_failed))
                .child(PAYMENT_FOR)
                .child(user_id)
                .child(payment_id)
                .child(title)
                .setValue(new ErrorReport(title, msg, DateTime.getTimestamp()));
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");

        Intent intent = new Intent();
        setResult(99, intent);

        super.onBackPressed();
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

        user_id = mAuth.getCurrentUser().getUid();
        ref_payments_processes = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_processes))
                .child(PAYMENT_FOR)
                .child(user_id);

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
            this.execute(url);
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
            }

            try {
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
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
