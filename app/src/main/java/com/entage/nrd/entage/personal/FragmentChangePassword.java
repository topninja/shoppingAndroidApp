package com.entage.nrd.entage.personal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FragmentChangePassword extends Fragment {
    private static final String TAG = "FragmentChangePassword";

    private Context mContext;
    private View view;
    private OnActivityListener mOnActivityListener;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ImageView backArrow;
    private TextView titlePage , save;
    private EditText currentPassword, newPassword, newPassword_2;
    private ProgressBar progressBar;
    private AlertDialog alert;

    private MessageDialog messageDialog = new MessageDialog();
    private String userId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_auth_password , container , false);
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
        titlePage.setText(mContext.getString(R.string.change_password));

        save = view.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        currentPassword = view.findViewById(R.id.inputCurrentPassword);
        newPassword = view.findViewById(R.id.inputNewPassword);
        newPassword_2 = view.findViewById(R.id.inputCheckNewPassword);

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

                if(checkingEmails(newPassword.getText().toString() , newPassword_2.getText().toString())){
                    checkPassword(currentPassword.getText().toString());
                }
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

    private boolean checkingEmails(String newEmail_1, String newEmail_2){
        if(newEmail_1 != null && newEmail_2 != null){
            if(newEmail_2.equals(newEmail_1)){
                if(!newEmail_1.equals(currentPassword.getText().toString())){
                    return true;
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.new_password_eqaule_current_password));
                    return false;
                }

            }else {
                messageDialog.errorMessage(mContext,mContext.getString(R.string.error_password_not_match));
                return false;
            }

        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_fill_all_blank));
            return false;
        }
    }

    private void enableButtons(boolean boo){
        save.setVisibility(boo? View.VISIBLE:View.GONE);
        progressBar.setVisibility(boo? View.GONE:View.VISIBLE);

        newPassword.setEnabled(boo);
        newPassword_2.setEnabled(boo);
    }

    private void checkPassword(String password){

        if(password == null || password.length() == 0){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_fill_all_blank));

        }else {
            enableButtons(false);

            // Get auth credentials from the user for re-authentication. The example below shows
            // email and password credentials but there are multiple possible providers,
            // such as GoogleAuthProvider or FacebookAuthProvider.
            AuthCredential credential = EmailAuthProvider
                    .getCredential(mAuth.getCurrentUser().getEmail(), password);

            ///////////////////// Prompt the user to re-provide their sign-in credentials
            mAuth.getCurrentUser().reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                updatePassword();


                            } else {
                                enableButtons(true);
                                if(task.getException() != null &&
                                        task.getException().getMessage().equals("The user account has been disabled by an administrator.")){
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.user_account_has_been_disabled));

                                }else if(task.getException() != null &&
                                        task.getException().getMessage().equals("The email address is badly formatted.")){
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.email_address_badly_formatted));

                                }else {
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.password_wrong)+", "
                                    + task.getException().getMessage());
                                }
                            }
                        }
                    });
        }
    }

    private void updatePassword(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseUser.updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                enableButtons(true);

                if(task.isSuccessful()){ // Snackbar
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.password_successfully_change));

                }else {
                    try
                    {
                        //setErrorMessage(task.getException().getMessage());
                        throw Objects.requireNonNull(task.getException());
                    }
                    // if user enters wrong password.
                    catch (FirebaseAuthWeakPasswordException weakPassword)
                    {
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_password_weak));
                    }
                    catch (Exception e)
                    {
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again)+ ", "+ e.getMessage());
                    }
                }
            }
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isAnonymous()) {
                    Log.d(TAG, "SignIn : Uid. : " + user.getUid());
                    userId = user.getUid();

                } else {
                    getActivity().onBackPressed();
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
        if (mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
