package com.entage.nrd.entage.personal;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;

import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.Models.ChangeEmail;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.ExceptionsMessages;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class FragmentChangeEmail extends Fragment {
    private static final String TAG = "FragmentChangeEmail";

    private Context mContext;
    private View view;
    private OnActivityListener mOnActivityListener;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ImageView backArrow;
    private TextView titlePage , save;
    private EditText currentEmail, newEmail, newEmail_2;

    private ProgressBar progressBar;

    private MessageDialog messageDialog = new MessageDialog();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_email , container , false);
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
        titlePage.setText(mContext.getString(R.string.change_email));

        save = view.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        currentEmail = view.findViewById(R.id.current_email);
        newEmail = view.findViewById(R.id.newEmail);
        newEmail_2 = view.findViewById(R.id.checkNewEmail);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        currentEmail.setEnabled(false);

        view.findViewById(R.id.relLayoutPhone).setVisibility(View.GONE);
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
                checkingTimesChangingEmail();
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

        newEmail.setEnabled(boo);
        newEmail_2.setEnabled(boo);
    }

    private void timeDifferenceForLastChange(int timesChangingEmail, ChangeEmail lastChangeEmail){
        Log.d(TAG, "timeDifferenceForLastChange: ");

        enableButtons(true);

        if(timesChangingEmail < 3){
            if(lastChangeEmail == null){
                if(checkingEmails(newEmail.getText().toString() , newEmail_2.getText().toString())){
                    getPassword();
                }

            }else{
                //
                long diff = DateTime.getDifferenceDays(DateTime.getDateToday(), DateTime.getTimestamp(lastChangeEmail.getDate()));
                if(diff >= 1){
                    getPassword();

                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.email_changed_recently));
                }
            }
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.timeout_changed_email));
        }

    }

    private boolean checkingEmails(String newEmail_1, String newEmail_2){
        if(newEmail_1 != null && newEmail_2 != null && newEmail_1.length() > 5 && newEmail_2.length() > 5 ){
            if(newEmail_2.equals(newEmail_1)){
                if(!newEmail_1.equals(currentEmail.getText().toString())){
                    return true;
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.new_email_eqaule_current_email));
                    return false;
                }

            }else {
                messageDialog.errorMessage(mContext,mContext.getString(R.string.email_not_match));
                return false;
            }

        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_fill_all_blank));
            return false;
        }
    }

    private void getPassword(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        final EditText edit_text = _view.findViewById(R.id.inputPassword);
        _view.findViewById(R.id.text_input2).setVisibility(View.VISIBLE);
        TextView resetPassword = _view.findViewById(R.id.text_view5);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resetPassword.setText(Html.fromHtml("<p><u>"+mContext.getString(R.string.forgot_password)+"</u></p>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            resetPassword.setText(Html.fromHtml("<p><u>"+mContext.getString(R.string.forgot_password)+"</u></p>"));
        }
        //resetPassword.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setTitle(mContext.getString(R.string.hint_password));

        builder.setPositiveButton(mContext.getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                checkPassword(edit_text.getText().toString());
            }
        });

        builder.setNeutralButton(mContext.getString(R.string.forgot_password), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                forgetPassword();
            }
        });

        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void forgetPassword(){

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        ((TextView) _view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.send_password_reset_1)+
                "\n"+ mAuth.getCurrentUser().getEmail()+ "\n" + mContext.getString(R.string.send_password_reset_2));
        TextView resetPassword = _view.findViewById(R.id.text_view5);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resetPassword.setText(Html.fromHtml("<p><u>"+mContext.getString(R.string.inform_problem_in_sigin)+"</u></p>", Html.FROM_HTML_MODE_COMPACT));
        } else {
            resetPassword.setText(Html.fromHtml("<p><u>"+mContext.getString(R.string.inform_problem_in_sigin)+"</u></p>"));
        }
        //resetPassword.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setTitle(mContext.getString(R.string.send_password_reset));

        builder.setPositiveButton(mContext.getString(R.string.send_password_reset_link), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                FirebaseAuth.getInstance().sendPasswordResetEmail(mAuth.getCurrentUser().getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.successfully_send_password_reset_link));
                                }else {
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
        });

        builder.setNeutralButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.setNegativeButton(mContext.getString(R.string.inform_problem_in_sigin), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkPassword(String password){

        if(password == null || password.length() == 0){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.password_wrong));

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
                                updateEmailAuth();

                            } else {
                                enableButtons(true);
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
    }

    private void updateEmailAuth(){
        ////////////////////// update email
        mAuth.getCurrentUser().updateEmail(newEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");

                            messageDialog.errorMessage(mContext, mContext.getString(R.string.email_updated)
                            +"\n"+mContext.getString(R.string.message_send_check_verify_link));

                            ///
                            updateEmailDb(FirebaseAuth.getInstance().getCurrentUser());

                        }else {
                            try
                            {
                                //setErrorMessage(task.getException().getMessage());
                                throw Objects.requireNonNull(task.getException());
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthWeakPasswordException weakPassword)
                            {
                                messageDialog.errorMessage(mContext, task.getException().getMessage());
                            }
                            // if email already exist.
                            catch (FirebaseAuthUserCollisionException existEmail)
                            {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_email_token));
                            }
                            // if user enters wrong email.
                            catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                            {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_email_wrong));
                            }
                            catch (Exception e)
                            {
                                messageDialog.errorMessage(mContext, task.getException().getMessage());
                            }
                            enableButtons(true);
                        }
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser){
        Log.d(TAG, "sendVerificationEmail: userUid: " + firebaseUser.getUid());
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseAuth.getInstance().getCurrentUser().reload();
                        new CountDownTimer(2000, 1000) {
                            public void onTick(long millisUntilFinished) { }
                            public void onFinish() {
                                restartApp();
                            }
                        }.start();
                    }
                });
    }

    private void updateEmailDb(final FirebaseUser firebaseUser) {
        // set in previous email
        ChangeEmail changeEmail = new ChangeEmail(userId, currentEmail.getText().toString(),
                firebaseUser.getEmail() , DateTime.getTimestamp() );
        String newKey = myRef.child(mContext.getString(R.string.dbname_user_previous_email)).push().getKey();
        if (newKey != null) {
            myRef.child(mContext.getString(R.string.dbname_user_previous_email))
                    .child(userId)
                    .child(newKey)
                    .setValue(changeEmail);
        }
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_email))
                .setValue(firebaseUser.getEmail());

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_email))
                .setValue(firebaseUser.getEmail())
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sendVerificationEmail(firebaseUser);
            }
        });
    }

    //
    private void restartApp(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //
    private void checkingTimesChangingEmail(){
        Query query = myRef.child(mContext.getString(R.string.dbname_user_previous_email))
                .child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChangeEmail lastChangeEmail = null;
                int timesChangingEmail = 0;
                if(dataSnapshot.exists()){
                    timesChangingEmail = (int) dataSnapshot.getChildrenCount();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        lastChangeEmail = snapshot.getValue(ChangeEmail.class);
                    }
                }

                timeDifferenceForLastChange(timesChangingEmail, lastChangeEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                enableButtons(true);
                if(databaseError.getMessage().equals("Permission denied")){
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));

                }else{
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_internet));
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
                    currentEmail.setText(user.getEmail());

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
