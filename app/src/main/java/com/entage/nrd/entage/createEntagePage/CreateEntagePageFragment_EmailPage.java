package com.entage.nrd.entage.createEntagePage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

public class CreateEntagePageFragment_EmailPage extends Fragment{
    private static final String TAG = "CreateEntagePageFragmen";

    private FirebaseUser firebaseUser;

    private View view;
    private Context mContext;
    private OnActivityListener onActivityListener;
    private CreateEntagePageListener mCreateEntagePageListener;

    private EditText emailOfPage;
    private TextInputEditText inputPhoneNumber;
    private TextView messageError;
    private CountryCodePicker countryCodePicker;

    private String phoneNumber;
    private boolean isPhoneNumberVerified = true;

    // Number Verification



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
        emailOfPage = view.findViewById(R.id.edit_text);
        messageError = view.findViewById(R.id.messageError);

        emailOfPage.setHint(mContext.getString(R.string.hint_login_email));
        emailOfPage.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        inputPhoneNumber = view.findViewById(R.id.inputPhoneNumber);
        countryCodePicker = view.findViewById(R.id.codePhoneNumber);
        view.findViewById(R.id.linearLayout_phone).setVisibility(View.VISIBLE);

        view.findViewById(R.id.linearLayout_1).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.communication_data));
        ((TextView)view.findViewById(R.id.text2)).setText(mContext.getString(R.string.creat_entage_page_communication_1));
        ((ImageView)view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_call);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        emailOfPage.setText(firebaseUser.getEmail());
        //inputPhoneNumber.setText(firebaseUser.getPhoneNumber());

        //Log.d(TAG, "initWidgets: "  + countryCodePicker.cod);
        if(firebaseUser.getPhoneNumber() != null && firebaseUser.getPhoneNumber().length() > 0){
            //Log.d(TAG, "initWidgets: " + firebaseUser.getPhoneNumber());
            String number = firebaseUser.getPhoneNumber();
            number = number.replace("+","");
            String upToNCharacters = number.substring(0, Math.min(number.length(), 3));
            countryCodePicker.setCountryForPhoneCode(Integer.parseInt(upToNCharacters));
            inputPhoneNumber.setText(number.substring(3));
        }else {
            isPhoneNumberVerified = false;
            //initPhoneVerificationLayout((RelativeLayout) view.findViewById(R.id.relLayout_verificationCode));
        }

    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(null);
        mCreateEntagePageListener.getNextButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(false);
                setEnableButtons(false);
                view.findViewById(R.id.messageErro_2).setVisibility(View.GONE);
                messageError.setText("");

                if(!isPhoneNumberVerified){
                    String number = inputPhoneNumber.getText().toString();
                    phoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + number;
                    if(phoneNumber.length() > 7){
                        //startPhoneNumberVerification(phoneNumber);
                        setEnableButtons(true);
                        String email = emailOfPage.getText().toString();
                        mCreateEntagePageListener.getEntagePage().setEmail_entage_page(email);
                        mCreateEntagePageListener.getEntagePage().setPhone_entage_page(phoneNumber);

                        onActivityListener.onActivityListener(new CreateEntagePageFragment_CurrencyPage());

                    }else {
                        setEnableButtons(true);
                        ((TextView)view.findViewById(R.id.messageErro_2)).setText(mContext.getString(R.string.error_phone_wrong));
                        view.findViewById(R.id.messageErro_2).setVisibility(View.VISIBLE);
                    }

                }else {
                    setEnableButtons(true);
                    String email = emailOfPage.getText().toString();
                    String number = firebaseUser.getPhoneNumber();
                    mCreateEntagePageListener.getEntagePage().setEmail_entage_page(email);
                    mCreateEntagePageListener.getEntagePage().setPhone_entage_page(number);

                    onActivityListener.onActivityListener(new CreateEntagePageFragment_CurrencyPage());
                }
            }
        });
    }

    private boolean checkInputsEmail(String _userName) {
        if (_userName.length() < 5){
            messageError.setText(mContext.getString(R.string.error_email_wrong));
            return false;
        }else{
            return true;
        }
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


/*  // startPhoneNumberVerification
    private String  mVerificationId, counter;
    private PhoneAuthProvider.ForceResendingToken mResendToken;;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;;
    private TextView messageErro_verification;
    private CountDownTimer countDownTimer;
    private AppCompatButton buttonResendCheckCode;

    private void initPhoneVerificationLayout(RelativeLayout relativeLayout){
        final EditText mVerify_number = relativeLayout.findViewById(R.id.inputVerifyNumber);
        AppCompatButton buttonVerifyNumber = relativeLayout.findViewById(R.id.buttonVerifyNumber);
        buttonResendCheckCode = relativeLayout.findViewById(R.id.buttonResendCheckCode);
        messageErro_verification = relativeLayout.findViewById(R.id.messageErro_verification_code);

        buttonVerifyNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageErro_verification.setVisibility(View.GONE);

                if(mVerificationId != null ){
                    if (mVerify_number.getText().toString().length() < 2 ) {
                        messageErro_verification.setVisibility(View.VISIBLE);
                        messageErro_verification.setText((mContext.getString(R.string.error_fill_all_blank)));

                    }else {
                        setEnableButtons(false);

                        verifyPhoneNumberWithCode(mVerificationId, mVerify_number.getText().toString());
                    }
                }
            }
        });
        buttonResendCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mResendToken != null){
                    setEnableButtons(false);
                    resendVerificationCode(phoneNumber, mResendToken);
                    //counterResendCodeNumber();
                }
            }
        });

        init_mCallbacks();
    }

    private void init_mCallbacks() {
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

                isPhoneNumberVerified = true;
                setEnableButtons(true);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    ((TextView)view.findViewById(R.id.messageErro_2)).setText(mContext.getString(R.string.error_phone_wrong));

                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    ((TextView)view.findViewById(R.id.messageErro_2)).setText(mContext.getString(R.string.error_quota_exceeded_sms));

                }else{
                    ((TextView)view.findViewById(R.id.messageErro_2)).setText(e.getMessage());
                }

                view.findViewById(R.id.messageErro_2).setVisibility(View.VISIBLE);
                setEnableButtons(true);
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

                view.findViewById(R.id.relLayout_verificationCode).setVisibility(View.VISIBLE);
                view.findViewById(R.id.phone_number_verification).setVisibility(View.VISIBLE);

                setEnableButtons(true);
                counterResendCodeNumber();
            }
        };
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        view.findViewById(R.id.messageErro_2).setVisibility(View.GONE);

        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                (Activity) mContext,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        view.findViewById(R.id.messageErro_2).setVisibility(View.GONE);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                (Activity) mContext,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void counterResendCodeNumber() {
        buttonResendCheckCode.setEnabled(false);
        buttonResendCheckCode.setText("");
        counter = "";
        countDownTimer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                //counter = ""+(millisUntilFinished / 1000);
                if(isAdded() && mContext!=null){
                    counter = String.format(Locale.getDefault(), "%d "+
                                    mContext.getString(R.string.minutes)+ " " + mContext.getString(R.string.and_text)+
                                    " %d "+ mContext.getString(R.string.seconds),
                            TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    buttonResendCheckCode.setText(counter);
                }else {
                    cancel();
                }
            }
            public void onFinish() {
                if(isAdded() && mContext!=null){
                    buttonResendCheckCode.setText(mContext.getString(R.string.resend_check_verify_number));
                    buttonResendCheckCode.setEnabled(true);
                }else {
                    cancel();
                }
            }
        }.start();
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        Log.d(TAG, "verifyPhoneNumberWithCode: e: " + mResendToken);
        Log.d(TAG, "verifyPhoneNumberWithCode: e: " + mVerificationId);
        Log.d(TAG, "verifyPhoneNumberWithCode: " +  credential.getProvider());
        Log.d(TAG, "verifyPhoneNumberWithCode: " +  credential.getSmsCode());
        Log.d(TAG, "verifyPhoneNumberWithCode: " +  credential.getSignInMethod());

        setEnableButtons(true);
        // [END verify_with_code]
       // signInWithPhoneAuthCredential(credential, layout_login, layout_code, user, userAccountSettings);
    }*/

}
