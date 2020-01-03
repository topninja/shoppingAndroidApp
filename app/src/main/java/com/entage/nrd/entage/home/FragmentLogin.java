package com.entage.nrd.entage.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.entage.nrd.entage.BuildConfig;
import com.entage.nrd.entage.login.RegisterActivity;
import com.entage.nrd.entage.Models.GarbageUid;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CustomPagerAdapterLayouts;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.ExceptionsMessages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class FragmentLogin extends Fragment {
    private static final String TAG = "FragmentLogin";

    private View view;
    private Context mContext;
    private ViewGroup container;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private CustomPagerAdapterLayouts adapterLayouts;

    private AppCompatButton butLogin, butNewAccount;
    private ProgressBar progressBarLogin;
    private TextView textError, problemSigin, privacyPolicy, termsAndConditions;
    private ImageView options_search;
    private boolean isComeFromPersonalActivity = false;

    // Loginby phone number
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId, counter;
    private CountDownTimer  countDownTimer;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_login, container, false);
            mContext = getActivity();
            this.container = container;

            getIncomingBundle();
            setupFirebaseAuth();
            inti();

        }
        return view;
    }

    public void getIncomingBundle() {
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                isComeFromPersonalActivity =  bundle.getBoolean("isComeFromPersonalActivity");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void inti() {
        initWidgets();
        onClickListener();
        setupAdapter();

        clearSharedPreferences(); // do this here insted in sign out, because need time to preform
    }

    private void initWidgets() {
        viewPager = view.findViewById(R.id.viewpager_info_register);
        tabLayout = view.findViewById(R.id.tabs);

        butLogin = view.findViewById(R.id.butLogin);
        textError = view.findViewById(R.id.textError);
        progressBarLogin = view.findViewById(R.id.progressBarLogin);
        problemSigin = view.findViewById(R.id.problem_login);

        butNewAccount = view.findViewById(R.id.buttonRegisterNewAccount);

        options_search  = view.findViewById(R.id.options_search);
        ((TextView)view.findViewById(R.id.textVersion)).setText(BuildConfig.VERSION_NAME);

        privacyPolicy = view.findViewById(R.id.privacy_policy);
        termsAndConditions = view.findViewById(R.id.terms_and_Conditions);


        if(isComeFromPersonalActivity){
            ((TextView)view.findViewById(R.id.title)).setText(mContext.getString(R.string.login));
            ((TextView)view.findViewById(R.id.title)).setVisibility(View.VISIBLE);

            view.findViewById(R.id.back_topbar).setVisibility(View.VISIBLE);
            options_search.setVisibility(View.GONE);
            view.findViewById(R.id.back_topbar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
    }

    private void onClickListener(){
        options_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager viewPager = (ViewPager) container;
                viewPager.setCurrentItem(1, true);
            }
        });

        butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem() == 0){ // login by email
                    loginEmail(viewPager.getChildAt(0));
                }else if (viewPager.getCurrentItem() == 1){ // login by phone
                    if(viewPager.getChildAt(1).findViewById(R.id.relLayout_loginByPhone).getVisibility() == View.GONE){
                        viewPager.getChildAt(1).findViewById(R.id.relLayout_verificationCode).setVisibility(View.GONE);
                        viewPager.getChildAt(1).findViewById(R.id.relLayout_loginByPhone).setVisibility(View.VISIBLE);

                    }
                    else {
                        if(countDownTimer != null ){
                            countDownTimer.cancel();
                        }
                        loginPhone(viewPager.getChildAt(1));
                    }
                }
            }
        });

        butNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                intent.putExtra("new_register", true);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
            }
        });

        problemSigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // first open activity then open fragment
                Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                intent.putExtra("notification_flag", mContext.getString(R.string.problem_login));
                startActivity(intent);
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                intent.putExtra("notification_flag", mContext.getString(R.string.privacy_policy_text));
                startActivity(intent);
            }
        });
        termsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                intent.putExtra("notification_flag", mContext.getString(R.string.term_text));
                startActivity(intent);
            }
        });
    }

    private void setupAdapter(){
        List<Integer> resources = new ArrayList<>();
        resources.add(R.layout.layout_login_email);
        resources.add(R.layout.layout_login_phone);
        adapterLayouts = new CustomPagerAdapterLayouts(mContext, resources);

        viewPager.setAdapter(adapterLayouts);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(mContext.getString(R.string.loginby_email));
        tabLayout.getTabAt(1).setText(mContext.getString(R.string.loginby_phone_number));
    }

    // loginEmail
    private void loginEmail(View _view){
        final String email = ((EditText)_view.findViewById(R.id.inputEmail)).getText().toString();
        String password = ((EditText)_view.findViewById(R.id.inputPassword)).getText().toString();
        Log.d(TAG, "loginEmail: " + email + " : " + password);

        if(!isStringNull(email, password)){
            enableButton(false);
            final String anonymousId = mAuth.getCurrentUser().getUid();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "Sighn With Emil&Passwoerd:onComplete: " + task.isSuccessful());
                            FirebaseUser user =  mAuth.getCurrentUser();

                            if (task.isSuccessful()) { // Sign in success
                                // delete current user first
                                addIdToGarbage(anonymousId, email);
                                deleteIdFromAnonymous(anonymousId);

                                // in case the user delete app without sginout
                                setFalseToAllSubscribes(user.getUid()); // then restart

                            } else {
                                enableButton(true);

                                // If sign in fails, display a message to the user.
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

    }

    // loginEmail
    private void loginPhone(View _view){
        String phone = ((EditText)_view.findViewById(R.id.inputPhoneNumber)).getText().toString();
        String mCodePhoneNum = ((CountryCodePicker)_view.findViewById(R.id.codePhoneNumber)).getSelectedCountryCodeWithPlus();
        Log.d(TAG, "login: " + phone + " : " + mCodePhoneNum);

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
                    Log.d(TAG, "onClick: mVerificationId :" + mVerificationId);
                    if (mVerify_number.getText().toString().length() < 2 ) {
                        setErrorMessage(mContext.getString(R.string.error_fill_all_blank));

                    }else {
                        enableButton(false);

                        verifyPhoneNumberWithCode(mVerificationId, mVerify_number.getText().toString(), relLayout_loginByPhone,
                                relLayout_verificationCode );
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

            startPhoneNumberVerification(phoneNumber, relLayout_loginByPhone, relLayout_verificationCode);
        }
    }

    private void startPhoneNumberVerification(String phoneNumber, RelativeLayout layout_login, RelativeLayout layout_code) {
        //init CallBack
        init_mCallbacks(layout_login, layout_code);

        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration, 2 mint
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void init_mCallbacks(final RelativeLayout layout_login, final RelativeLayout layout_code) {
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

                signInWithPhoneAuthCredential(credential, layout_login, layout_code);
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
        // [END phone_auth_callbacks]
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential, final RelativeLayout layout_login,
                                               final RelativeLayout layout_code) {
        final String anonymousId = mAuth.getCurrentUser().getUid();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user =  mAuth.getCurrentUser();

                        if (task.isSuccessful()) {// Sign in success
                            // delete current user first
                            addIdToGarbage(anonymousId, mAuth.getCurrentUser().getPhoneNumber());
                            deleteIdFromAnonymous(anonymousId);

                            // in case the user delete app without sginout
                            setFalseToAllSubscribes(user.getUid()); // then restart

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
                                           final RelativeLayout layout_code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential, layout_login, layout_code);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration, 5 mint
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    //
    private boolean isStringNull(String string1, String string2){
        if(string1 != null && string2 != null && string1.length()>5 && string2.length()>5){
            return false;

        }else {
            setErrorMessage(mContext.getString(R.string.error_fill_all_blank));
            return true;
        }
    }

    private void enableButton(boolean boo){
        if(!boo){
            textError.setVisibility(View.GONE);
        }
        progressBarLogin.setVisibility(boo ? View.GONE : View.VISIBLE);
        butLogin.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        butLogin.setEnabled(boo);
    }

    private void setErrorMessage(String msg){
        textError.setVisibility(View.GONE);
        textError.setText(msg);
        textError.setVisibility(View.VISIBLE);
    }

    //
    private void deleteIdFromAnonymous(String uid){
        FirebaseDatabase.getInstance().getReference()
                .child("anonymous_account")
                .child(uid)
                .removeValue();
    }

    private void addIdToGarbage(String uid, String email) {
        myRef.child("garbage_uid").child(uid)
                .setValue(new GarbageUid(uid, DateTime.getTimestamp(), email));
    }

    //subscribe
    private void setFalseToAllSubscribes(String userId){
        final DatabaseReference reference = mFirebaseDatabase.getReference().child(getString(R.string.dbname_users_subscribes))
                .child(userId);

        Query query = reference;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String topic = singleSnapshot.getKey();
                        reference.child(topic).setValue(false);
                    }

                }

                restartApp();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                restartApp();
            }
        });
    }

    private void restartApp(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void clearSharedPreferences(){
        SharedPreferences sharedPrefs = mContext.getSharedPreferences("entaji_app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.apply();
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
    }

}
