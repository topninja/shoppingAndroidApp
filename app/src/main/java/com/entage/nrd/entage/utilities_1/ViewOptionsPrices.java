package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entage.nrd.entage.Models.ConvertCurrency;
import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

public class ViewOptionsPrices {
    private static final String TAG = "ViewOptionsPrices";

    private Context mContext;
    private OptionsPrices optionsPrices;
    private LinearLayout container;
    private HashMap<String, String> selectedOptions;
    private HashMap<String, HashMap<String, CheckBox>> checkBoxes;
    private int textSizeOptions, layout;
    private ArrayList<CheckBox> hold = new ArrayList<>();
    private GlobalVariable globalVariable;
    private ArrayList<String> selectedOptionsList;

    private RelativeLayout priceLayout, currencyExchangeLayout;
    private ImageView line_currency;
    private TextView price_item, currency;
    private int textSizePrice, textColor;
    private String mPrice;
    private Animation animBlink;
    private boolean firstTime = true;

    private String currencyPrice;

    public ViewOptionsPrices(Context mContext, OptionsPrices optionsPrices, LinearLayout container, RelativeLayout priceLayout,
                             ArrayList<String> selectedOptionsList, int textSizeOptions, int textSizePrice, int textColor,
                             GlobalVariable globalVariable) {
        this.mContext = mContext;
        this.optionsPrices = optionsPrices;
        this.container = container;
        this.selectedOptionsList = selectedOptionsList;
        this.textSizeOptions = textSizeOptions;
        this.priceLayout = priceLayout;
        this.textSizePrice = textSizePrice;
        this.textColor = textColor;
        this.globalVariable = globalVariable;

        currencyExchangeLayout = (RelativeLayout)priceLayout.getChildAt(0);
        line_currency =(ImageView) currencyExchangeLayout.getChildAt(0);
        price_item = (TextView) priceLayout.getChildAt(1);
        currency = (TextView) priceLayout.getChildAt(2);

        checkBoxes = new HashMap<>();
        selectedOptions = new HashMap<>();

        layout = R.layout.layout_field_item_option;

        if(this.globalVariable == null){
            this.globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        }

        animBlink = AnimationUtils.loadAnimation(mContext, R.anim.blink);

        currencyPrice = optionsPrices.getCurrency_price();

        // check, in case user want to change order data but item has change its options to no options
        if(selectedOptionsList != null && optionsPrices.getLinkingOptions() == null){
            this.selectedOptionsList = new ArrayList<>();
        }

        setupOptions();
    }

    public ViewOptionsPrices(Context mContext, OptionsPrices optionsPrices, RelativeLayout priceLayout, int textSizePrice, int textColor,
                             GlobalVariable globalVariable) {
        this.mContext = mContext;
        this.optionsPrices = optionsPrices;
        this.priceLayout = priceLayout;
        this.textSizePrice = textSizePrice;
        this.globalVariable = globalVariable;
        this.textColor = textColor;

        currencyExchangeLayout = (RelativeLayout)priceLayout.getChildAt(0);
        line_currency =(ImageView) currencyExchangeLayout.getChildAt(0);
        price_item = (TextView) priceLayout.getChildAt(1);
        currency = (TextView) priceLayout.getChildAt(2);

        if(globalVariable == null){
            this.globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        }

        animBlink = AnimationUtils.loadAnimation(mContext, R.anim.blink);

        if(optionsPrices != null){
            currencyPrice = optionsPrices.getCurrency_price();
            setupPrice(optionsPrices.getMain_price());
        }
    }

    // this use in adapterBasket
    public ViewOptionsPrices(Context mContext, OptionsPrices optionsPrices, LinearLayout container, RelativeLayout priceLayout,
                             ArrayList<String> selectedOptionsList, String price,  int textSizeOptions, int textSizePrice, int textColor,
                             GlobalVariable globalVariable) {
        this.mContext = mContext;
        this.optionsPrices = optionsPrices;
        this.container = container;
        this.selectedOptionsList = selectedOptionsList;
        this.textSizeOptions = textSizeOptions;
        this.priceLayout = priceLayout;
        this.textSizePrice = textSizePrice;
        this.textColor = textColor;
        this.globalVariable = globalVariable;

        currencyExchangeLayout = (RelativeLayout)priceLayout.getChildAt(0);
        line_currency =(ImageView) currencyExchangeLayout.getChildAt(0);
        price_item = (TextView) priceLayout.getChildAt(1);
        currency = (TextView) priceLayout.getChildAt(2);

        checkBoxes = new HashMap<>();
        selectedOptions = new HashMap<>();

        layout = R.layout.layout_field_item_option;

        if(this.globalVariable == null){
            this.globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        }

        animBlink = AnimationUtils.loadAnimation(mContext, R.anim.blink);

        currencyPrice = optionsPrices.getCurrency_price();

        ArrayList<ArrayList<String>> op = new ArrayList<>();
        for(int i=0; i<selectedOptionsList.size(); i++){
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(selectedOptionsList.get(i));
            op.add(arrayList);
        }
        this.optionsPrices.setOptions(op);

        setupOptions();

        setupPrice(price);
    }

