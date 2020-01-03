package com.entage.nrd.entage.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.Models.User;
import com.entage.nrd.entage.Models.UserAccountSettings;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

public class FragmentCompleteRegister extends Fragment {
    private static final String TAG = "FragmentCompleteRegiste";

    private View view;
    private Context mContext;

    private DatabaseReference referenceUserNames;
    private String userId;

    private AppCompatButton butRegister, signOut;
    private ProgressBar butProgressBar;
    private TextView textError, terms_and_Conditions, privacy_policy;

    private AutoCompleteTextView mCity ;
    private TextView search ;
    private EditText mUserName, mFirstName;
    private CountryCodePicker mCountry;
    private RadioButton mSexMale;
    private TextInputLayout textInputLayout;

    private ArrayList<String> citiesNames;
    private ArrayList<LocationInformation> citiesInformation;
    private LocationInformation locationInformation;
    private String countryCode, lang;
    private HashMap<String, String> statesId;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        mContext = getActivity();

        referenceUserNames = FirebaseDatabase.getInstance().getReference().child("usernames");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        init();

        return view;
    }

    private void init() {
        initWidgets();

        onClickListener();

        setupAdapter();
    }

    private void initWidgets() {
        view.findViewById(R.id.viewpager_info_register).setVisibility(View.GONE);
        view.findViewById(R.id.tabs).setVisibility(View.GONE);
        view.findViewById(R.id.backArrow).setVisibility(View.GONE);
        view.findViewById(R.id.text10).setVisibility(View.GONE);
        view.findViewById(R.id.relLayou10).setVisibility(View.GONE);
        view.findViewById(R.id.text7).setVisibility(View.GONE);
        view.findViewById(R.id.text8).setVisibility(View.GONE);
        view.findViewById(R.id.line3).setVisibility(View.GONE);

        view.findViewById(R.id.text6).setVisibility(View.VISIBLE);
        butRegister = view.findViewById(R.id.buttonRegisterAccount);
        butProgressBar = view.findViewById(R.id.progressBarRegister);
        textError = view.findViewById(R.id.textError);
        terms_and_Conditions = view.findViewById(R.id.Terms_and_Conditions);
        privacy_policy = view.findViewById(R.id.Privacy_policy);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputLayout.setErrorEnabled(true);

        mCity =  view.findViewById(R.id.input_city);
        search =  view.findViewById(R.id.search);

        signOut =  view.findViewById(R.id.sign_out);
        view.findViewById(R.id. relLayou12).setVisibility(View.VISIBLE);

        mUserName = view.findViewById(R.id.inputUserNme);
        mFirstName = view.findViewById(R.id.inputFirstName);
        mSexMale = view.findViewById(R.id.sexMale);
        mCountry = view.findViewById(R.id.country);
        mCity = view.findViewById(R.id.city);

        ((TextView)view.findViewById(R.id.buttonRegisterAccount)).setText(mContext.getString(R.string.continue_register_text));
        ((TextView)view.findViewById(R.id.title)).setText(mContext.getString(R.string.continue_register_text));

        lang = Locale.getDefault().getLanguage();
        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener() {
        butRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData();
            }
        });

        terms_and_Conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mUserName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkIfUserNameExist(mUserName.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                signOut.setVisibility(View.INVISIBLE);
                FirebaseAuth.getInstance().signOut();

                new CountDownTimer(2000, 1000) {
                    public void onTick(long millisUntilFinished) { }
                    public void onFinish() {
                        restartApp();
                    }
                }.start();
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

    }

    private void setupAdapter(){
        statesId = new HashMap<>();
        citiesInformation = new ArrayList<>();
        citiesNames = new ArrayList<>();

        mCity.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));
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

        countryCode = "SA"; // default
        mCountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                locationInformation = null;
                citiesNames.clear();
                citiesInformation.clear();
                mCity.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, citiesNames));

                countryCode = mCountry.getSelectedCountryNameCode();
            }
        });
    }

    private void collectData(){
        FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String username = mUserName.getText().toString();
        String first_name = mFirstName.getText().toString();
        if(!isStringNull2(username, first_name)){
            username = StringManipulation.condenseUsername(StringManipulation.removeLastSpace(username.replace("\n","")));
            if(checkUserName(username)){
                if(checkSelectCity()){
                    enableButton(false);

                    // get currency of country
                    String currency = null;
                    String language = Locale.getDefault().getLanguage();
                    try {
                        Currency curr = Currency.getInstance(new Locale(language, mCountry.getSelectedCountryNameCode()));
                        currency = curr.getCurrencyCode();
                    } catch (Exception ignored) {

                    }

                    User user = new User(firebaseUser.getUid(), username, username.toLowerCase(), null, null, uid, null);

                    UserAccountSettings userAccountSettings = new UserAccountSettings(firebaseUser.getUid(), username,  first_name, null,
                            locationInformation, "0", "0", "", null, null,
                            mSexMale.isChecked()? "male" : "female", false,  null, language, currency) ;

                    if(firebaseUser.getPhoneNumber() != null && firebaseUser.getPhoneNumber().length() >0){
                        user.setPhone_number(firebaseUser.getPhoneNumber());
                        user.setSign_in_method("phone");
                        userAccountSettings.setPhone_number(firebaseUser.getEmail());
                    }
                    if(firebaseUser.getEmail() != null && firebaseUser.getEmail().length() >0){
                        user.setEmail(firebaseUser.getEmail());
                        user.setSign_in_method("email");
                        userAccountSettings.setEmail(firebaseUser.getEmail());
                    }

                    checkIfUserNameExist(user, userAccountSettings);
                }
            }
        }
    }

    private void checkIfUserNameExist(final User user, final UserAccountSettings userAccountSettings) {
        String username = encodeString(user.getUsername());
        Query query = referenceUserNames.child(username.toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    addDataUserInDatabase( user, userAccountSettings);

                }else {
                    // check maybe step set username already done
                    String uidOfUsername = dataSnapshot.getValue().toString();
                    if(!uidOfUsername.equals(userId)){
                        enableButton(true);
                        setErrorMessage(mContext.getString(R.string.error_name_token), R.style.ErrorText);
                    }else {
                        addDataUserInDatabase( user, userAccountSettings);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                enableButton(true);
                if(databaseError.getMessage().equals("Permission denied")){
                    setErrorMessage(mContext.getString(R.string.error_permission_denied));
                }else{
                    setErrorMessage(mContext.getString(R.string.error_internet));
                }
            }
        });
    }

    private void checkIfUserNameExist(final String _username){
        if(_username.length() > 2){
            String username = encodeString(_username);
            referenceUserNames.child(username.toLowerCase())
                    .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        setErrorMessage("@"+_username, R.style.ErrorTextGreen);

                    }else {
                        // check maybe step set username already done
                        String uidOfUsername = dataSnapshot.getValue().toString();
                        if(!uidOfUsername.equals(userId)){
                            setErrorMessage(mContext.getString(R.string.error_name_token), R.style.ErrorText);
                        }else {
                            setErrorMessage("@"+_username, R.style.ErrorTextGreen);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else {
            setErrorMessage(mContext.getString(R.string.error_username), R.style.ErrorText);
        }
    }

    private String encodeString(String username){
        username = StringManipulation.condenseUsername(StringManipulation.removeLastSpace(username.replace("\n","")));
        username = username.replace("#","");
        username = username.replace("]","");
        username = username.replace("[","");
        username = username.replace("$","");
        username = username.replace(")","");
        username = username.replace("(","");
        username = username.replace("*","");
        username = username.replace("+","");
        username = username.replace("-","");
        username = username.replace("/","");
        username = username.replace("'\'","");
        username = username.replace("|","");
        username = username.replace("=","");
        username = username.replace("-","");
        username = username.replace("&","");
        username = username.replace("^","");
        username = username.replace("@","");
        username = username.replace("!","");
        username = username.replace("`","");
        username = username.replace("?","");
        username = username.replace("}","");
        username = username.replace("{","");
        username = username.replace(">","");
        username = username.replace("<","");
        username = username.replace(";","");
        username = username.replace(":","");
        return username;
    }

    private void hideKeyboard(){
        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //
    private boolean isStringNull2(String string1, String string2){
        if(string1 != null && string2 != null){
            return false;

        }else {
            setErrorMessage(mContext.getString(R.string.error_fill_all_blank));
            return true;
        }
    }

    private boolean isStringNull(String string1, String string2){
        if(string1 != null && string2 != null && string1.length()>5 && string2.length()>5){
            return false;

        }else {
            setErrorMessage(mContext.getString(R.string.error_fill_all_blank));
            return true;
        }
    }

    private boolean checkUserName(String string1){
        if(string1 != null && string1.length()>2 && string1.length()<20){
            return true;

        }else {
            setErrorMessage(mContext.getString(R.string.error_username));
            return false;
        }
    }

    private void enableButton(boolean boo){
        if(!boo){
            textError.setVisibility(View.GONE);
        }
        butProgressBar.setVisibility(boo ? View.GONE : View.VISIBLE);
        butRegister.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        butRegister.setEnabled(boo);
    }

    private void setErrorMessage(String msg){
        textError.setVisibility(View.GONE);
        textError.setText(msg);
        textError.setVisibility(View.VISIBLE);
    }

    private void setErrorMessage(String message, int resId){
        textInputLayout.setErrorTextAppearance(resId);
        textInputLayout.setError(message);
    }

    private boolean checkSelectCity() {
        if(locationInformation == null){
            setErrorMessage(mContext.getString(R.string.error_select_city));
            return false;
        }else {
            return true;
        }
    }

    private void addDataUserInDatabase(final User user, final UserAccountSettings userAccountSettings){
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

        // delete if not
        deleteIdFromAnonymous(user.getUser_id());

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(user.getUser_id())
                .setValue(user)
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                                    .child(user.getUser_id())
                                    .setValue(userAccountSettings)
                                    .addOnCompleteListener((Activity) mContext, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(user.getUsername()).build();

                                                firebaseUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    referenceUserNames.child(encodeString(user.getUsername())).setValue(user.getUser_id())
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(mContext, mContext.getString(R.string.signup_successful),
                                                                                                Toast.LENGTH_LONG).show();
                                                                                        restartApp(); // Restart App after Complete
                                                                                    }else {
                                                                                        enableButton(true);
                                                                                        setErrorMessage(mContext.getString(R.string.happened_wrong_try_again));
                                                                                    }
                                                                                }
                                                                            });
                                                                }else {
                                                                    enableButton(true);
                                                                    setErrorMessage(mContext.getString(R.string.happened_wrong_try_again));
                                                                }

                                                            }
                                                        });
                                            }else{
                                                enableButton(true);
                                                setErrorMessage(mContext.getString(R.string.happened_wrong_try_again));
                                            }
                                        }
                                    });

                        } else  {
                            enableButton(true);
                            setErrorMessage(mContext.getString(R.string.happened_wrong_try_again));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        enableButton(true);
                        setErrorMessage(mContext.getString(R.string.happened_wrong_try_again));
                    }
                });
    }

    private void restartApp(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void deleteIdFromAnonymous(String uid){
        FirebaseDatabase.getInstance().getReference()
                .child("anonymous_account")
                .child(uid)
                .removeValue();
    }

    private void startSearch(String countryCode, String text, String typeSearch, String featureClass, String featureCode){
        locationInformation = null;
        hideKeyboard();
        FragmentCompleteRegister.GeonamesSearch geonamesSearc = new FragmentCompleteRegister.GeonamesSearch();
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
