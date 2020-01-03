package com.entage.nrd.entage.personal;

import android.content.Context;
import android.content.Intent;
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

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ExceptionsMessages;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FragmentChangePhoneNumber extends Fragment {
    private static final String TAG = "FragmentChangeEmail";

    private Context mContext;
    private View view;
    private OnActivityListener mOnActivityListener;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private ImageView backArrow;
    private TextView titlePage , save;

    private ProgressBar progressBar;

    private MessageDialog messageDialog = new MessageDialog();
    private String userId;

    // Loginby phone number
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId, counter;
    private CountDownTimer  countDownTimer;

    boolean done = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_phonenumber , container , false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti() {
        initWidgets();
        onClickNewPage();
    }

    private void initWidgets() {
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.change_phone));

        save = view.findViewById(R.id.save);
        save.setText(mContext.getString(R.string.current_phone_authentication));
        save.setVisibility(View.VISIBLE);


        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void onClickNewPage() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButtons(false);
                loginPhone(view);


            }
        });

        view.findViewById(R.id.problem_auth_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
                Bundle bundle = new Bundle();
                bundle.putString("typeProblem", mContext.getString(R.string.change_auth_info_problems));
                fragmentInformProblem.setArguments(bundle);
                mOnActivityListener.onActivityListener_noStuck(fragmentInformProblem);
            }
        });

    }

    private void enableButtons(boolean boo){
        save.setVisibility(boo? View.VISIBLE:View.GONE);
        progressBar.setVisibility(boo? View.GONE:View.VISIBLE);
    }

    // loginEmail
    private void loginPhone(View _view){
        if(done){
            enableButtons(done);
            messageDialog.errorMessage(mContext, mContext.getString(R.string.phone_updated));

        }else {
            String phone = ((EditText)_view.findViewById(R.id.inputPhoneNumber)).getText().toString();
            String mCodePhoneNum = ((CountryCodePicker)_view.findViewById(R.id.codePhoneNumber)).getSelectedCountryCodeWithPlus();
            Log.d(TAG, "login: " + phone + " : " + mCodePhoneNum);

            final RelativeLayout relLayout_loginByPhone = _view.findViewById(R.id.relLayout_loginByPhone);
            final RelativeLayout relLayout_verificationCode = _view.findViewById(R.id.relLayout_verificationCode);
            final EditText mVerify_number = _view.findViewById(R.id.inputVerifyNumber);
            final AppCompatButton buttonVerifyNumber = _view.findViewById(R.id.buttonVerifyNumber);
            AppCompatButton buttonResendCheckCode = _view.findViewById(R.id.buttonResendCheckCode);


            if(countDownTimer != null ){
                countDownTimer.cancel();
            }
            if(relLayout_loginByPhone.getVisibility() == View.GONE){
                relLayout_verificationCode.setVisibility(View.GONE);
                relLayout_loginByPhone.setVisibility(View.VISIBLE);
                enableButtons(true);

            }else {
                final String phoneNumber = mCodePhoneNum + phone;

                buttonVerifyNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mVerificationId != null ){
                            Log.d(TAG, "onClick: mVerificationId :" + mVerificationId);
                            if (mVerify_number.getText().toString().length() < 2 ) {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_fill_all_blank));

                            }else {
                                enableButtons(false);

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
                            enableButtons(false);
                            resendVerificationCode(phoneNumber, mResendToken);
                            //counterResendCodeNumber();
                        }
                    }
                });

                if(!isStringNull(phone, "00000000")) {
                    enableButtons(false);

                    startPhoneNumberVerification(phoneNumber, relLayout_loginByPhone, relLayout_verificationCode);
                }
            }

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
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_phone_wrong));

                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_quota_exceeded_sms));

                }else{
                    messageDialog.errorMessage(mContext, e.getMessage());
                }

                enableButtons(true);
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

                enableButtons(true);
                counterResendCodeNumber(layout_code);
            }
        };
        // [END phone_auth_callbacks]
    }


    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential, final RelativeLayout layout_login,
                                               final RelativeLayout layout_code) {

        mAuth.getCurrentUser().updatePhoneNumber(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        enableButtons(true);

                        if (task.isSuccessful()) {// Sign in success

                            done = true;

                            layout_login.setVisibility(View.VISIBLE);
                            layout_code.setVisibility(View.GONE);

                            FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbname_users))
                                    .child(mAuth.getCurrentUser().getUid()).child(mContext.getString(R.string.field_phone_number))
                                    .setValue(mAuth.getCurrentUser().getPhoneNumber());

                            messageDialog.errorMessage(mContext, mContext.getString(R.string.phone_updated));


                        } else {
                            // Sign in failed
                            try
                            {
                                throw Objects.requireNonNull(task.getException());
                            }
                            catch (Exception e)
                            {
                                ExceptionsMessages.showExceptionMessage(mContext, e);
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
        if(string1 != null && string2 != null && string1.length()>1 && string2.length()>1){
            return false;

        }else {
            messageDialog.errorMessage(mContext, mContext.getString(R.string.error_fill_all_blank));
            return true;
        }
    }


    private void restartApp(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();
    }
}
