package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.createEntagePage.FragmentShowInfo;
import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.SharingLocation;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;

import org.geonames.FeatureClass;
import org.geonames.InvalidParameterException;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class FragmentAddLocations extends Fragment {
    private static final String TAG = "FragmentAddLocations";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view;
    private InputMethodManager imm;
    private final int LOCATION_REQUEST_CODE = 350;

    private LinearLayout layout_add_new_location, layout_added_location;
    private AutoCompleteTextView  setResultInThis;
    private TextView textViewSearch;
    private CheckBox show_added_location;
    private Spinner typeSearchSpinner;

    private ArrayList<String> countriesNames, citiesLastUsed, addressLastUsed, phoneNumberLastUsed;
    private ArrayAdapter<String> adapterCities,  adapterAddress,  adapterPhoneNumber;
    private HashMap<String, AreaShippingAvailable> countriesAreas, citiesAreas ;

    private MessageDialog messageDialog = new MessageDialog();
    private String entagePageId, itemId, lang;
    private GlobalVariable globalVariable;

    private AutoCompleteTextView addressSelected;
    private AutoCompleteTextView locationByGoogleMapSelected;
    private String lanLat;
    private Address addressGPS;

    //private ReceivingLocation receivingLocation;
    private ArrayList<ReceivingLocation> myReceivingLocation;

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_locations_item, container , false);
            mContext = getActivity();

            getIncomingBundle();
            inti();
        }else {
            if(mOnActivityDataItemListener != null){
                mOnActivityDataItemListener.setTitle(mContext.getString(R.string.add_location_shipping_available));
                mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);
            }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(data != null && data.hasExtra("lat_lng")){
                    lanLat = data.getStringExtra("lat_lng");
                    addressGPS =  data.getParcelableExtra("address");

                    if(addressGPS != null){
                        locationByGoogleMapSelected.setText(addressGPS.getAddressLine(0));
                        addressSelected.setText(addressGPS.getAddressLine(0));
                    }
                }
            }
        }

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

    private void inti() {
        initWidgets();
        onClickListener();

        setupLists();

        myReceivingLocation = mOnActivityDataItemListener.getReceivingLocation();

        show_added_location.setChecked(true);
        if(myReceivingLocation.size() > 0){
            setAddedLocations();
        }
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.add_location_shipping_available));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        layout_add_new_location =  view.findViewById(R.id.layout_add_location);

        typeSearchSpinner =  view.findViewById(R.id.type_search);
        show_added_location =  view.findViewById(R.id.show_added_location);
        layout_added_location =  view.findViewById(R.id.layout_added_location);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        if(globalVariable.getLanguage() != null){
            lang = globalVariable.getLanguage();
        }else {
            lang = Locale.getDefault().getLanguage();
        }

        Transition transition = new Fade();
        transition.setDuration(300);
        transition.addTarget(layout_added_location);
        TransitionManager.beginDelayedTransition(layout_added_location, transition);

        Transition transition1 = new Fade();
        transition1.setDuration(300);
        transition1.addTarget(layout_add_new_location);
        TransitionManager.beginDelayedTransition(layout_add_new_location, transition1);
    }

    private void onClickListener() {
        show_added_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAddedLocations();
            }
        });

        typeSearchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSpinner(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void selectSpinner(int position){
        if(position != 0){
            view.findViewById(R.id.no_locations).setVisibility(View.GONE);
            show_added_location.setChecked(false);
            show_added_location.setClickable(true);
            lanLat = null;
            addressGPS = null;

            hideKeyboard();

            ReceivingLocation receivingLocation = new ReceivingLocation();

            receivingLocation.setTitle(mContext.getString(R.string.the_address)+ " "+ (myReceivingLocation.size()+1));
            receivingLocation.setBy_google_map(position == 1);

            layout_add_new_location.removeAllViews();

            layout_add_new_location.addView(addLayout(-1, receivingLocation));

            layout_add_new_location.setVisibility(View.VISIBLE);
            layout_added_location.setVisibility(View.GONE);
        }
    }

    private void setupLists() {
        // Adapter Countries
        countriesNames = CountriesData.getCountriesNames(null);
        countriesAreas = new HashMap<>();
        for (String cn: countriesNames) {
            countriesAreas.put(cn, new AreaShippingAvailable("", "", cn,
                    CountriesData.getCountryId(cn), CountriesData.getCountryCode(cn) ,false,false,
                    null, "0", false,  "0", false, false));
        }

        citiesAreas = new HashMap<>();
        citiesLastUsed = new ArrayList<>();
        addressLastUsed = new ArrayList<>();
        phoneNumberLastUsed = new ArrayList<>();
    }

    private void setAddedLocations() {
        typeSearchSpinner.setSelection(0);
        show_added_location.setChecked(true);
        show_added_location.setClickable(false);

        if(myReceivingLocation.size() == 0){
            view.findViewById(R.id.no_locations).setVisibility(View.VISIBLE);
        }else {
            view.findViewById(R.id.no_locations).setVisibility(View.GONE);
        }

        LinearLayout linearLayout1 =  view.findViewById(R.id.linearLayout1);
        linearLayout1.removeAllViews();
        for(int i = 0; i<myReceivingLocation.size(); i++){

            ReceivingLocation rl = new ReceivingLocation();
            rl.setTitle(myReceivingLocation.get(i).getTitle());
            rl.setLocation(myReceivingLocation.get(i).getLocation());
            rl.setCountry(myReceivingLocation.get(i).getCountry());
            rl.setCity(myReceivingLocation.get(i).getCity());
            rl.setAddress(myReceivingLocation.get(i).getAddress());
            rl.setPhone_number_1(myReceivingLocation.get(i).getPhone_number_1());
            rl.setPhone_number_2(myReceivingLocation.get(i).getPhone_number_2());
            rl.setPayment_bs(myReceivingLocation.get(i).isPayment_bs());
            rl.setPayment_wr(myReceivingLocation.get(i).isPayment_wr());

            linearLayout1.addView(addLayout(i, rl));

        }

        layout_add_new_location.setVisibility(View.GONE);
        layout_added_location.setVisibility(View.VISIBLE);
    }

    private View addLayout(final int position, final ReceivingLocation rl){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_add_location, null, false);

        final EditText title =  _view.findViewById(R.id.title);
        ImageView icon_google_map =  _view.findViewById(R.id.icon_google_map);
        final AutoCompleteTextView location_google_map =  _view.findViewById(R.id.location_google_map);
        AutoCompleteTextView input_country =  _view.findViewById(R.id.input_country);
        RelativeLayout layout_searching_city =  _view.findViewById(R.id.layout_searching_city);
        final AutoCompleteTextView input_city =  _view.findViewById(R.id.input_city);
        final TextView search =  _view.findViewById(R.id.search);
        final AutoCompleteTextView address =  _view.findViewById(R.id.address);
        final AutoCompleteTextView phone_number_1 =  _view.findViewById(R.id.phone_number_1);
        final AutoCompleteTextView phone_number_2 =  _view.findViewById(R.id.phone_number_2);

        final CheckBox payment_before_sending =  _view.findViewById(R.id.payment_before_sending);
        final CheckBox payment_when_receiving =  _view.findViewById(R.id.payment_upon_receipt);
        TextView save_location =  _view.findViewById(R.id.save_location);

        icon_google_map.setVisibility(View.VISIBLE);
        input_country.setVisibility(View.VISIBLE);
        layout_searching_city.setVisibility(View.VISIBLE);
        address.setHint(mContext.getString(R.string.the_address));

        title.setText(rl.getTitle());
        location_google_map.setText(rl.getLocation());
        address.setText(rl.getAddress());
        phone_number_1.setText(rl.getPhone_number_1());
        phone_number_2.setText(rl.getPhone_number_2());
        payment_before_sending.setChecked(rl.isPayment_bs());
        payment_when_receiving.setChecked(rl.isPayment_wr());

        icon_google_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UtilitiesMethods.checkPermissionsLocations(mContext)){
                    if(isLocationEnabled(mContext)){
                        lanLat = null;
                        addressGPS = null;
                        locationByGoogleMapSelected = location_google_map;
                        addressSelected = address;

                        Intent intent = new Intent(mContext, SharingLocation.class);
                        startActivityForResult(intent, LOCATION_REQUEST_CODE);
                    }else {
                        openGPS();
                    }
                }
            }
        });

        if(rl.getCountry() != null){
            input_country.setText(rl.getCountry().getName_area());
            input_city.setText(rl.getCity().getName_area());
        }

        setupAdapters(rl, input_country, input_city, address, phone_number_1, phone_number_2, payment_before_sending,
                payment_when_receiving);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rl.getCountry() != null){
                    if(input_city.getText().length() > 0){
                        textViewSearch = search;
                        setResultInThis = input_city;
                        String text = StringManipulation.removeLastSpace(input_city.getText().toString());
                        startSearch(rl.getCountry().getCountry_code(), text);
                    }

                }else {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_select_country_firs));
                }
            }
        });
        input_city.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(rl.getCountry() != null){
                        if(input_city.getText().length() > 0){
                            textViewSearch = search;
                            setResultInThis = input_city;
                            String text = StringManipulation.removeLastSpace(input_city.getText().toString());
                            startSearch(rl.getCountry().getCountry_code(), text);
                        }

                    }else {
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_select_country_firs));
                    }
                    return true;
                }
                return false;
            }
        });

        save_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl.setTitle(title.getText().toString());
                rl.setLocation(location_google_map.getText().toString());
                rl.setAddress(address.getText().toString());
                rl.setPhone_number_1(phone_number_1.getText().toString());
                rl.setPhone_number_2(phone_number_2.getText().toString());

                if(lanLat != null){
                    rl.setLat_lng(lanLat);
                }

                saveLocation(position, rl, phone_number_1);
            }
        });

        if(position != -1) {
            save_location.setBackground(mContext.getDrawable(R.drawable.border_curve_gray_r3));
            save_location.setTextColor(mContext.getColor(R.color.gray1));
            save_location.setText(mContext.getString(R.string.save_edit));
            final ImageView arrow = _view.findViewById(R.id.arrow);
            arrow.setVisibility(View.VISIBLE);
            final LinearLayout container = _view.findViewById(R.id.container);
            container.setVisibility(View.GONE);
            ImageView delete_location = _view.findViewById(R.id.delete_location);
            delete_location.setVisibility(View.VISIBLE);

            arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(container.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(container);
                    }else {
                        UtilitiesMethods.expand(container);
                    }
                    arrow.animate().rotation(arrow.getRotation()+180).setDuration(500).start();
                }
            });

            delete_location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    container.setVisibility(View.GONE);
                    myReceivingLocation.remove(position);
                    setAddedLocations();
                }
            });
            UtilitiesMethods.collapse(container);
        }

        return _view;
    }

    private void setupAdapters(final ReceivingLocation receivingLocation, final AutoCompleteTextView  input_country,
                               final AutoCompleteTextView input_city, final AutoCompleteTextView address,
                               final AutoCompleteTextView phone_number_1, final AutoCompleteTextView phone_number_2,
                               CheckBox payment_before_sending, CheckBox payment_when_receiving) {
        // Adapter Countries
        input_country.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, countriesNames));
        input_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                hideKeyboard();
                Object item = parent.getItemAtPosition(position);
                receivingLocation.setCountry(countriesAreas.get(item.toString()));
            }
        });
        input_country.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                input_country.showDropDown();
                return false;
            }
        });
        // END

        // Adapter Cities
        input_city.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesLastUsed));
        input_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                hideKeyboard();
                Object item = parent.getItemAtPosition(position);
                receivingLocation.setCity(citiesAreas.get(item));
            }
        });
        input_city.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                input_city.showDropDown();
                return false;
            }
        });
        // END

        // Adapter Address
        address.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, addressLastUsed));
        address.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                address.showDropDown();
                return false;
            }
        });// END

        // Adapter PhoneNumber
        phone_number_1.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, phoneNumberLastUsed));
        phone_number_1.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                phone_number_1.showDropDown();
                return false;
            }
        });
        phone_number_2.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, phoneNumberLastUsed));
        phone_number_2.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                phone_number_2.showDropDown();
                return false;
            }
        });
        // END

        payment_before_sending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                receivingLocation.setPayment_bs(isChecked);
            }
        });
        payment_when_receiving.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                receivingLocation.setPayment_wr(isChecked);
                if(isChecked){
                    View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_waring_payment, null, false);
                    messageDialog.errorMessage(mContext, _view);
                    _view.findViewById(R.id.what_is_cutting_money).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("info", mContext.getString(R.string.see_details_fees_sale));
                            mOnActivityDataItemListener.onActivityListener(new FragmentShowInfo(), bundle);
                        }
                    });
                }
            }
        });
    }

    private void saveLocation(int position, ReceivingLocation receivingLocation, AutoCompleteTextView phone_number_1){

        if(receivingLocation.getTitle() == null || receivingLocation.getTitle().length() == 0){
            messageDialog.errorMessage(mContext, mContext.getString(R.string.error_enter_title));
            return;
        }

        if(receivingLocation.isBy_google_map()){
            if(receivingLocation.getLocation() == null || receivingLocation.getLocation().length() < 4){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_enter_right_location));
                return;
            }

        }
        else {
            if(receivingLocation.getCountry() == null){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_enter_country));
                return;
            }

            if(receivingLocation.getCity() == null){
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_select_city));
                return;
            }
        }

        if(phone_number_1.getText() == null || phone_number_1.getText().length() < 5){
            messageDialog.errorMessage(mContext, mContext.getString(R.string.error_phone_wrong));
            return;
        }

        if(!receivingLocation.isPayment_bs()&& !receivingLocation.isPayment_wr()){
            messageDialog.errorMessage(mContext, mContext.getString(R.string.error_choose_payment_method));
            return;
        }

        if(position == -1){
            myReceivingLocation.add(receivingLocation);
            setAddedLocations();

        }else {
            myReceivingLocation.remove(position);
            myReceivingLocation.add(position, receivingLocation);
            Toast.makeText(mContext, mContext.getString(R.string.save_successfully), Toast.LENGTH_LONG).show();
        }
    }

    private void hideKeyboard(){
        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    private void openGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setMessage(mContext.getString(R.string.open_gps_to_send));
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.open_gps), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        builder.create().show();
    }

    private void startSearch(String countryCode, String text){
        hideKeyboard();
        GeonamesSearch geonamesSearc = new GeonamesSearch();
        geonamesSearc.startSearch(mContext.getString(R.string.search_for_cities), text,
                countryCode,
                "P", "");
    }

    // in back ground
    private void setResultList(ToponymSearchResult searchResult){
        if (!((Activity)mContext).isDestroyed()){
            citiesAreas.clear();
            citiesLastUsed.clear();
            if (searchResult != null){
                for (Toponym t : searchResult.getToponyms())
                {
                    String idArea = String.valueOf(t.getGeoNameId());
                    String countryCode = t.getCountryCode();

                    citiesLastUsed.add(t.getName());
                    citiesAreas.put(t.getName(), new AreaShippingAvailable("","",t.getName(), idArea
                            , countryCode, true,false, null, "0",false, "0", false, false));
                }
            }

            if(setResultInThis != null){
                setResultInThis.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesLastUsed));
                setResultInThis.showDropDown();
            }
        }
    }
    //

    public class GeonamesSearch extends AsyncTask<Integer, String, ToponymSearchResult> {
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

        private ToponymSearchCriteria searchCriteria ;

        public GeonamesSearch() {
        }

        private void startSearch(String typeSearch, String nameStartsWith, String _area, String _featureClass, String _featureCode){
            // for more info http://www.geonames.org/export/codes.html

            // q: "cont"
            // FeatureClass: null
            // - FeatureCode: CONT: continents

            // country: _area
            // FeatureClass: A: country, state, region,...
            // - FeatureCode: ADM1: a primary administrative division of a country, such as a state in the United States

            // FeatureClass: P city, village,...
            // - FeatureCode:


            // FeatureClass
            FeatureClass featureClass = null;
            if (_featureClass.equals("A")){
                featureClass = FeatureClass.A;

            } else if (_featureClass.equals("P")){
                featureClass = FeatureClass.P;
            }

            searchCriteria = new ToponymSearchCriteria();
            try {
                if(typeSearch.equals(mContext.getString(R.string.search_for_continents))){
                    //api.geonames.org/search?q=cont&username=entage&fcode=CONT&lang=ar
                    searchCriteria.setQ("cont");
                }
                else if(typeSearch.equals(mContext.getString(R.string.search_for_states))){
                    //api.geonames.org/search?country=US&username=entage&fclass=A&fcode=ADM1&lang=ar
                    searchCriteria.setCountryCode(_area);
                    searchCriteria.setFeatureClass(featureClass);
                }
                else if(typeSearch.equals(mContext.getString(R.string.search_for_cities))){
                    // api.geonames.org/search?name_startsWith=مك&country=sa&username=entage&featureClass=P&lang=ar
                    searchCriteria.setNameStartsWith(nameStartsWith);
                    searchCriteria.setCountryCode(_area);
                    searchCriteria.setFeatureClass(featureClass);
                }
                else {
                    searchCriteria.setNameStartsWith(nameStartsWith);
                    searchCriteria.setFeatureClass(featureClass);
                    searchCriteria.setStartRow(100);
                }

                searchCriteria.setFeatureCode(_featureCode);
                searchCriteria.setStyle(Style.SHORT);
                searchCriteria.setLanguage(lang);

                this.execute();
            } catch (InvalidParameterException e) {
                e.printStackTrace();
            }
        }

        /**  **/
        @Override
        protected ToponymSearchResult doInBackground(Integer... integers) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()

            WebService.setUserName(globalVariable.GEONAMES_USERNAME);
            ToponymSearchResult searchResult = null;
            try {
                searchResult = WebService.search(searchCriteria);
                searchResult.setStyle(Style.SHORT);

            } catch (UnknownHostException e) {
                e.printStackTrace();
                //Toast.makeText(mContext, mContext.getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                //messageDialog.errorMessage(mContext,mContext.getString(R.string.error_internet));
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(mContext,e.getMessage(), Toast.LENGTH_SHORT).show();
                //messageDialog.errorMessage(mContext,e.getMessage());
            }
            return searchResult;
        }

        @Override
        protected void onPostExecute(ToponymSearchResult searchResult) {
            if(textViewSearch != null){
                textViewSearch.setClickable(true);
                textViewSearch.setVisibility(View.VISIBLE);
            }
            setResultList(searchResult);
        }

        @Override
        protected void onPreExecute() {
            if(textViewSearch != null){
                textViewSearch.setClickable(false);
                textViewSearch.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // finalResult.setText(text[0]);
        }

    }

}
