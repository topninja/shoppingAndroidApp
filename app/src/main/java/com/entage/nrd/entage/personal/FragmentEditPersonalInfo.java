package com.entage.nrd.entage.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.home.HomeActivity;
import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.Models.User;
import com.entage.nrd.entage.Models.UserAccountSettings;
import com.entage.nrd.entage.Models.UserSettings;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

public class FragmentEditPersonalInfo extends Fragment {
    private static final String TAG = "FragmentEditPe";

    @SuppressLint("LongLogTag")
    public void onConfirmPassword(String password){
        Log.d(TAG, "onConfirmPasswordString: go the password: " + password);

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
       /* AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");

                            updateAccountInformation();
                        }else {
                            generalDialog.showDialog("", "فشل عملية التحقق من الرقم السري", R.drawable.ic_check_password_wrong);
                            mUserName.setText(mUserSettings.getUser().getUsername());
                            mEmail.setText(mUserSettings.getUser().getEmail());
                            //Toast.makeText(getActivity(),"فشلة عملية التحقق من الرقم السري", Toast.LENGTH_SHORT).show();
                        }

                    }
                });*/

    }

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private View view;
    private Context mContext;

    private ImageView backArrow;
    private TextView  titlePage, savePersonalInfo ;

    private EditText mFirstName, mLastName, mPhoneNum, mEmail;
    private CountryCodePicker mCountry,  mCodePhoneNum;
    private AutoCompleteTextView mCity, currencies; ;
    private TextView search;
    private RadioButton mSexMale, mSexFemale;
    private RelativeLayout relativeLayoutPhone;

    private ArrayList<String> citiesNames;
    private ArrayList<LocationInformation> citiesInformation;
    private String countryCode, lang, currency;
    private LocationInformation locationInformation;

    private ProgressBar progressBar;
    private UserSettings mUserSettings = new UserSettings();

    private HashMap<String, String> statesId;

    private MessageDialog messageDialog = new MessageDialog();
    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_personal_info , container , false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    private void inti(){
        initWidgets();
        onClickListener();
        isUserAuhEmail();
        getInfo();
    }

