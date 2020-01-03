package com.entage.nrd.entage.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class FragmentEditUsername extends Fragment {
    private static final String TAG = "FragmentEditUsername";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private OnActivityListener mOnActivityListener;

    private View view;
    private Context mContext;

    private ImageView backArrow ;
    private TextView  titlePage, savePersonalInfo ;

    private EditText mUsername, mNewUsername;

    private ProgressBar progressBar;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String phoneNumber;

    private long maxDifferenceDaysToChangeUsername = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_username, container , false);
        mContext = getActivity();

        inti();

        return view;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    @SuppressLint("LongLogTag")
    private void inti(){
        initWidgets();
        onClickListener();
    }

    private void initWidgets(){
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.change_username));

        savePersonalInfo = view.findViewById(R.id.save);
        savePersonalInfo.setVisibility(View.VISIBLE);

        mUsername = view.findViewById(R.id.userName);
        mNewUsername = view.findViewById(R.id.newUserName);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        //mUsername.setText(mOnActivityPageListener.getUserSetting().getUser().getUsername());
        mUsername.setEnabled(false);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        savePersonalInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //savePersonalInfo.setEnabled(false);
                /**
                 * 1- timeDifferenceForLastChange
                 * 2- checkNewUsername()
                 * 3- checkIfUserNameExist OR updateUsername
                 * 4- confirmPasswordDialogs.show
                 */
                //timeDifferenceForLastChange();
            }
        });

        savePersonalInfo.setEnabled(true);
    }
    
    @SuppressLint("LongLogTag")
    public void checkNewUsername() {
        Log.d(TAG, "updateInformation: ");

        //
        String _cureentUsername = StringManipulation.condenseUsername(mUsername.getText().toString());
        String _newUsername = StringManipulation.condenseUsername(mNewUsername.getText().toString());
        mNewUsername.setText(_newUsername);
        if ( _cureentUsername != _newUsername){

            if(_newUsername.length()>=3){
                progressBar.setVisibility(View.VISIBLE);


            }else {
                savePersonalInfo.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(mContext, mContext.getString(R.string.error_username_less_two),
                        Toast.LENGTH_SHORT).show();
            }

        }else {
            savePersonalInfo.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(mContext, mContext.getString(R.string.username_equal_new_username)
                    , Toast.LENGTH_LONG).show();
            Log.d(TAG, "updateInformation: NOthing change");
        }

    }

    //Check If EntageName Exist in database
    @SuppressLint("LongLogTag")
    private void checkIfUserNameExist(final String userName) {
        Log.d(TAG, "checkIfUserNameExist: ");

        myRef.child(mContext.getString(R.string.dbname_users))
                .orderByChild("username_lower_case")
                .equalTo(userName.toLowerCase())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    savePersonalInfo.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "checkIfEntageNameExist: FOUND MATCH " );
                    Toast.makeText(mContext, getString(R.string.error_name_token),
                            Toast.LENGTH_LONG).show();
                }else {


                    Log.d(TAG, "onDataChange: Signup successful.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                savePersonalInfo.setEnabled(true);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateUsername() {

        final String userId =  mAuth.getCurrentUser().getUid();
        final String cureentUsername = StringManipulation.condenseUsername(mUsername.getText().toString());
        final String newUsername = StringManipulation.condenseUsername(mNewUsername.getText().toString());

        //  checkIfUserNameExist
        String username = mNewUsername.getText().toString().toLowerCase();
        myRef.child(mContext.getString(R.string.dbname_users))
                .orderByChild("username_lower_case")
                .equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Log.d(TAG, "checkIfEntageNameExist: FOUND MATCH " );
                            savePersonalInfo.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(mContext, getString(R.string.error_name_token),
                                    Toast.LENGTH_LONG).show();
                        }else {
                            // update username
                            Log.d(TAG, "onDataChange: Signup successful.");
                            myRef.child(mContext.getString(R.string.dbname_users))
                                    .child(userId)
                                    .child( mContext.getString(R.string.field_username))
                                    .setValue(newUsername)
                                    .addOnCompleteListener( (Activity) mContext, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                // set in previous username
                                                /*ChangeUsername changeUsername = new ChangeUsername(userId, cureentUsername,
                                                        newUsername, DateTime.getTimestamp());
                                                int TimesChangeUsername = mOnActivityPageListener.getUserSetting().getUserAccountSettings().getTimes_change_username();
                                                String newKey = "";
                                                if (TimesChangeUsername == 0){ // This First Time To change
                                                    newKey = mOnActivityPageListener.newKey(mContext.getString(R.string.dbname_user_previous_username));
                                                }else { // This Not First Time To change
                                                    newKey = mOnActivityPageListener.newKey(mContext.getString(R.string.dbname_user_previous_username),
                                                            userId);
                                                }

                                                // update username in account_settings
                                                mOnActivityPageListener.setValue(mContext.getString(R.string.dbname_user_account_settings), userId,
                                                        mContext.getString(R.string.field_username),newUsername);

                                                //  ++ counter Change Username
                                                mOnActivityPageListener.setValue(mContext.getString(R.string.dbname_user_account_settings),
                                                        userId, mContext.getString(R.string.field_times_change_username), ++TimesChangeUsername);

                                                //  set in dbname user previous username
                                                mOnActivityPageListener.setValue(mContext.getString(R.string.dbname_user_previous_username),
                                                        userId, newKey, changeUsername);

                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(mContext, mContext.getString(R.string.successfully_save)
                                                        , Toast.LENGTH_LONG).show();
                                                confirmPasswordDialogs.dismiss();
                                                getActivity().finish();*/

                                            }else  {
                                                progressBar.setVisibility(View.GONE);
                                                Log.d(TAG, "onComplete: Exception:" +task.getException());
                                                Toast.makeText(mContext, mContext.getString(R.string.failed_save_)+", "+mContext.getString(R.string.error_internet)
                                                        , Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        savePersonalInfo.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void checkIfPhoneNumberExist() {

    }

    private void startPhoneNumberVerification(String phoneNumber) {

    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]


    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                getActivity(),               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void timeDifferenceForLastChange(){
        Log.d(TAG, "timeDifferenceForLastChange: ");
       /* progressBar.setVisibility(View.VISIBLE);
        String userId = mOnActivityPageListener.getAuth().getCurrentUser().getUid();
        final Date dateToday = DateTime.getDateToday();

        int TimesChangeUsername = mOnActivityPageListener.getUserSetting().getUserAccountSettings().getTimes_change_username();
        if (TimesChangeUsername == 0){
            checkNewUsername();
        }else {
            mOnActivityPageListener.getQuery(mContext.getString(R.string.dbname_user_previous_username), userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long deff = -1 ;
                            for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                                Date datePreviousChange = DateTime.getTimestamp(singleSnapshot.getValue(ChangeUsername.class).getDate());
                                long differenceDays = DateTime.getDifferenceDays(dateToday, datePreviousChange);

                                if (deff == -1){
                                    if (differenceDays >= deff){
                                        deff = differenceDays ;
                                    }
                                }else {
                                    if(differenceDays < deff){
                                        deff = differenceDays ;
                                    }
                                }

                            }

                            savePersonalInfo.setEnabled(true);
                            if (deff >= maxDifferenceDaysToChangeUsername){
                                checkNewUsername();
                            }else {
                                savePersonalInfo.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(mContext,
                                        mContext.getString(R.string.username_changed_recently)
                                        , Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            savePersonalInfo.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }*/
    }

    private void changeUsernameByEmail(String password){
        Log.d(TAG, "changeUsernameByEmail: ");


        progressBar.setVisibility(View.VISIBLE);
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        /*AuthCredential credential = EmailAuthProvider
                .getCredential(mOnActivityPageListener.getUserSetting().getUser().getEmail(), password);*/

        // Prompt the user to re-provide their sign-in credentials
       /* mOnActivityPageListener.getAuth().getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User re-authenticated.");
                            progressBar.setVisibility(View.GONE);
                            updateUsername();
                        }else {
                            savePersonalInfo.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(mContext, mContext.getString(R.string.password_wrong),
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });*/
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
}
