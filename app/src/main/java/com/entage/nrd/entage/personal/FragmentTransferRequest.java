package com.entage.nrd.entage.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.TotalAmounts;
import com.entage.nrd.entage.Models.TransferMoneyRequest;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.payment.PaymentsUtil;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class FragmentTransferRequest extends Fragment {
    private static final String TAG = "FragmentTransferRequest";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private TextView available_amount, transfer_fee, transfer_time, information, send, to_SAR;
    private EditText amount;
    private AutoCompleteTextView email_of_paypal, confirm_email_of_paypal, bank_name, bank_account_iban, bank_account_number,
            bank_account_name;
    private Spinner spinner, countriesSpinner;
    private LinearLayout layout_info_account_paypal, layout_info_account_bank;
    private BigDecimal total_sar, total_usd;

    private String transfer_by, transaction_fee;

    private View view;
    private Context mContext;

    private InputMethodManager imm ;
    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_transfer_request, container , false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    private void inti() {
        initWidgets();

        setupAdapter();

        fetchCurrencyUSD_SAR();
        getSavedData();
    }

    private void initWidgets() {
        TextView titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.new_transfer_money_request));

        ImageView backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        available_amount = view.findViewById(R.id.available_amount);
        transfer_fee = view.findViewById(R.id.transfer_fee);
        transfer_time = view.findViewById(R.id.transfer_time);
        information = view.findViewById(R.id.information);
        send = view.findViewById(R.id.send);

        amount = view.findViewById(R.id.amount);
        amount.setEnabled(false);
        amount.setHint(mContext.getString(R.string.the_amount).substring(0, mContext.getString(R.string.the_amount).length()-1));

        layout_info_account_paypal = view.findViewById(R.id.layout_info_account_paypal);
        layout_info_account_bank = view.findViewById(R.id.layout_info_account_bank);

        email_of_paypal = view.findViewById(R.id.email_of_paypal);
        confirm_email_of_paypal = view.findViewById(R.id.confirm_email_of_paypal);

        bank_name = view.findViewById(R.id.bank_name);
        bank_account_iban = view.findViewById(R.id.bank_account_iban);
        bank_account_number = view.findViewById(R.id.bank_account_number);
        bank_account_name = view.findViewById(R.id.bank_account_name);
        countriesSpinner = view.findViewById(R.id.countriesSpinner);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        spinner = view.findViewById(R.id.spinner);

        to_SAR = view.findViewById(R.id.to_SAR);
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = amount.getText().toString();

                String price =  "0.00";
                if(text.length()>0 && !text.equals(".")){
                    price = text;
                }

                to_SAR.setText(PaymentsUtil.converter_USD_SAR_print(price)+ "  SAR");
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });
    }

    private void onClickListener() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send.setClickable(false);
                send.setVisibility(View.INVISIBLE);
                sendTransferRequest();
            }
        });
    }

    private void setupAdapter(){
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(" - ");
        arrayList.add(mContext.getString(R.string.to_my_paypal_account));
        //arrayList.add(mContext.getString(R.string.to_my_bank_account));

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(mContext, R.layout.spinner_item_selected, arrayList);
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinner.setAdapter(adapterSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position );

                if(position == 0){
                    if(layout_info_account_paypal.getVisibility()  == View.VISIBLE){
                        UtilitiesMethods.collapse(layout_info_account_paypal);
                    }
                    if(layout_info_account_bank.getVisibility()  == View.VISIBLE){
                        UtilitiesMethods.collapse(layout_info_account_bank);
                    }
                    transfer_fee.setText("0%");
                    transfer_time.setText("-");

                }else if (position == 1){ // to_my_paypal_account
                    if(layout_info_account_bank.getVisibility()  == View.VISIBLE){
                        UtilitiesMethods.collapse(layout_info_account_bank);
                    }
                    UtilitiesMethods.expand(layout_info_account_paypal);
                    transfer_fee.setText(mContext.getString(R.string.transfer_fee_paypal));
                    transfer_time.setText(mContext.getString(R.string.transfer_time_1));
                    transfer_by = "PayPal";
                    transaction_fee = "PayPal";

                }else if (position == 2){ // to_my_bank_account
                    if(layout_info_account_paypal.getVisibility()  == View.VISIBLE){
                        UtilitiesMethods.collapse(layout_info_account_paypal);
                    }
                    UtilitiesMethods.expand(layout_info_account_bank);

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Adapter Countries
        /*GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        String countryUser = null;
        if(globalVariable.getLocationInformation() != null){
            countryUser = globalVariable.getLocationInformation().getCountry_code();
        }*/
        ArrayList<String> countriesNames = CountriesData.getCountriesNames(null);

        ArrayAdapter<String> adapterCountries = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, countriesNames);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countriesSpinner.setAdapter(adapterCountries);

        countriesSpinner.setSelection(178); // saudi arabia
        //
    }

    private void getSavedData(){
        // get email of paypal if exist
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_balance_accounts))
                .child(user_id)
                .child("paypal")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            ArrayList<String> emails = new ArrayList<>();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                String email = (String) snapshot.child("email").getValue();
                                emails.add(email);
                            }

                            //
                            setupAutoCompleteTextView(email_of_paypal, emails);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void fetchCurrencyUSD_SAR() {
        // first get USD_SAR
        if(PaymentsUtil.PayPal_SAR_USD != null){
            getBalance();

        }else {
            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_app_data))
                    .child("usd_sar")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                PaymentsUtil.setPayPal_SAR_USD((String) dataSnapshot.getValue());

                                getBalance();
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

    private void getBalance(){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount_total))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            total_sar = PaymentsUtil.microsToString("0.00");
                            total_usd = PaymentsUtil.microsToString("0.00");

                            for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                                TotalAmounts total = singleSnapshot.getValue(TotalAmounts.class);

                                total_sar = total_sar.add(PaymentsUtil.microsToString(total.getTotal_sar()));
                                total_usd = total_usd.add(PaymentsUtil.microsToString(total.getTotal_usd()));
                            }

                            available_amount.setText(PaymentsUtil.print(total_sar)+
                                    " SAR  |  "+ PaymentsUtil.print(total_usd)+" USD");
                            amount.setEnabled(true);
                        }

                        view.findViewById(R.id.progressBar_loading).setVisibility(View.GONE);

                        if(total_usd == null || total_usd.doubleValue() == 0.0){
                            noBalance();
                        }else {
                            onClickListener();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        view.findViewById(R.id.progressBar_loading).setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void setupAutoCompleteTextView(final AutoCompleteTextView autoCompleteTextView, ArrayList<String> arrayList){
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, arrayList);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                keyboard(false);
            }
        });
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                autoCompleteTextView.showDropDown();
                return false;
            }
        });
    }

    private void noBalance(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        //builder.setTitle(mContext.getString(R.string.no_balance_to_transfer));
        builder.setMessage(mContext.getString(R.string.no_balance_to_transfer));
        builder.setNegativeButton(mContext.getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void keyboard(boolean show){
        //InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(show){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }else {
            // HideSoftInputFromWindow
            View v = ((Activity)mContext).getCurrentFocus();
            if (v == null) {
                v = new View(mContext);
            }
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void sendTransferRequest()  {
        if(spinner.getSelectedItemPosition() == 0){
            send.setClickable(true);
            send.setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, getString(R.string.select_transfer_money_to));
            return;
        }
        if(email_of_paypal.getText().length() < 8 || confirm_email_of_paypal.getText().length() < 8){
            send.setClickable(true);
            send.setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, getString(R.string.error_email_wrong));
            return;
        }
        if(!email_of_paypal.getText().toString().equals(confirm_email_of_paypal.getText().toString())){
            send.setClickable(true);
            send.setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, getString(R.string.email_not_match));
            return;
        }
        if(amount.getText().toString().length() == 0 || amount.getText().toString().equals(".") ||
                Double.parseDouble(amount.getText().toString()) <=0){
            send.setClickable(true);
            send.setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, getString(R.string.enter_valid_amount));
            return;
        }
        if(Double.parseDouble(amount.getText().toString()) > total_usd.doubleValue()){
            send.setClickable(true);
            send.setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, getString(R.string.transfer_money_bigger_than_available));
            return;
        }


        //send
        final DatabaseReference reference  = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_transfer_money_request));
        String key = reference.push().getKey();
        Object o = ServerValue.TIMESTAMP;
        BigDecimal _amount = PaymentsUtil.microsToString(amount.getText().toString());
        TransferMoneyRequest request = new TransferMoneyRequest(user_id, transfer_by, email_of_paypal.getText().toString(),
                PaymentsUtil.print(_amount), PaymentsUtil.print(total_sar), PaymentsUtil.print(total_usd),
                transaction_fee, "USD",
                "init",
                DateTime.getTimestamp(), o, o, key);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_transfer_money_request))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_open_request))
                .child(key)
                .setValue(request);
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_transfer_money_request))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_requests))
                .child(key)
                .setValue(request)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        send.setClickable(true);
                        send.setVisibility(View.VISIBLE);
                        if(task.isSuccessful()){
                            successfullySend();

                        }else {
                            messageDialog.errorMessage(mContext, getString(R.string.happened_wrong_try_again));
                        }
                    }
                });
    }

    private void successfullySend(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        //builder.setTitle(mContext.getString(R.string.no_balance_to_transfer));
        builder.setMessage(mContext.getString(R.string.successfully_send));
        builder.setNegativeButton(mContext.getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getActivity().onBackPressed();
            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    /*
   -------------------------------Firebase-------------------------------------------------------
   */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*myRef = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(amount_type);*/

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
