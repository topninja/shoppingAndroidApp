package com.entage.nrd.entage.personal;

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
import android.text.InputType;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.SharingLocation;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import org.geonames.FeatureClass;
import org.geonames.InsufficientStyleException;
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

public class FragmentAddAddress extends Fragment {
    private static final String TAG = "FragmentEditPe";

    private View view;
    private Context mContext;
    private final int LOCATION_REQUEST_CODE = 350;

    private ImageView backArrow;
    private TextView  titlePage, saveAddress ;
    private ProgressBar progressBar, progressBarSave;
    private EditText title, location_google_map, address, phone_number, email;
    private ImageView icon_google_map;
    private AutoCompleteTextView input_city;
    private TextView search;
    private CountryCodePicker mCountry;

    private ArrayList<MyAddress> myAddresses;
    private int position;

    private MyAddress mMyAddress;
    private LocationInformation locationInformation;
    private String countryCode, lang;
    private ArrayList<LocationInformation> citiesInformation;
    private ArrayList<String> citiesNames;
    private HashMap<String, String> statesId;
    private Address addressGPS;
    private String lanLat;

    private GlobalVariable globalVariable;
    private InputMethodManager imm;
    private MessageDialog messageDialog = new MessageDialog();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_address , container , false);
        mContext = getActivity();

        //setupFirebaseAuth();
        getIncomingBundle();
        inti();

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                myAddresses =  bundle.getParcelableArrayList("myAddresses");
                position = bundle.getInt("position");

                if(position != -1 && myAddresses != null){
                    mMyAddress = myAddresses.get(position);
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void inti(){
        initWidgets();
        setDataInWidgets();
        setupAdapters();
        onClickListener();
    }

    private void initWidgets(){
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        //titlePage.setText(mContext.getString(R.string.my_address));
        progressBar = view.findViewById(R.id.progressBar_9);
        progressBar.setVisibility(View.GONE);
        view.findViewById(R.id.layout_add_new_address).setVisibility(View.VISIBLE);

        title =  view.findViewById(R.id.title);
        icon_google_map =  view.findViewById(R.id.icon_google_map);
        location_google_map =  view.findViewById(R.id.location_google_map);
        input_city =  view.findViewById(R.id.input_city);
        search =  view.findViewById(R.id.search);
        address =  view.findViewById(R.id.address);
        email =  view.findViewById(R.id.phone_number_1);
        phone_number =  view.findViewById(R.id.phone_number_2);
        mCountry =  view.findViewById(R.id.country);

        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setHint(mContext.getString(R.string.hint_login_email));

        progressBarSave =  view.findViewById(R.id.progressBar_save_address);

        view.findViewById(R.id.input_country).setVisibility(View.GONE);
        view.findViewById(R.id.layout_country).setVisibility(View.VISIBLE);

        view.findViewById(R.id.text9).setVisibility(View.GONE);
        view.findViewById(R.id.payment_before_sending).setVisibility(View.GONE);
        view.findViewById(R.id.payment_upon_receipt).setVisibility(View.GONE);
        saveAddress = view.findViewById(R.id.save_location);
        saveAddress.setText(mContext.getString(R.string.save));


        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        if(globalVariable.getLanguage() != null){
            lang = globalVariable.getLanguage();
        }else {
            lang = Locale.getDefault().getLanguage();
        }

        statesId = new HashMap<>();
        citiesNames = new ArrayList<>();
        citiesInformation = new ArrayList<>();
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countryCode != null){
                    if(input_city.getText().length() > 0){
                        String text = StringManipulation.removeLastSpace(input_city.getText().toString());
                        startSearch(countryCode, text, mContext.getString(R.string.search_for_cities)
                                ,"P", "");
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
                    if(countryCode != null){
                        if(input_city.getText().length() > 0){
                            String text = StringManipulation.removeLastSpace(input_city.getText().toString());
                            startSearch(countryCode, text, mContext.getString(R.string.search_for_cities)
                                    ,"P", "");
                        }
                    }else {
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_select_country_firs));
                    }
                    return true;
                }
                return false;
            }
        });


        icon_google_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UtilitiesMethods.checkPermissionsLocations(mContext)){
                    if(isLocationEnabled(mContext)){
                        Intent intent = new Intent(mContext, SharingLocation.class);
                        startActivityForResult(intent, LOCATION_REQUEST_CODE);
                    }else {
                        openGPS();
                    }
                }
            }
        });

        saveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if(data != null && data.hasExtra("lat_lng")){
                    lanLat = data.getStringExtra("lat_lng");
                    addressGPS =  data.getParcelableExtra("address");

                    if(addressGPS != null) {
                        locationInformation = new LocationInformation( null, addressGPS.getSubAdminArea(), addressGPS.getPostalCode(),
                                addressGPS.getAdminArea(), addressGPS.getAdminArea(),  addressGPS.getCountryCode(),
                                addressGPS.getCountryCode(), addressGPS.getPostalCode());
                        location_google_map.setText(addressGPS.getAddressLine(0));
                    }
                }
            }
        }

    }

    private void setDataInWidgets(){
        if(position != -1){
            mMyAddress = myAddresses.get(position);

            titlePage.setText(mMyAddress.getTitle());
            title.setText(mMyAddress.getTitle());
            location_google_map.setText(mMyAddress.getLocation());
            address.setText(mMyAddress.getAddress_home());
            email.setText(mMyAddress.getEmail());
            phone_number.setText(mMyAddress.getPhone_number());

            locationInformation = mMyAddress.getCity();
            countryCode = locationInformation.getCountry_code();
            mCountry.setCountryForNameCode(locationInformation.getCountry_code());
            input_city.setText(locationInformation.getCity_name());
            citiesNames.add(locationInformation.getCity_name());
            citiesInformation.add(locationInformation);
            input_city.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));

        }else {
            titlePage.setText(mContext.getString(R.string.new_address));
        }
    }

    private void setupAdapters() {
        if(globalVariable.getLocationInformation() != null
        && globalVariable.getLocationInformation().getCountry_code()!=null && position == -1){
            LocationInformation locationInformation = globalVariable.getLocationInformation();
            countryCode = locationInformation.getCountry_code();
            mCountry.setCountryForNameCode(countryCode);

            input_city.setText(locationInformation.getCity_name());
            citiesNames.add(locationInformation.getCity_name());
            citiesInformation.add(locationInformation);
            input_city.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));
        }

        mCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                locationInformation = null;
                citiesNames.clear();
                citiesInformation.clear();
                input_city.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));

                countryCode = mCountry.getSelectedCountryNameCode();
            }
        });

        input_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                hideKeyboard();
                Object item = parent.getItemAtPosition(position);
                locationInformation = citiesInformation.get(position);
            }
        });
        input_city.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                input_city.showDropDown();
                return false;
            }
        });
    }

    private void hideKeyboard(){
        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void saveAddress() {
        if(locationInformation != null){
            if(title.getText() != null && title.getText().length()!=0){
                if(phone_number.getText() != null && phone_number.getText().length()>5){
                    if(( address.getText()==null||address.getText().length()==0 )&& addressGPS == null){
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_write_description_address));
                    }else {
                        saveAddress.setClickable(false);
                        progressBarSave.setVisibility(View.VISIBLE);
                        saveAddress.setVisibility(View.INVISIBLE);

                        final int _position = collectData();
                        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.dbname_addresses))
                                .child(user_id)
                                .setValue(myAddresses).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                position = _position;
                                saveAddress.setClickable(true);
                                progressBarSave.setVisibility(View.GONE);
                                saveAddress.setVisibility(View.VISIBLE);

                                Toast.makeText(mContext, getString(R.string.save_successfully), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }else {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_phone_wrong));
                }
            }else {
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_write_title_address));
            }
        }else {
            messageDialog.errorMessage(mContext, mContext.getString(R.string.error_select_city));
        }
    }

    private int collectData() {
        MyAddress myAddress = new MyAddress(title.getText().toString(), null, null, countryCode,
                locationInformation,   address.getText().toString(),  phone_number.getText().toString(),  email.getText().toString());

        if(addressGPS!=null && lanLat != null){
            myAddress.setLat_lng(lanLat);
            myAddress.setLocation(addressGPS.getAddressLine(0));
            myAddress.setCountry(addressGPS.getCountryCode());
        }

        int po = position;
        if(position == -1){
            if(myAddresses == null){
                myAddresses = new ArrayList<>();
            }
            po = myAddresses.size();
            myAddresses.add(po, myAddress);
        }else {
            myAddresses.remove(position);
            myAddresses.add(position, myAddress);
        }
        return po;
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

    private void startSearch(String countryCode, String text, String typeSearch, String featureClass, String featureCode){
        locationInformation = null;
        hideKeyboard();
        FragmentAddAddress.GeonamesSearch geonamesSearc = new FragmentAddAddress.GeonamesSearch();
        geonamesSearc.startSearch(typeSearch, text, countryCode, featureClass, featureCode);
    }

    // in back ground
    private void setResultList(ToponymSearchResult searchResult){
        if (!((Activity)mContext).isDestroyed()){

            // search for city then search for state of city
            boolean searchingForCity = false;
            if (searchResult != null){
                if(searchResult.getToponyms().size() > 0 && searchResult.getToponyms().get(0).getFeatureClass().toString().equals("P")){
                    citiesNames.clear();
                    citiesInformation.clear();
                }

                for (int i=0; i<searchResult.getToponyms().size(); i++)
                {
                    Toponym t = searchResult.getToponyms().get(i);

                    String idArea = String.valueOf(t.getGeoNameId());
                    String nameArea = String.valueOf(t.getName());
                    String countryCode = t.getCountryCode();
                    String nameState = null ;

                    try {
                        nameState = t.getAdminName1();
                    } catch (InsufficientStyleException e) {
                        e.printStackTrace();
                    }

                    if(t.getFeatureClass().toString().equals("P")){ // city
                        searchingForCity = true;
                        citiesNames.add(nameArea);
                        citiesInformation.add(new LocationInformation("", nameArea,
                                idArea, nameState,
                                (nameState != null && statesId.containsKey(nameState))? statesId.get(nameState) : null,
                                countryCode, CountriesData.getCountryIdByCode(countryCode), null));

                        if(nameState != null && !statesId.containsKey(nameState)){
                            statesId.put(nameState, null);
                        }

                    }else if(t.getFeatureClass().toString().equals("A")){ // state
                        statesId.put(nameArea, idArea);
                        for(LocationInformation lc : citiesInformation){
                            if(lc.getState_name().equals(nameArea)){
                                lc.setState_name(nameArea);
                                lc.setState_id(idArea);
                            }
                        }
                    }
                }

                if(searchingForCity){
                    for(String name : statesId.keySet()){
                        if(statesId.get(name) == null){
                            startSearch(mCountry.getSelectedCountryNameCode(), name, mContext.getString(R.string.search_for_states)
                                    ,"A", "ADM1");
                        }
                    }

                    input_city.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));
                    input_city.showDropDown();
                }
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
                    searchCriteria.setNameStartsWith(nameStartsWith);
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
                searchCriteria.setStyle(Style.LONG);
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
            search.setClickable(true);
            search.setVisibility(View.VISIBLE);
            setResultList(searchResult);
        }

        @Override
        protected void onPreExecute() {
            search.setClickable(false);
            search.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // finalResult.setText(text[0]);
        }

    }


}
