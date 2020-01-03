package com.entage.nrd.entage.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.home.ActivityForOpenFragments;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.Models.GarbageUid;
import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.Models.User;
import com.entage.nrd.entage.Models.UserAccountSettings;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.CustomPagerAdapterLayouts;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.ExceptionsMessages;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";

    private View view;
    private Context mContext;
    private PressBack mPressBack;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private CustomPagerAdapterLayouts adapterLayouts;

    private DatabaseReference referenceUserNames;

    private ImageView backArrow;
    private AppCompatButton butRegister;
    private ProgressBar butProgressBar;
    private TextView textError, terms_and_Conditions, privacy_policy;

    private EditText mUserName, mFirstName, mLastName;
    private CountryCodePicker mCountry;
    private AutoCompleteTextView mCity ;
    private TextView search ;
    private RadioButton mSexMale;
    private TextInputLayout textInputLayout;

    private ArrayList<String> citiesNames;
    private ArrayList<LocationInformation> citiesInformation;
    private LocationInformation locationInformation;
    private HashMap<String, String> statesId;
    private String countryCode, lang;
    private GlobalVariable globalVariable;

    // Loginby phone number
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId, counter;
    private CountDownTimer  countDownTimer;

    private MessageDialog messageDialog = new MessageDialog();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        mContext = getActivity();

        referenceUserNames = FirebaseDatabase.getInstance().getReference().child("usernames");
        init();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mPressBack = (PressBack) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void init() {
        initWidgets();

        onClickListener();

        setupAdapter();
    }

    private void setupAdapter(){
        statesId = new HashMap<>();
        List<Integer> resources = new ArrayList<>();
        resources.add(R.layout.layout_login_email);
        resources.add(R.layout.layout_login_phone);
        adapterLayouts = new CustomPagerAdapterLayouts(mContext, resources);

        viewPager.setAdapter(adapterLayouts);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(mContext.getString(R.string.loginby_email));
        tabLayout.getTabAt(1).setText(mContext.getString(R.string.loginby_phone_number));

        //
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
                Log.d(TAG, "onCountrySelected: " + mCountry.getSelectedCountryNameCode());
            }
        });

    }

    private void initWidgets() {
        viewPager = view.findViewById(R.id.viewpager_info_register);
        tabLayout = view.findViewById(R.id.tabs);
        backArrow = view.findViewById(R.id.backArrow);
        butRegister = view.findViewById(R.id.buttonRegisterAccount);
        butProgressBar = view.findViewById(R.id.progressBarRegister);
        textError = view.findViewById(R.id.textError);
        terms_and_Conditions = view.findViewById(R.id.Terms_and_Conditions);
        privacy_policy = view.findViewById(R.id.Privacy_policy);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputLayout.setErrorEnabled(true);

        mUserName = view.findViewById(R.id.inputUserNme);
        mFirstName = view.findViewById(R.id.inputFirstName);
        mLastName = view.findViewById(R.id.inputLastName);
        mSexMale = view.findViewById(R.id.sexMale);
        mCountry = view.findViewById(R.id.country);
        mCity =  view.findViewById(R.id.input_city);
        search =  view.findViewById(R.id.search);

        lang = Locale.getDefault().getLanguage();
        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        butRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData(viewPager.getCurrentItem());
            }
        });

        mUserName.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                checkIfUserNameExist(mUserName.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
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

        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                intent.putExtra("notification_flag", mContext.getString(R.string.privacy_policy_text));
                startActivity(intent);
            }
        });
        terms_and_Conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                intent.putExtra("notification_flag", mContext.getString(R.string.term_text));
                startActivity(intent);
            }
        });
    }

    private void collectData(int viewIndex){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String username = mUserName.getText().toString();
        String first_name = mFirstName.getText().toString();
        String last_name = mLastName.getText().toString();
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

                    User user = new User(null, username, username.toLowerCase(), null, null, uid, null);

                    UserAccountSettings userAccountSettings = new UserAccountSettings(null, username,  first_name, last_name,
                            locationInformation,
                            "0", "0", "", null, null, mSexMale.isChecked()? "male" : "female",
                            false, null,
                            language, currency) ;

                    checkIfUserNameExist(user, userAccountSettings, viewIndex);
                }
            }
        }
    }

    // register By Email
    private void registerByEmail(View _view, User user, UserAccountSettings userAccountSettings){
        final String email = ((EditText)_view.findViewById(R.id.inputEmail)).getText().toString();
        String password = ((EditText)_view.findViewById(R.id.inputPassword)).getText().toString();
        String passwordConfirm = ((EditText)_view.findViewById(R.id.inputCheckPassword)).getText().toString();

        if(!isStringNull(email, "0000000") && !checkPassword(password, passwordConfirm)){
            if(checkMatchThePasswords(password, passwordConfirm)){

                // Sign in method
                registerNewEmilFromAnonymous(email, password, user, userAccountSettings);

            }else {
                enableButton(true);
            }
        }else {
            enableButton(true);
        }
    }

    private void registerNewEmilFromAnonymous(String email, String password, final User user, final UserAccountSettings userAccountSettings){
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseUser firebaseUser =  FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.linkWithCredential(credential)
                    .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                deleteIdFromAnonymous(firebaseUser.getUid());

                                // send verification email
                                sendVerificationEmail(firebaseUser);

                                user.setUser_id(firebaseUser.getUid());
                                user.setEmail(firebaseUser.getEmail());
                                user.setSign_in_method("email");

                                userAccountSettings.setUser_id(firebaseUser.getUid());
                                userAccountSettings.setEmail(firebaseUser.getEmail());

                                addDataUserInDatabase(user, userAccountSettings);
                            } else  {
                                try
                                {
                                    throw Objects.requireNonNull(task.getException());
                                }
                                catch (Exception e)
                                {
                                    setErrorMessage(ExceptionsMessages.getExceptionMessage(mContext, e));
                                }
                                enableButton(true);
                            }
                        }
                    });

        }else {
            enableButton(true);
            setErrorMessage( mContext.getString(R.string.register_failed));
        }
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser){
        Log.d(TAG, "sendVerificationEmail: userUid: " + firebaseUser.getUid());
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            /*Toast.makeText(mContext, mContext.getString(R.string.signup_successful_sending_verification_emil),
                                    Toast.LENGTH_LONG).show();*/
                        }else{
                           /* Toast.makeText(mContext, mContext.getString(R.string.couldnt_send_verification_email),
                                    Toast.LENGTH_LONG).show();*/
                        }
                    }
                });
    }

    // register By Phone
    private void registerByPhone(View _view, final User user, final UserAccountSettings userAccountSettings){
        String phone = ((EditText)_view.findViewById(R.id.inputPhoneNumber)).getText().toString();
        String mCodePhoneNum = ((CountryCodePicker)_view.findViewById(R.id.codePhoneNumber)).getSelectedCountryCodeWithPlus();

        final RelativeLayout relLayout_loginByPhone = _view.findViewById(R.id.relLayout_loginByPhone);
        final RelativeLayout relLayout_verificationCode = _view.findViewById(R.id.relLayout_verificationCode);
        final EditText mVerify_number = _view.findViewById(R.id.inputVerifyNumber);
        final AppCompatButton buttonVerifyNumber = _view.findViewById(R.id.buttonVerifyNumber);
        AppCompatButton buttonResendCheckCode = _view.findViewById(R.id.buttonResendCheckCode);

        final String phoneNumber = mCodePhoneNum + phone;

        buttonVerifyNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVerificationId != null ){
                    if (mVerify_number.getText().toString().length() < 2 ) {
                        setErrorMessage(mContext.getString(R.string.error_fill_all_blank));

                    }else {
                        enableButton(false);

                        verifyPhoneNumberWithCode(mVerificationId, mVerify_number.getText().toString(), relLayout_loginByPhone,
                                relLayout_verificationCode , user, userAccountSettings);
                    }
                }
            }
        });
        buttonResendCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResendToken != null){
                    enableButton(false);
                    resendVerificationCode(phoneNumber, mResendToken);
                    //counterResendCodeNumber();
                }
            }
        });

        if(!isStringNull(phone, "00000000")) {
            enableButton(false);

            startPhoneNumberVerification(phoneNumber, relLayout_loginByPhone, relLayout_verificationCode, user, userAccountSettings);

        }else {
            enableButton(true);
        }
    }


    private void startPhoneNumberVerification(String phoneNumber, RelativeLayout layout_login, RelativeLayout layout_code,
                                              final User user, final UserAccountSettings userAccountSettings) {
        //init CallBack
        init_mCallbacks(layout_login, layout_code, user, userAccountSettings);

        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void init_mCallbacks(final RelativeLayout layout_login, final RelativeLayout layout_code, final User user,
                                 final UserAccountSettings userAccountSettings) {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted: KOK " + credential);

                signInWithPhoneAuthCredential(credential, layout_login, layout_code,user, userAccountSettings);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    setErrorMessage(mContext.getString(R.string.error_phone_wrong));

                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    setErrorMessage(mContext.getString(R.string.error_quota_exceeded_sms));

                }else{
                    setErrorMessage(e.getMessage());
                }

                enableButton(true);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                layout_login.setVisibility(View.GONE);
                layout_code.setVisibility(View.VISIBLE);

                enableButton(true);
                counterResendCodeNumber(layout_code);
            }
        };
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential, final RelativeLayout layout_login,
                                               final RelativeLayout layout_code, final User user,
                                               final UserAccountSettings userAccountSettings) {

        final String anonymousId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {// Sign in success
                            // delete current user first
                            //deleteCurrentUser(uid, mAuth.getCurrentUser().getPhoneNumber());
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                            // check if phone number exist login, if not create new account
                            checkPhoneNumber(anonymousId, firebaseUser, user,userAccountSettings);

                        } else {
                            enableButton(true);
                            // Sign in failed
                            try
                            {
                                throw Objects.requireNonNull(task.getException());
                            }
                            catch (Exception e)
                            {
                                setErrorMessage(ExceptionsMessages.getExceptionMessage(mContext, e));
                            }
                        }
                    }
                });
    }

    private void counterResendCodeNumber(RelativeLayout layout_code) {
        final AppCompatButton button = (AppCompatButton) ((RelativeLayout)layout_code.getChildAt(3)).getChildAt(0);
        button.setEnabled(false);
        button.setText("");
        counter = "";

        countDownTimer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                //counter = ""+(millisUntilFinished / 1000);
                counter = String.format(Locale.getDefault(), "%d "+
                                mContext.getString(R.string.minutes)+ " " + mContext.getString(R.string.and_text)+
                                " %d "+ mContext.getString(R.string.seconds),
                        TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                button.setText(counter);
            }
            public void onFinish() {
                button.setText(mContext.getString(R.string.resend_check_verify_number));
                button.setEnabled(true);
            }
        }.start();
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code, final RelativeLayout layout_login,
                                           final RelativeLayout layout_code, final User user,
                                           final UserAccountSettings userAccountSettings) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential, layout_login, layout_code, user, userAccountSettings);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
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

    private boolean checkPassword(String string1, String string2){
        if(string1 != null && string2 != null && string1.length()>1 && string2.length()>1){
            return false;

        }else {
            setErrorMessage(mContext.getString(R.string.password_wrong));
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
        mPressBack.canGoBack(boo);
        butProgressBar.setVisibility(boo ? View.GONE : View.VISIBLE);
        butRegister.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        butRegister.setEnabled(boo);
    }

    private void setErrorMessage(String msg){
        textError.setVisibility(View.GONE);
        textError.setText(msg);
        textError.setVisibility(View.VISIBLE);
    }

    private boolean checkSelectCity() {
        if(locationInformation == null){
            setErrorMessage(mContext.getString(R.string.error_select_city));
            return false;
        }else {
            return true;
        }
    }

    //
    private boolean checkMatchThePasswords(String _password, String _checkPassword) {
        if (_password.equals(_checkPassword)) {
            return true;
        }else {
            setErrorMessage(mContext.getString(R.string.error_password_not_match));
            return false;
        }
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

    private void addIdToGarbage(String uid, String email) {
        FirebaseDatabase.getInstance().getReference().child("garbage_uid").child(uid)
                .setValue(new GarbageUid(uid, DateTime.getTimestamp(), email));
    }

    //
    private void checkIfUserNameExist(final User user, final UserAccountSettings userAccountSettings, final int viewIndex) {
        final String username = encodeString(user.getUsername());
        Query query = referenceUserNames.child(username.toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    mUserName.setText(username);

                    if(viewIndex == 0){ // login by email
                        registerByEmail(viewPager.getChildAt(viewIndex), user, userAccountSettings);

                    }else if (viewIndex == 1){ // login by phone
                        if(viewPager.getChildAt(viewIndex).findViewById(R.id.relLayout_loginByPhone).getVisibility() == View.GONE){
                            viewPager.getChildAt(viewIndex).findViewById(R.id.relLayout_verificationCode).setVisibility(View.GONE);
                            viewPager.getChildAt(viewIndex).findViewById(R.id.relLayout_loginByPhone).setVisibility(View.VISIBLE);
                            enableButton(true);

                        }else {
                            if(countDownTimer != null ){
                                countDownTimer.cancel();
                            }
                            registerByPhone(viewPager.getChildAt(viewIndex), user, userAccountSettings);
                        }
                    }

                }else {
                    enableButton(true);
                    setErrorMessage(mContext.getString(R.string.error_name_token), R.style.ErrorText);
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
            referenceUserNames.child(username.toLowerCase()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()){
                        setErrorMessage("@"+_username, R.style.ErrorTextGreen);
                    }else {
                        setErrorMessage(mContext.getString(R.string.error_name_token), R.style.ErrorText);
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

    private void setErrorMessage(String message, int resId){
        textInputLayout.setErrorTextAppearance(resId);
        textInputLayout.setError(message);
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

    private void checkPhoneNumber(String anonymousId, final FirebaseUser firebaseUser, final User user,
                                  final UserAccountSettings userAccountSettings){
        Log.d(TAG, "checkPhoneNumber: " + firebaseUser.getPhoneNumber());

        if(firebaseUser.getDisplayName() != null && firebaseUser.getDisplayName().length() > 0){
            // there is account with this phone
            addIdToGarbage(anonymousId, firebaseUser.getPhoneNumber());
            deleteIdFromAnonymous(anonymousId);

            restartApp();

        }else {
            // new account with this phone
            deleteIdFromAnonymous(firebaseUser.getUid());

            // no need to add the id to garbage because we linked the id with our new account

            user.setUser_id(firebaseUser.getUid());
            user.setPhone_number(firebaseUser.getPhoneNumber());
            user.setSign_in_method("phone");

            userAccountSettings.setUser_id(firebaseUser.getUid());
            userAccountSettings.setPhone_number(firebaseUser.getPhoneNumber());

            addDataUserInDatabase(user, userAccountSettings);
        }
    }

    private void addDataUserInDatabase(final User user, final UserAccountSettings userAccountSettings){
        final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

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

                                                referenceUserNames.child(encodeString(user.getUsername())).setValue(user.getUser_id());
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(user.getUsername()).build();

                                                firebaseUser.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                Toast.makeText(mContext, mContext.getString(R.string.signup_successful),
                                                                        Toast.LENGTH_LONG).show();
                                                                restartApp(); // Restart App after Complete

                                                            }
                                                        });
                                            }
                                        }
                                    });

                        } else  {
                            FirebaseAuth.getInstance().signOut();
                            restartApp();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseAuth.getInstance().signOut();
                        restartApp();
                    }
                });
    }

    private void hideKeyboard(){
        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        ((InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void startSearch(String countryCode, String text, String typeSearch, String featureClass, String featureCode){
        locationInformation = null;
        hideKeyboard();
        RegisterFragment.GeonamesSearch geonamesSearc = new RegisterFragment.GeonamesSearch();
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

