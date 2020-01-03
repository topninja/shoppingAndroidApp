package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;

public class FragmentPriceItem extends Fragment {
    private static final String TAG = "FragmentPriceItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;

    private InputMethodManager imm ;
    private AutoCompleteTextView currenciesPrice;
    private EditText priceItem;
    private String CurrencyItem;
    private ArrayList<String> currencies;
    private OptionsPrices optionsPrices;
    private MessageDialog messageDialog = new MessageDialog();

    private ArrayList<EditText> editTexts;
    private LinearLayout container;
    private String entagePageId, itemId, currencyPage;
    private boolean isDataFetched = true;

    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_price_item, container, false);
            mContext = getActivity();


            getIncomingBundle();
            setupFirebaseAuth();
            init();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    public void getIncomingBundle() {
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId = bundle.getString("entagePageId");
                itemId = bundle.getString(mContext.getString(R.string.field_item_id));
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        setupAdviceLayout();
        setupBarAddItemLayout();

        initWidgets();
        onClickListener();

        setupAdapters();

        fetchCurrencyUSD_SAR();
        getCurrencyPage();
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[6]);

        currenciesPrice = view.findViewById(R.id.currency_price);
        priceItem = view.findViewById(R.id.price_item);
        container = view.findViewById(R.id.container);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
    }

    private void onClickListener(){

    }

    private void setupAdapters(){
        // Adapter Currencies
        Locale[] locales = Locale.getAvailableLocales();
        currencies = new ArrayList<>();
        Currency currency ;
        for (Locale locale : locales) {

            try {
                currency = Currency.getInstance(locale);
                currencies.add(currency.getCurrencyCode()+" "+currency.getDisplayName());
            } catch (Exception ignored) { }
        }
        Collections.sort(currencies);
        currencies = removeDuplicates(currencies);

        // set currency that belongs to users country in first
        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        String countryUSer = null;
        if(globalVariable.getLocationInformation() != null){
            countryUSer = globalVariable.getLocationInformation().getCountry_code();
        }
        if(countryUSer != null){
            try {
                Currency curr = Currency.getInstance(new Locale(Locale.getDefault().getLanguage(), countryUSer));
                String currencyInList = curr.getCurrencyCode()+" "+curr.getDisplayName();
                currencies.remove(currencyInList);
                currencies.add(0, currencyInList);
            } catch (Exception ignored) { }
        }


        ArrayAdapter<String> adapter = new  ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, currencies);
        currenciesPrice.setAdapter(adapter);
        currenciesPrice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                keyboard(false);

                Object item = parent.getItemAtPosition(position);
                CurrencyItem = ((String)item).substring(0, 3); // get first three char
            }
        });
        currenciesPrice.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                currenciesPrice.showDropDown();
                return false;
            }
        });

        //currenciesPrice.setEnabled(false);
        currenciesPrice.setFocusable(false);
        //
    }

    private void fetchCurrencyUSD_SAR() {
        if(PaymentsUtil.getPayPal_SAR_USD() == null){
            FirebaseMethods.fetchCurrencyUSD_SAR(mContext);
        }
    }

    private void setupData(){
        CurrencyItem = currencyPage;
        Currency currency = Currency.getInstance(CurrencyItem);
        if(currency != null){
            CurrencyItem = currency.toString();
            int index = currencies.indexOf(CurrencyItem+" "+currency.getDisplayName());
            if(index != -1){
                currenciesPrice.setText(CurrencyItem+" "+currency.getDisplayName(),false);
            }
        }

        if(optionsPrices != null){

            if(optionsPrices.getOptions() != null){
                view.findViewById(R.id.relLayou_price).setVisibility(View.GONE);

                editTexts = new ArrayList<>();
                if(optionsPrices.getOptions().size() == 1){
                    setLayouts(optionsPrices.getOptions().get(0));

                }else if(optionsPrices.getLinkingOptions() != null) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    for(int i = 0; i< optionsPrices.getLinkingOptions().size(); i++){
                        arrayList.add(StringManipulation.listToString(optionsPrices.getLinkingOptions().get(i), " : ").substring(3));
                    }
                    setLayouts(arrayList);
                }

            } else {
                priceItem.setText(optionsPrices.getMain_price().equals("0")? "" : String.valueOf(optionsPrices.getMain_price()));
            }

        }else {

            priceItem.setText("");
        }
    }

    private void setLayouts(ArrayList<String> arrayList){
        View v = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_price_option, null, false);
        ((TextView) v.findViewById(R.id.text)).setText((mContext.getString(R.string.item_options).substring(0,mContext.getString(R.string.item_options).length()-1)));
        ((TextView) v.findViewById(R.id.text)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ((EditText) v.findViewById(R.id.price)).setText(mContext.getString(R.string.the_price));
        ((EditText) v.findViewById(R.id.price)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        v.findViewById(R.id.price).setBackground(null);
        v.findViewById(R.id.price).setEnabled(false);
        ((RadioButton)v.findViewById(R.id.radio)).setText(mContext.getString(R.string.default_option));
        ((RadioButton)v.findViewById(R.id.radio)).setButtonDrawable(null);
        final CheckBox checkBox =  v.findViewById(R.id.checkBox);
        checkBox.setVisibility(View.VISIBLE);
        v.findViewById(R.id.line).setVisibility(View.VISIBLE);
        container.addView(v);

        // options
        final ArrayList<RadioButton> radioButtons = new ArrayList<>();
        for(int i=0; i<arrayList.size(); i++){
            View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_price_option, null, false);
            ((TextView) _view.findViewById(R.id.text))
                    .setText(arrayList.get(i));

            RadioButton rButton = _view.findViewById(R.id.radio);
            radioButtons.add(rButton);
            final EditText editText = _view.findViewById(R.id.price);
            final TextView usd_sar =  _view.findViewById(R.id.usd_sar);
            usd_sar.setVisibility(View.VISIBLE);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    String text = editText.getText().toString();

                    String price =  "0.0";
                    if(text.length()>0 && !text.equals(".")){
                        price = text;
                    }

                    usd_sar.setText(PaymentsUtil.converter_SAR_USD_print(price) + "  USD");
                }
                @Override
                public void afterTextChanged(Editable editable) { }
            });

            editText.setText(optionsPrices.getPrices()==null? "" : String.valueOf(optionsPrices.getPrices().get(i)));
            editTexts.add(editText);
            final int finalI = i;
            rButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if(checkBox.isChecked()){
                            int x = Integer.parseInt(optionsPrices.getDefault_option());
                            editTexts.get(x).setTextColor(mContext.getColor(R.color.gray0));
                            editTexts.get(x).setEnabled(false);
                        }

                        optionsPrices.setDefault_option(String.valueOf(finalI));
                        editText.setTextColor(mContext.getColor(R.color.black));
                        editText.setEnabled(true);
                        editText.requestFocus();
                        for(int r=0; r<radioButtons.size(); r++){
                            if(radioButtons.get(r) != buttonView){
                                radioButtons.get(r).setChecked(false);
                            }
                        }
                    }
                }
            });
            if(i==arrayList.size()-1){
                _view.findViewById(R.id.line).setVisibility(View.VISIBLE);
            }

            container.addView(_view);
        }

        if(optionsPrices.getDefault_option()!=null){
            int x = Integer.parseInt(optionsPrices.getDefault_option());
            radioButtons.get(x).setChecked(true);
        }else {
            radioButtons.get(0).setChecked(true);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                optionsPrices.setIs_price_unify(isChecked);
                if(isChecked){
                    int default_option = Integer.parseInt(optionsPrices.getDefault_option());
                    for(int r=0; r<editTexts.size(); r++){
                        editTexts.get(r).setTextColor(r==default_option? mContext.getColor(R.color.black):mContext.getColor(R.color.gray0));
                        editTexts.get(r).setEnabled(r==default_option);
                    }
                }else {
                    for(int r=0; r<editTexts.size(); r++){
                        editTexts.get(r).setTextColor(mContext.getColor(R.color.black));
                        editTexts.get(r).setEnabled(true);
                    }
                }
            }
        });

        checkBox.setChecked(optionsPrices.isIs_price_unify());
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

    // Function to remove duplicates from an ArrayList
    private static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {

        // Create a new ArrayList
        ArrayList<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }

    private boolean checkNumberPrice(String number){
        return number.length()!=0 && !number.equals(".") && Double.valueOf(number)>0;
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_options_prices));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_options_prices));

        myRefLastEdit = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_last_edit_was_in));


        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void getCurrencyPage(){
        showProgress(false, false);

        //selectedOptionsFromDb = new HashMap<>();

        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entagePageId)
                .child("currency_entage_page");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currencyPage = dataSnapshot.getValue(String.class);

                    getDataFromDbArchives();

                } else {
                    isDataFetched = false;
                    databaseError(null);
                    showProgress(true, false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });
    }

    private void getDataFromDbArchives(){
        showProgress(false, false);

        //selectedOptionsFromDb = new HashMap<>();

        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    optionsPrices = dataSnapshot.getValue(OptionsPrices.class);

                    setupData();
                    showProgress(true, false);

                }else {
                    getDataFromDbItems();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });
    }

    private void getDataFromDbItems(){
        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    optionsPrices = dataSnapshot.getValue(OptionsPrices.class);

                }

                setupData();
                showProgress(true, false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });
    }

    private void databaseError(DatabaseError databaseError){
        Log.d(TAG, "onCancelled: query cancelled");
        if(databaseError == null){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");

        } else  if(databaseError.getMessage().equals("Permission denied")){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied)+ "  " +
                    databaseError.getMessage());
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_msg)+ "  " +
                    databaseError.getMessage());
        }
    }

    private void saveData() {
        Log.d(TAG, "saveData: ");
        keyboard(false);

        if (CurrencyItem == null){
            (view.findViewById(R.id.error)).setVisibility(View.VISIBLE);
            Toast.makeText(mContext, getString(R.string.error_write_currencies),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if(optionsPrices !=null && optionsPrices.getOptionsTitle() != null){ // if there are options
            (view.findViewById(R.id.error)).setVisibility(View.GONE);

            ArrayList<String> mPrices = new ArrayList<>();
            if(optionsPrices.isIs_price_unify()){  // if price unify
                int u = Integer.parseInt(optionsPrices.getDefault_option());
                String prise = editTexts.get(u).getText().toString();
                if(!checkNumberPrice(prise)){
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_write_price));
                    return;
                }

                String _price = PaymentsUtil.print(PaymentsUtil.microsToString(prise));
                for(int i=0; i<editTexts.size(); i++){
                    editTexts.get(i).setText(_price+"");
                    mPrices.add(_price);
                }
                optionsPrices.setMain_price(_price);

            }else {
                int u = Integer.parseInt(optionsPrices.getDefault_option());
                for(int i=0; i<editTexts.size(); i++){
                    String prise = editTexts.get(i).getText().toString();
                    if(!checkNumberPrice(prise)){
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_write_price_some_options));
                        return;
                    }else {
                        if(u==i){

                            optionsPrices.setMain_price(PaymentsUtil.print(PaymentsUtil.microsToString(prise))); // to scale it to 2
                        }
                        mPrices.add(i, PaymentsUtil.print(PaymentsUtil.microsToString(prise))); // to scale it to 2
                        editTexts.get(i).setText(mPrices.get(i)+"");
                    }
                }
            }

            optionsPrices.setPrices(mPrices);
            optionsPrices.setCurrency_price(CurrencyItem);
        }
        else {
            if(priceItem.getText() == null || !checkNumberPrice(priceItem.getText().toString())){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_write_price));
                return;
            }

            String _price = PaymentsUtil.print(PaymentsUtil.microsToString(priceItem.getText().toString()));
            optionsPrices = new OptionsPrices(null,null, null, null,
                    _price, CurrencyItem, null, true);
            priceItem.setText(_price);
        }

        showProgress(false, true);
        myRefArchives.setValue(optionsPrices)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            updateTimeLastEdit();
                            Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                    Toast.LENGTH_SHORT).show();

                        }else {
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) +
                                    task.getException().getMessage());
                        }

                        showProgress(true, true);
                    }
                });
    }

    private void updateTimeLastEdit(){
        myRefLastEdit.setValue(DateTime.getTimestamp());
    }


    // setup Bar Add Item Layout
    private TextView save;
    private RelativeLayout nextStep;
    private ImageView tips;
    private void setupBarAddItemLayout(){
        save = view.findViewById(R.id.save);
        nextStep = view.findViewById(R.id.next_step);
        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_shipping_item);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataFetched){
                    saveData();
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");
                }
            }
        });

        nextStep.findViewById(R.id.next_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentShippingItem());
            }
        });
    }

    private void showProgress(boolean boo, boolean all){
        if(all){
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            nextStep.setEnabled(boo);
            nextStep.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            tips.setEnabled(boo);

        }else {
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        }

        //
    }

    private void setupAdviceLayout(){
        tips = view.findViewById(R.id.tips);
        final String title = mContext.getResources().getStringArray(R.array.advices)[6];
        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices(), bundle);
            }
        });
        view.findViewById(R.id.advice_oky).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilitiesMethods.collapse(view.findViewById(R.id.advice_linear_layout));
            }
        });
        view.findViewById(R.id.advice_see_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices(), bundle);
            }
        });
        ((TextView)view.findViewById(R.id.advice_title)).setText(title);
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_price_item)[0]);
    }
}