    private void initWidgets(){
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.my_personal_info));
        mEmail = view.findViewById(R.id.email);
        mFirstName = view.findViewById(R.id.inputFirstName);
        mLastName = view.findViewById(R.id.inputLastName);
        mPhoneNum = view.findViewById(R.id.inputPhoneNumber);
        mSexMale = view.findViewById(R.id.sexMale);
        mSexFemale = view.findViewById(R.id.sexFemale);
        mCountry = view.findViewById(R.id.country);
        mCity =  view.findViewById(R.id.input_city);
        search =  view.findViewById(R.id.search);
        mCodePhoneNum =  view.findViewById(R.id.codePhoneNumber);
        relativeLayoutPhone =  view.findViewById(R.id.relLayoutPhone) ;
        currencies =  view.findViewById(R.id.currencies);


        progressBar = view.findViewById(R.id.progressBar);
        savePersonalInfo = view.findViewById(R.id.save);
        savePersonalInfo.setVisibility(View.VISIBLE);

        if (isUserAuhEmail()){
            mEmail.setVisibility(View.GONE);
            relativeLayoutPhone.setVisibility(View.VISIBLE);
        }else {
            mEmail.setVisibility(View.VISIBLE);
            relativeLayoutPhone.setVisibility(View.GONE);
        }

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

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

        mSexFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSexMale.setChecked(!isChecked);
            }
        });
        mSexMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSexFemale.setChecked(!isChecked);
            }
        });

        mCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                locationInformation = null;
                citiesNames.clear();
                citiesInformation.clear();
                mCity.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));

                countryCode = mCountry.getSelectedCountryNameCode();
                currency = getCurrencyCode(countryCode);
                currencies.setText(currency);
            }
        });

        savePersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInformation();
            }
        });
        savePersonalInfo.setEnabled(false);


        mCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                hideKeyboard();
                Object item = parent.getItemAtPosition(position);
                locationInformation = citiesInformation.get(position);
            }
        });
        mCity.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                mCity.showDropDown();
                return false;
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countryCode != null){
                    if(mCity.getText().length() > 0){
                        String text = StringManipulation.removeLastSpace(mCity.getText().toString());
                        startSearch(countryCode, text, mContext.getString(R.string.search_for_cities)
                                ,"P", "");
                    }
                }else {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_select_country_firs));
                }
            }
        });

        mCity.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(countryCode != null){
                        if(mCity.getText().length() > 0){
                            String text = StringManipulation.removeLastSpace(mCity.getText().toString());
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


        setupCurrenciesAdapter();
    }

    private void setupCurrenciesAdapter(){
        // Adapter Currencies
        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> currenciesList = new ArrayList<>();
        Currency _currency ;
        for (Locale locale : locales) {
            try {
                _currency = Currency.getInstance(locale);
                currenciesList.add(_currency.getCurrencyCode()+" "+_currency.getDisplayName());
            } catch (Exception ignored) { }
        }
        Collections.sort(currenciesList);
        currenciesList = removeDuplicates(currenciesList);

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
                currenciesList.remove(currencyInList);
                currenciesList.add(0, currencyInList);
            } catch (Exception ignored) { }
        }


        ArrayAdapter<String> adapter = new  ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, currenciesList);
        currencies.setAdapter(adapter);
        currencies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                hideKeyboard();

                Object item = parent.getItemAtPosition(position);
                //CurrencyItem = ((String)item).substring(0, 3); // get first three char
                currency = ((String)item).substring(0, 3); // get first three char
            }
        });
        currencies.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                currencies.showDropDown();
                return false;
            }
        });
        //
    }

    private void getInfo(){
        savePersonalInfo.setEnabled(false);
        savePersonalInfo.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            final String user_id = firebaseUser.getUid();

            Query query = myRef.child(mContext.getString(R.string.dbname_users))
                    .child(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUserSettings.setUser(dataSnapshot.getValue(User.class));

                    Query query1 = myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                            .child(user_id);
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mUserSettings.setUserAccountSettings(dataSnapshot.getValue(UserAccountSettings.class));
                            setProfileWidgets(mUserSettings);

                            progressBar.setVisibility(View.GONE);
                            savePersonalInfo.setEnabled(true);
                            savePersonalInfo.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(mContext, mContext.getString(R.string.error_internet),
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(mContext, mContext.getString(R.string.error_internet),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void setProfileWidgets(UserSettings userSettings){
        mEmail.setText(userSettings.getUser().getEmail());
        mFirstName.setText(userSettings.getUserAccountSettings().getFirst_name());
        mLastName.setText(userSettings.getUserAccountSettings().getLast_name());

        LocationInformation locationInformation = userSettings.getUserAccountSettings().getLocation_information();

        mCountry.setCountryForNameCode(locationInformation.getCountry_code());
        countryCode = locationInformation.getCountry_code();
        citiesNames.add(locationInformation.getCity_name());
        citiesInformation.add(locationInformation);

        mCity.setText(locationInformation.getCity_name());

        mCity.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));

        lang = userSettings.getUserAccountSettings().getLanguage();

        currency = userSettings.getUserAccountSettings().getCurrency();
        currencies.setText(currency);

        if(userSettings.getUser().getPhone_number() != null){
            if(userSettings.getUser().getPhone_number().length() > 0){
                String number = String.valueOf(userSettings.getUser().getPhone_number());
                number = number.replace("+","");
                String upToNCharacters = number.substring(0, Math.min(number.length(), 3));
                mCodePhoneNum.setCountryForPhoneCode(Integer.parseInt(upToNCharacters));
                mPhoneNum.setText(number.substring(3));
            }
        }

        if(String.valueOf(userSettings.getUserAccountSettings().getSex()).equals("male")){
            mSexMale.setChecked(true);
            mSexFemale.setChecked(false);
        }else {
            mSexFemale.setChecked(true);
            mSexMale.setChecked(false);
        }

        this.locationInformation = locationInformation;
    }

    private void updateInformation() {
        Log.d(TAG, "updateInformation: ");

        // 
        if(locationInformation != null){
            savePersonalInfo.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            final UserSettings userSettings = mUserSettings ;
            userSettings.getUserAccountSettings().setFirst_name(mFirstName.getText().toString());
            userSettings.getUserAccountSettings().setLast_name(mLastName.getText().toString());
            userSettings.getUserAccountSettings().setSex((mSexMale.isChecked()) ? "male" : "female");
            //userSettings.getUserAccountSettings().getLocationInformation(mCountry.getSelectedCountryNameCode().toString());
            //userSettings.getUserAccountSettings().setCity(mCity.getSelectedItem().toString());
            userSettings.getUserAccountSettings().setLocation_information(locationInformation);
            userSettings.getUserAccountSettings().setCurrency(currency);

            if(isUserAuhEmail()){
                String number = mCodePhoneNum.getSelectedCountryCode() + mPhoneNum.getText().toString();
                userSettings.getUser().setPhone_number(number);
                userSettings.getUserAccountSettings().setPhone_number(number);
            }else {
                userSettings.getUserAccountSettings().setEmail(mEmail.getText().toString());
                userSettings.getUser().setEmail(mEmail.getText().toString());
            }

            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(userSettings.getUser())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(userSettings.getUserAccountSettings())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            progressBar.setVisibility(View.GONE);
                                            savePersonalInfo.setVisibility(View.VISIBLE);

                                            Toast.makeText(mContext, mContext.getString(R.string.successfully_save)
                                                    , Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            //getActivity().finish();
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            savePersonalInfo.setVisibility(View.VISIBLE);
                            if(e.getMessage().equals("Permission denied")){
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_permission_denied));
                            }else {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.failed_save_)
                                        +", "+mContext.getString(R.string.error_internet));
                            }
                        }
                    }) ;


        }else {
            Toast.makeText(mContext, mContext.getString(R.string.error_select_city),
                    Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private boolean isUserAuhEmail(){
        if (mAuth.getCurrentUser().getEmail() == null){
            return false ;
        }else {
            return !mAuth.getCurrentUser().getEmail().equals("");
        }
    }

    private void hideKeyboard(){
        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private String getCurrencyCode(String countryCode){
        Currency curr = null;
        try {
            curr = Currency.getInstance(new Locale(Locale.getDefault().getLanguage(), countryCode));
        }catch (Exception ignored){}

        if(curr != null){
            return curr.getCurrencyCode();
        }else {
            return "USD";
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

     /*
    -------------------------------Firebase-------------------------------------------------------
     */
    /**
     * SetUP Firebase Auth
     */
    @SuppressLint("LongLogTag")
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "SignIn : Uid:  " + user.getUid());
                }else {
                    Log.d(TAG, "SignOut");
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


    private void startSearch(String countryCode, String text, String typeSearch, String featureClass, String featureCode){
        locationInformation = null;
        hideKeyboard();
        FragmentEditPersonalInfo.GeonamesSearch geonamesSearc = new FragmentEditPersonalInfo.GeonamesSearch();
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

                    mCity.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));
                    mCity.showDropDown();
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