    private void setupOptions() {
        if(optionsPrices.getOptionsTitle() != null){
            for(int i = 0; i< optionsPrices.getOptionsTitle().size(); i++) {
                final String title = optionsPrices.getOptionsTitle().get(i);
                ArrayList<String> options = optionsPrices.getOptions().get(i);

                com.nex3z.flowlayout.FlowLayout flowlayout = (com.nex3z.flowlayout.FlowLayout) ((Activity)mContext).getLayoutInflater().
                        inflate(R.layout.flowlayout, container, false);

                CheckBox _tv = (CheckBox) ((Activity)mContext).getLayoutInflater().
                        inflate(layout, flowlayout, false);
                _tv.setBackground(mContext.getDrawable(R.drawable.border_curve_bubble_text_gray));
                _tv.setTextColor(mContext.getColor(R.color.entage_blue));
                _tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeOptions);
                _tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                _tv.setText(title);
                flowlayout.addView(_tv);

                //final ArrayList<CheckBox> checkBoxes = new ArrayList<>();
                checkBoxes.put(title, new HashMap<String, CheckBox>());
                for(final String op: options){
                    CheckBox tv = (CheckBox) ((Activity)mContext).getLayoutInflater().
                            inflate(layout, flowlayout, false);
                    tv.setText(op);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeOptions);
                    //tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    flowlayout.addView(tv);
                    checkBoxes.get(title).put(op, tv);

                    final int finalI = i;
                    tv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                selectedOptions.put(title, op);
                                setAllFalse(finalI, title, buttonView);

                                //
                                checkOptions(finalI, op);

                            }else {
                                if(selectedOptions.get(title).equals(op)){
                                    buttonView.setChecked(true);
                                }
                            }
                        }
                    });
                }
                container.addView(flowlayout);
            }

            //
            if(optionsPrices.getOptionsTitle().size() > 1){
                // checking if there is option of first list not exit in any linking lists
                for(int i=0; i<optionsPrices.getOptions().get(0).size(); i++){
                    boolean available = false;
                    for(ArrayList<String> linkingList: optionsPrices.getLinkingOptions()) {
                        if(linkingList.get(0).equals(optionsPrices.getOptions().get(0).get(i))){
                            available = true;
                        }
                    }
                    checkBoxes.get(optionsPrices.getOptionsTitle().get(0)).get(optionsPrices.getOptions().get(0).get(i)).setEnabled(available);
                    if(!available){
                        checkBoxes.get(optionsPrices.getOptionsTitle().get(0)).get(optionsPrices.getOptions().get(0).get(i)).setTextColor(mContext.getColor(R.color.gray0));
                    }
                }

               /* // check first
                if(!optionsPrices.getLinkingOptions().contains(selectedOptionsList)){
                    this.selectedOptionsList = null;
                }*/

                // set default checked
                if(selectedOptionsList != null && selectedOptionsList.size() != 0){
                    ArrayList<String> arrayList = new ArrayList<>(selectedOptionsList);
                    for(int i=0; i<optionsPrices.getOptionsTitle().size(); i++){
                        checkBoxes.get(optionsPrices.getOptionsTitle().get(i)).get(arrayList.get(i)).setChecked(true);
                    }

                }else {
                    int _default = Integer.parseInt(optionsPrices.getDefault_option());
                    ArrayList<String> arrayList = new ArrayList<>(optionsPrices.getLinkingOptions().get(_default));
                    for(int i=0; i<optionsPrices.getOptionsTitle().size(); i++){
                        checkBoxes.get(optionsPrices.getOptionsTitle().get(i)).get(arrayList.get(i)).setChecked(true);
                    }
                    //checkBoxes.get(optionsPrices.getOptionsTitle().get(0)).get(optionsPrices.getOptions().get(0)
                          //  .get(index==-1? _default:index)).setChecked(true);
                }

            }
            else if(optionsPrices.getOptionsTitle().size() == 1){

                /*// check first
                if(!optionsPrices.getLinkingOptions().contains(selectedOptionsList)){
                    this.selectedOptionsList = null;
                }*/

                if(selectedOptionsList != null && selectedOptionsList.size() != 0){
                    checkBoxes.get(optionsPrices.getOptionsTitle().get(0)).get(selectedOptionsList.get(0)).setChecked(true);
                }
                else {
                    int _default = Integer.parseInt(optionsPrices.getDefault_option());
                    checkBoxes.get(optionsPrices.getOptionsTitle().get(0)).get(optionsPrices.getOptions().get(0)
                            .get(_default!=-1? _default:0)).setChecked(true);
                }
            }

        }
        else {
            setupPrice(optionsPrices.getMain_price());
        }
    }

    private void setAllFalse(int finalI, String mTitle, CompoundButton _checkBox){
        for(String op: optionsPrices.getOptions().get(finalI)){
            checkBoxes.get(mTitle).get(op).setChecked(checkBoxes.get(mTitle).get(op) == _checkBox);
        }

        for(int i = finalI+1; i< optionsPrices.getOptionsTitle().size(); i++){
            String title = optionsPrices.getOptionsTitle().get(i);
            selectedOptions.put(title, "");
            for(String op: optionsPrices.getOptions().get(i)){
                checkBoxes.get(title).get(op).setTextColor(mContext.getColor(R.color.black));
                checkBoxes.get(title).get(op).setChecked(false);
            }
        }
    }

    private void checkOptions(int finalI, String item){
        Log.d(TAG, "checkOptions: ---------- ");

        // check is user select previous options
        if(finalI != 0){
            String previousTitle = optionsPrices.getOptionsTitle().get(finalI-1);
            if(selectedOptions.get(previousTitle).length() > 0){

            }else {
                String firstOption = selectedOptions.get(optionsPrices.getOptionsTitle().get(0)); // always first option is selected
                for(ArrayList<String> linkingList: optionsPrices.getLinkingOptions()) {
                    if(linkingList.get(0).equals(firstOption) && linkingList.get(finalI).equals(item)){
                        for (int i=finalI-1; i>0; i--){
                            if(checkBoxes.get(previousTitle).get(linkingList.get(i)).isEnabled()){
                                hold .add(checkBoxes.get(optionsPrices.getOptionsTitle().get(finalI)).get(item)) ;
                                checkBoxes.get(previousTitle).get(linkingList.get(i)).setChecked(true);
                                return;
                            }
                        }
                    }
                }
            }
        }

        //
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        s(hashMap, finalI, finalI==0? null : selectedOptions.get(optionsPrices.getOptionsTitle().get(finalI-1)), item);

        for(Map.Entry<String, ArrayList<String>> hash: hashMap.entrySet()){
            String title = hash.getKey();
            ArrayList<String> arrayList = hash.getValue();

            for(Map.Entry<String, CheckBox> checking: checkBoxes.get(title).entrySet()){
                CheckBox cb = checking.getValue();
                if(arrayList.contains(checking.getKey())){
                    cb.setEnabled(true);
                    cb.setTextColor(mContext.getColor(R.color.black));

                }else {
                    cb.setEnabled(false);
                    cb.setTextColor(mContext.getColor(R.color.gray0));

                }
            }
        }

        //
        if(hold.size() > 0){ // if we select from bottom
            for(int i=0; i<hold.size(); i++){
                hold.get(i).setChecked(true);
            }
            hold.clear();

        }else { // if we select from top
            for(String ti : optionsPrices.getOptionsTitle()){ // by order of title
                if(hashMap.containsKey(ti) && hashMap.get(ti).size() > 0){
                    CheckBox checkBox = checkBoxes.get(ti).get(hashMap.get(ti).get(0));
                    if(checkBox != null){ // some options will be nll when come from  adapterBasket
                        checkBox.setChecked(true);
                    }
                    return;
                }
            }
        }

        // we come here only if the last list of options select

        // get price
        String mPrice = "0";
        if(optionsPrices.getOptionsTitle().size()==1){
            int index = optionsPrices.getOptions().get(0).indexOf(item);
            if(index != -1){
                selectedOptionsList = new ArrayList<>(Collections.singleton(item));
                mPrice = optionsPrices.getPrices().get(index);
            }

        }else {
            ArrayList<String> arrayList = new ArrayList<>();
            for(String ti : optionsPrices.getOptionsTitle()){ // by order of title
                arrayList.add(selectedOptions.get(ti));
            }
            int index = optionsPrices.getLinkingOptions().indexOf(arrayList);
            if(index != -1){
                selectedOptionsList = new ArrayList<>(arrayList);
                mPrice = optionsPrices.getPrices().get(index);
            }
        }

        if(optionsPrices.isIs_price_unify() && firstTime){
            setupPrice(optionsPrices.getMain_price());
        }else if(!optionsPrices.isIs_price_unify()){
            setupPrice(mPrice);
        }
    }

    private void s(HashMap<String, ArrayList<String>> hashMap, int finalI,  String previousOption, String currentOption){
        if(optionsPrices.getOptions().size()-1 > finalI){
            for(ArrayList<String> linkingList: optionsPrices.getLinkingOptions()) {
                if (linkingList.get(finalI).equals(currentOption) && (finalI==0 || linkingList.get(finalI-1).equals(previousOption))){
                    String nextTitle = optionsPrices.getOptionsTitle().get(finalI+1);
                    String nextOptions = linkingList.get(finalI+1);
                    if(!hashMap.containsKey(nextTitle)){
                        hashMap.put(nextTitle, new ArrayList<String>());
                    }
                    hashMap.get(nextTitle).add(nextOptions);
                    s(hashMap, finalI+1, currentOption, nextOptions);
                }
            }
        }
    }

    public ArrayList<String> getSelectedOptions() {
        return selectedOptionsList;
    }

    public String getOptionsId(){
        StringBuilder options_id = new StringBuilder();
        if(optionsPrices.getOptionsTitle()==null){
            options_id = new StringBuilder("-1");
        }else {
            for(int i=0; i<selectedOptionsList.size(); i++){
                String op = selectedOptionsList.get(i);
                int index = optionsPrices.getOptions().get(i).indexOf(op);
                options_id.append(index);
            }
        }
        return options_id.toString();
    }

    public void setupPrice(final String price){
        if(!firstTime){
            priceLayout.startAnimation(animBlink);
        }
        firstTime = false;

        mPrice = price;
        if(price.length()>0 && !price.equals("0")){
            price_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizePrice);
            currency.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizePrice-4);
            if(textColor != 0){
                price_item.setTextColor(textColor);
                currency.setTextColor(textColor);
            }

            price_item.setText(new BigDecimal(price+"").stripTrailingZeros().toPlainString());
            final Currency itemCurrency = Currency.getInstance(currencyPrice);
            currency.setText(itemCurrency != null? itemCurrency.getDisplayName() : currencyPrice);
            currency.setVisibility(View.VISIBLE);

            /*if(itemCurrency != null){
                if(globalVariable.getCurrency() != null && !itemCurrency.equals(globalVariable.getCurrency())){
                    UtilitiesMethods.rotateAnimation(line_currency);
                    currencyExchangeLayout.setVisibility(View.VISIBLE);

                    Currencylayer.Converter(new ConvertCurrency(itemCurrency, globalVariable.getCurrency()
                            , price, price_item, currency
                            , currencyExchangeLayout, null, null));

                    priceLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(line_currency.getAnimation() == null){
                                UtilitiesMethods.rotateAnimation(line_currency);
                                Currencylayer.Converter(new ConvertCurrency(itemCurrency, globalVariable.getCurrency()
                                        , price, price_item, currency
                                        , currencyExchangeLayout, null, null));
                            }
                        }
                    });
                }else {
                    currencyExchangeLayout.setVisibility(View.GONE);
                }
            }*/
        }else {
            price_item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            price_item.setTextColor(mContext.getColor(R.color.red));
            price_item.setText(mContext.getString(R.string.happened_wrong_price));
            currency.setVisibility(View.GONE);
        }
    }

    public void setCurrencyPrice(String currencyPrice) {
        this.currencyPrice = currencyPrice;
    }

    public String getmPrice() {
        return mPrice;
    }
}
