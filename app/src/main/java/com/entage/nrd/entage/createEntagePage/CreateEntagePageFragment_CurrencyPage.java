package com.entage.nrd.entage.createEntagePage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.GlobalVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;

public class CreateEntagePageFragment_CurrencyPage extends Fragment{
    private static final String TAG = "CreateEntagePageFragmen";

    private View view;
    private Context mContext;
    private OnActivityListener onActivityListener;
    private CreateEntagePageListener mCreateEntagePageListener;

    private EditText captionOfPage;
    private TextView messageError;
    private AutoCompleteTextView autoCurrency;
    private String CurrencyItem;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_entage_page, container , false);
        mContext = getActivity();

        inti();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            onActivityListener = (OnActivityListener) getActivity();
            mCreateEntagePageListener = (CreateEntagePageListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti(){
        initWidgets();

        onClickListener();
    }

    private void initWidgets() {
        captionOfPage = view.findViewById(R.id.edit_text);
        messageError = view.findViewById(R.id.messageError);
        captionOfPage.setVisibility(View.GONE);

        autoCurrency  = view.findViewById(R.id.currency);
        autoCurrency.setVisibility(View.VISIBLE);

        view.findViewById(R.id.linearLayout_1).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.currency_entage_page));
        ((TextView)view.findViewById(R.id.text2)).setText(mContext.getString(R.string.creat_entage_page_currency));
        ((ImageView)view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_currency_1);

        mCreateEntagePageListener.show_hideBar(true);

        setupAdapters();
    }

    private void setupAdapters(){
        // Adapter Currencies
        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> currencies = new ArrayList<>();
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
        String countryUSer = globalVariable.getLocationInformation().getCountry_code();
        if(countryUSer != null){
            try {
                Currency curr = Currency.getInstance(new Locale(Locale.getDefault().getLanguage(), countryUSer));
                String currencyInList = curr.getCurrencyCode()+" "+curr.getDisplayName();
                currencies.remove(currencyInList);
                currencies.add(0, currencyInList);
            } catch (Exception ignored) { }
        }

        Currency curr = Currency.getInstance(new Locale(Locale.getDefault().getLanguage(), "SA"));
        final String SAR = curr.getCurrencyCode()+" "+curr.getDisplayName();

        ArrayAdapter<String> adapter = new  ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, currencies);
        autoCurrency.setAdapter(adapter);
        autoCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                keyboard(false);

                Object item = parent.getItemAtPosition(position);
                CurrencyItem = ((String)item).substring(0, 3); // get first three char
                if(!CurrencyItem.equals("SAR")){
                    messageError.setText(mContext.getString(R.string.currency_available_saudi_riyal));
                    CurrencyItem = "SAR";
                    autoCurrency.setText(SAR);
                }else {
                    messageError.setText("");
                }
            }
        });
        autoCurrency.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                autoCurrency.showDropDown();
                return false;
            }
        });
        //
    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(null);
        mCreateEntagePageListener.getNextButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(false);
                setEnableButtons(false);
                messageError.setText("");

                if(autoCurrency.getText().length() != 0 && CurrencyItem !=  null && CurrencyItem.length() == 3){
                    setEnableButtons(true);

                    mCreateEntagePageListener.getEntagePage().setCurrency_entage_page("SAR");


                    onActivityListener.onActivityListener(new CreateEntagePageFragment_Final());


                }else {
                    setEnableButtons(true);
                    messageError.setText(mContext.getString(R.string.creat_entage_page_currency));
                }
            }
        });
    }

    private void setEnableButtons(boolean enable){
        mCreateEntagePageListener.getNextButton().setEnabled(enable);
        if(enable){
            mCreateEntagePageListener.getNextButton().getChildAt(1).setVisibility(View.GONE);
            mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue));
        }else {
            mCreateEntagePageListener.getNextButton().getChildAt(1).setVisibility(View.VISIBLE);
            mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue_ops));
        }
    }

    private void keyboard(boolean show){
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
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

}
