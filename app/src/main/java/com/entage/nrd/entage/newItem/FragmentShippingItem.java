package com.entage.nrd.entage.newItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentShippingItem extends Fragment {
    private static final String TAG = "FragmentShippingItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;

    private CheckBox locationAvailable, shippingAvailable;
    private RelativeLayout selectPlaces, infoLocationItem ;
    private EditText city;
    private Spinner countriesSpinner;
    private ArrayAdapter<String> adapterCountries;
    private InputMethodManager imm ;

    private MessageDialog messageDialog = new MessageDialog();

    private String entagePageId, itemId;
    private boolean isDataFetched = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_shipping_info_item, container, false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();
            init();

        }else {
            mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[7]);
            saveData();
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

        getDataFromDbArchives();
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[7]);

        countriesSpinner =  view.findViewById(R.id.countries_spinner) ;
       // paymentMethods =  view.findViewById(R.id.payment_methods) ;
        selectPlaces =  view.findViewById(R.id.selected_places);
        infoLocationItem =  view.findViewById(R.id.info_location_item);
        locationAvailable =  view.findViewById(R.id.no_shipping_available);
        shippingAvailable =  view.findViewById(R.id.yes_shipping_available);
        city =  view.findViewById(R.id.city);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
    }

    private void onClickListener(){
        selectPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener(new FragmentSelectShippingAreas());
            }
        });

        infoLocationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener(new FragmentAddLocations());
            }
        });

        shippingAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectPlaces.setVisibility(isChecked? View.VISIBLE : View.GONE);
            }
        });

        locationAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                infoLocationItem.setVisibility(isChecked? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupAdapters(){
        // Adapter Countries
        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        String countryUser = null;
        if(globalVariable.getLocationInformation() != null){
            countryUser = globalVariable.getLocationInformation().getCountry_code();
        }
        ArrayList<String> countriesNames = CountriesData.getCountriesNames(countryUser);

        adapterCountries = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, countriesNames);
        adapterCountries.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countriesSpinner.setAdapter(adapterCountries);
        //
    }

    private void setDataInWidgets(ShippingInformation shippingInformation) {

        city.setText(shippingInformation.getCity_store_item());

        if(shippingInformation.getCountry_store_item() != null){
            countriesSpinner.setSelection(adapterCountries.getPosition(shippingInformation.getCountry_store_item() ));
        }

        shippingAvailable.setChecked(shippingInformation.isShipping_available());
        locationAvailable.setChecked(shippingInformation.isLocationAvailable());

        mOnActivityDataItemListener.setAreaShippingAvailable(shippingInformation.getArea_shipping_available());
        mOnActivityDataItemListener.setReceivingLocation(shippingInformation.getReceiving_location());
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


    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_shipping_information));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_shipping_information));

        myRefLastEdit = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_last_edit_was_in));


        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void getDataFromDbArchives(){
        showProgress(false, false);
        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    setDataInWidgets(dataSnapshot.getValue(ShippingInformation.class));
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
                    setDataInWidgets(dataSnapshot.getValue(ShippingInformation.class));
                }
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
        if(databaseError.getMessage().equals("Permission denied")){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied)+ "  " +
                    databaseError.getMessage());
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_msg)+ "  " +
                    databaseError.getMessage());
        }
    }

    private boolean checking(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(Map.Entry<String, HashMap<String, AreaShippingAvailable>> hashMapEntry :
                mOnActivityDataItemListener.getAreaShippingAvailable().entrySet()){

            for(Map.Entry<String, AreaShippingAvailable> areas : hashMapEntry.getValue().entrySet()){
                if(areas.getValue().getShipping_price() == null  || areas.getValue().getShipping_price().length() == 0 ||
                        areas.getValue().getShipping_price().equals("0")){
                    areas.getValue().setShipping_price_later(true);
                }
                if(areas.getValue().getShipping_company().equals("announced_later") ){
                    areas.getValue().setShipping_price_later(true);
                }

                if(!areas.getValue().isPaymentBs() && !areas.getValue().isPaymentWr()){
                    arrayList.add(areas.getValue().getName_area());
                }
            }
        }

        if(arrayList.size()>0){
            ((TextView)view.findViewById(R.id.error)).setText(mContext.getString(R.string.error_choose_payment_method_for_area)+
                    arrayList.toString());
            (view.findViewById(R.id.error)).setVisibility(View.VISIBLE);
            return false;

        }else {
            return true;
        }
    }

    private void saveData() {
        if(isDataFetched){
            (view.findViewById(R.id.error1)).setVisibility(View.GONE);
            (view.findViewById(R.id.error)).setVisibility(View.GONE);
            keyboard(false);

            if (city.length()<3){
                (view.findViewById(R.id.error1)).setVisibility(View.VISIBLE);
                return;
            }

            if(!locationAvailable.isChecked() && !shippingAvailable.isChecked()){
                ((TextView)view.findViewById(R.id.error)).setText(mContext.getString(R.string.error_choose_one_shipping_or_location));
                (view.findViewById(R.id.error)).setVisibility(View.VISIBLE);
                return;
            }

            if(locationAvailable.isChecked()){
                if(mOnActivityDataItemListener.getReceivingLocation() != null
                        && mOnActivityDataItemListener.getReceivingLocation().size() == 0){
                    ((TextView)view.findViewById(R.id.error)).setText(mContext.getString(R.string.error_add_address));
                    (view.findViewById(R.id.error)).setVisibility(View.VISIBLE);
                    return;
                }
            }

            if(shippingAvailable.isChecked()){
                if(mOnActivityDataItemListener.getAreaShippingAvailable() != null
                        && mOnActivityDataItemListener.getAreaShippingAvailable().size() == 0){
                    ((TextView)view.findViewById(R.id.error)).setText(mContext.getString(R.string.error_choose_area));
                    (view.findViewById(R.id.error)).setVisibility(View.VISIBLE);
                    return;
                }else {
                    // checking user set payment methods for each area has shipping available
                    if(!checking()){
                        return;
                    }
                }
            }

            showProgress(false, true);
            myRefArchives
                    .setValue(new ShippingInformation(countriesSpinner.getSelectedItem().toString(),
                            city.getText().toString(), shippingAvailable.isChecked(), locationAvailable.isChecked(),
                            mOnActivityDataItemListener.getAreaShippingAvailable(), mOnActivityDataItemListener.getReceivingLocation()))
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
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");
        }
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

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_payment_item);

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
            }
        });

        view.findViewById(R.id.progress_next_step).setVisibility(View.GONE);
        nextStep.setVisibility(View.GONE);
    }

    private void showProgress(boolean boo, boolean all){
        if(all){
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            //nextStep.setEnabled(boo);
            //nextStep.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            tips.setEnabled(boo);

        }else {
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        }

        //
    }

    private void setupAdviceLayout(){
        tips = view.findViewById(R.id.tips);
        final String title = mContext.getResources().getStringArray(R.array.advices)[7];
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
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_shipping_item)[0]);
    }
}
