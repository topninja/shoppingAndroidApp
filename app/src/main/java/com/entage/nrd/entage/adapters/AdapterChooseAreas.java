package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.utilities_1.ShippingCompanies;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterChooseAreas extends RecyclerView.Adapter{
    private static final String TAG = "AdapterChooseAreas";


    private Context mContext;
    private RecyclerView recyclerView;

    private ArrayList<AreaShippingAvailable> dataSearchAreas = null;
    private HashMap<String, HashMap<String, AreaShippingAvailable>> dataSelected;

    private ArrayList<String> shippingCompanies;
    private View.OnClickListener onClickListener;

    private String entagePageName;

    public AdapterChooseAreas(Context context, RecyclerView recyclerView, ArrayList<AreaShippingAvailable> dataSearchAreas
                             , View.OnClickListener onClickListener
            , HashMap<String, HashMap<String, AreaShippingAvailable>> dataSelected, String entagePageName) {

        this.mContext = context;
        this.dataSearchAreas = dataSearchAreas;
        this.recyclerView = recyclerView;
        this.onClickListener = onClickListener;
        this.dataSelected = dataSelected;
        this.entagePageName = entagePageName;

        ShippingCompanies.init(mContext);
        shippingCompanies = ShippingCompanies.getCompaniesList();
    }

    public class TextViewViewHolder extends RecyclerView.ViewHolder{
        TextView nameArea, calculatorPricing;
        CheckBox checkBoxArea, checkBoxIsFreeShipping, shippingPriceLater, pbs, pwr;
        RelativeLayout selectArea, selectFreeShipping;
        LinearLayout layout, layoutShipping_price;
        AppCompatSpinner shippingCompaniesSpinner;
        EditText shippingPrice;
        private TextViewViewHolder(View itemView) {
            super(itemView);
            nameArea = itemView.findViewById(R.id.name_area);
            checkBoxArea = itemView.findViewById(R.id.checkbox_area);
            checkBoxIsFreeShipping = itemView.findViewById(R.id.checkbox_free_shipping);
            calculatorPricing = itemView.findViewById(R.id.calculator_pricing);

            layout = itemView.findViewById(R.id.layout);
            selectArea = itemView.findViewById(R.id.select_area);
            selectFreeShipping = itemView.findViewById(R.id.select_free_shipping);

            shippingCompaniesSpinner = itemView.findViewById(R.id.shipping_companies);
            shippingPrice = itemView.findViewById(R.id.shipping_price);
            layoutShipping_price = itemView.findViewById(R.id.layoutShipping_price);

            shippingPriceLater = itemView.findViewById(R.id.shipping_price_later);
            pbs = itemView.findViewById(R.id.payment_before_sending);
            pwr = itemView.findViewById(R.id.payment_upon_receipt);

            shippingPrice.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    int position = getAdapterPosition();
                    if(position != -1){
                        String country_code = dataSearchAreas.get(position).getCountry_code();
                        String id_area = dataSearchAreas.get(position).getId_area();

                        String price = "0.0";
                        String text = shippingPrice.getText().toString();
                        if(text.length()>0 && !text.equals(".")){
                            price = text;
                        }

                        dataSelected.get(country_code).get(id_area).setShipping_price(
                                PaymentsUtil.print(PaymentsUtil.microsToString(price)));
                    }
                }
                @Override
                public void afterTextChanged(Editable editable) { }
            });

            calculatorPricing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != -1){
                        openInWeb(position);
                    }
                }
            });

            selectFreeShipping.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int _position = getAdapterPosition();
                    if(_position != -1){
                        String country_code = dataSearchAreas.get(getAdapterPosition()).getCountry_code();
                        String id_area = dataSearchAreas.get(getAdapterPosition()).getId_area();
                        boolean bo = !dataSelected.get(country_code).get(id_area).isFree_shipping();
                        dataSelected.get(country_code).get(id_area).setFree_shipping(bo);

                        checkBoxIsFreeShipping.setChecked(bo);
                        if(bo){
                            layoutShipping_price.setVisibility(View.GONE);
                        }else {
                            layoutShipping_price.setVisibility(shippingCompaniesSpinner.getSelectedItemPosition() == 0 ? View.GONE:View.VISIBLE);
                        }
                    }
                }
            });

            shippingPriceLater.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int _position = getAdapterPosition();
                    if(_position != -1) {
                        String country_code = dataSearchAreas.get(getAdapterPosition()).getCountry_code();
                        String id_area = dataSearchAreas.get(getAdapterPosition()).getId_area();
                        dataSelected.get(country_code).get(id_area).setShipping_price_later(shippingPriceLater.isChecked());

                        shippingPrice.setVisibility(shippingPriceLater.isChecked() ? View.GONE:View.VISIBLE);

                    }else {
                        shippingPriceLater.setChecked(!shippingPriceLater.isChecked());
                    }
                }
            });

            shippingCompaniesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int _position = getAdapterPosition();
                    if(_position != -1){
                        String country_code = dataSearchAreas.get(getAdapterPosition()).getCountry_code();
                        String id_area = dataSearchAreas.get(getAdapterPosition()).getId_area();
                        dataSelected.get(country_code).get(id_area)
                                .setShipping_company(ShippingCompanies.getCompany(shippingCompanies.get(position)));
                        calculatorPricing.setText(mContext.getString(R.string.shipping_price_for_company)+" "+shippingCompaniesSpinner.getSelectedItem());

                        if(shippingCompanies.get(position).equals(mContext.getString(R.string.shipping_company_later))){
                            calculatorPricing.setVisibility(View.GONE);
                            layoutShipping_price.setVisibility(View.GONE);
                        } else {
                            boolean bo = dataSelected.get(country_code).get(id_area).isShipping_price_later();
                            shippingPrice.setVisibility(bo ? View.GONE:View.VISIBLE);
                            calculatorPricing.setVisibility(bo ? View.GONE:View.VISIBLE);

                            layoutShipping_price.setVisibility(dataSelected.get(country_code).get(id_area).isFree_shipping()?View.GONE:View.VISIBLE);
                            if(shippingCompanies.get(position).equals(mContext.getString(R.string.other_shipping_company)) ||
                                    shippingCompanies.get(position).equals(mContext.getString(R.string.personal_shipping))) {
                                calculatorPricing.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });

            pbs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int _position = getAdapterPosition();
                    if(_position != -1){
                        String country_code = dataSearchAreas.get(getAdapterPosition()).getCountry_code();
                        String id_area = dataSearchAreas.get(getAdapterPosition()).getId_area();
                        dataSelected.get(country_code).get(id_area).setPaymentBs(isChecked);
                    }else {
                        pbs.setChecked(!pbs.isChecked());
                    }
                }
            });

            pwr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int _position = getAdapterPosition();
                    if(_position != -1){
                        String country_code = dataSearchAreas.get(getAdapterPosition()).getCountry_code();
                        String id_area = dataSearchAreas.get(getAdapterPosition()).getId_area();
                        dataSelected.get(country_code).get(id_area).setPaymentWr(isChecked);
                    }else {
                        pwr.setChecked(!pwr.isChecked());
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_listview_choose_areas, parent, false);
        RecyclerView.ViewHolder viewHolder = new AdapterChooseAreas.TextViewViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final TextViewViewHolder _holder = (TextViewViewHolder) holder;

        _holder.nameArea.setText(dataSearchAreas.get(position).getName_area());

        final String country_code = dataSearchAreas.get(position).getCountry_code();
        final String id_area = dataSearchAreas.get(position).getId_area();

        boolean isSelected = dataSelected.containsKey(country_code) && dataSelected.get(country_code).containsKey(id_area);
        _holder.checkBoxArea.setChecked(isSelected);

        _holder.selectArea.setBackgroundColor(!isSelected? mContext.getColor(R.color.white) : mContext.getColor(R.color.entage_blue_3));

        _holder.layout.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        _holder.checkBoxIsFreeShipping.setChecked(isSelected && dataSelected.get(country_code).get(id_area).isFree_shipping());

        if(isSelected){
            String price = dataSelected.get(country_code).get(id_area).getShipping_price();
            _holder.shippingPrice.setText(price == null ? "" : price);

            if(_holder.shippingCompaniesSpinner.getAdapter() == null){
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, shippingCompanies);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                _holder.shippingCompaniesSpinner.setAdapter(adapter);
            }
            String sc = dataSelected.get(country_code).get(id_area).getShipping_company();
            _holder.shippingCompaniesSpinner.setSelection(sc != null? shippingCompanies
                    .indexOf(ShippingCompanies.getCompanyShowText(sc)) : 0);

            _holder.shippingPriceLater.setChecked(dataSelected.get(country_code).get(id_area).isShipping_price_later());

            _holder.pbs.setChecked(dataSelected.get(country_code).get(id_area).isPaymentBs());

            _holder.pwr.setChecked(dataSelected.get(country_code).get(id_area).isPaymentWr());
        }


        _holder.selectArea.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        if(dataSearchAreas==null) {
            dataSearchAreas = new ArrayList<>();
        }
        return dataSearchAreas.size();
    }

    private void openInWeb(int position){
        String country_code = dataSearchAreas.get(position).getCountry_code();
        String id_area = dataSearchAreas.get(position).getId_area();
        String company = dataSelected.get(country_code).get(id_area).getShipping_company();
        if(ShippingCompanies.getCompanyLink(company).contains("http")){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ShippingCompanies.getCompanyLink(company)));
            mContext.startActivity(browserIntent);
        }
    }

}
