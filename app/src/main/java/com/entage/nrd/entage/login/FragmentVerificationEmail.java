package com.entage.nrd.entage.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.personal.FragmentChangeEmail;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CustomListView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FragmentVerificationEmail extends Fragment {
    private static final String TAG = "FragmentVerificationEma";

    private View view;
    private Context mContext;
    private OnActivityListener mOnActivityListener;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText email;
    private AppCompatButton butResendVerification, butChecking, signOut;
    private ProgressBar butProgressBar;
    private String counter;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_verification_email, container , false);
        mContext = getActivity();

        init();
        setupFirebaseAuth();

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

    private void init() {
        initWidgets();
        onClickListener();

        setupAdapter();
    }

    private void initWidgets() {
        email =  view.findViewById(R.id.mEmail_notVerified);
        butResendVerification =  view.findViewById(R.id.buttonResendCheckCode);
        signOut =  view.findViewById(R.id.sign_out);
        butProgressBar =  view.findViewById(R.id.ProgressBar_resendCode);
        butChecking =  view.findViewById(R.id.buttonChecking);
    }

    private void onClickListener() {
        butResendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mAuth.getCurrentUser().isEmailVerified()){
                    sendVerificationEmail(mAuth.getCurrentUser());

                }else {
                   restartApp();
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut.setVisibility(View.INVISIBLE);
                mAuth.signOut();

                new CountDownTimer(2000, 1000) {
                    public void onTick(long millisUntilFinished) { }
                    public void onFinish() {
                        restartApp();
                    }
                }.start();
            }
        });

        butChecking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                butChecking.setEnabled(false);
                butChecking.setVisibility(View.INVISIBLE);
                view.findViewById(R.id.mesg).setVisibility(View.GONE);

                //
                butResendVerification.setEnabled(false);
                butResendVerification.setVisibility(View.INVISIBLE);
                butProgressBar.setVisibility(View.VISIBLE);

                FirebaseAuth.getInstance().getCurrentUser().reload();

                new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) { }
                    public void onFinish() {
                        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                            // some time come here from  after changing email, be sure email change in users database
                            updateEmailDb(FirebaseAuth.getInstance().getCurrentUser());

                        }else {
                            view.findViewById(R.id.mesg).setVisibility(View.VISIBLE);
                            ((TextView)view.findViewById(R.id.mesg)).setText(mContext.getString(R.string.verification_not_done_yet));
                            butChecking.setVisibility(View.VISIBLE);
                            butChecking.setEnabled(true);

                            //
                            butResendVerification.setEnabled(true);
                            butResendVerification.setVisibility(View.VISIBLE);
                            butProgressBar.setVisibility(View.GONE);
                        }
                    }
                }.start();


            }
        });
    }

    private void setupAdapter() {
        CustomListView listView = view.findViewById(R.id.listView);

        final String delete = mContext.getString(R.string.delete_this_account)+" "+email.getText().toString();

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(mContext.getString(R.string.change_email));
        arrayList.add(delete);
        arrayList.add(mContext.getString(R.string.inform_problem_in_sigin));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                mContext, android.R.layout.simple_list_item_1, arrayList){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.BLUE);

                return view;
            }
        };

        ArrayAdapter<String> adapterListView = new ArrayAdapter<>(mContext,  android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);
                if(value.equals(mContext.getString(R.string.change_email))){
                    mOnActivityListener.onActivityListener(new FragmentChangeEmail());

                }else if(value.equals(delete)){
                    Toast.makeText(mContext, "This Operation not work yet.", Toast.LENGTH_LONG).show();
                    //deleteAccount();

                }else if(value.equals(mContext.getString(R.string.inform_problem_in_sigin))){
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.login_problems));
                    FragmentInformProblem fragment = new FragmentInformProblem();
                    fragment.setArguments(bundle);
                    mOnActivityListener.onActivityListener(fragment);

                }
            }
        });


    }

    private void deleteAccount (){
        // unsubscribe from all topics if there

        // check if has entage page

        // check if has orders page
    }

    private void sendVerificationEmail(FirebaseUser firebaseUser){
        Log.d(TAG, "sendVerificationEmail: userUid: " + firebaseUser.getUid());
        view.findViewById(R.id.mesg).setVisibility(View.GONE);
        butResendVerification.setEnabled(false);
        butResendVerification.setVisibility(View.INVISIBLE);
        butProgressBar.setVisibility(View.VISIBLE);

        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        butResendVerification.setVisibility(View.VISIBLE);
                        butProgressBar.setVisibility(View.GONE);

                        view.findViewById(R.id.mesg).setVisibility(View.VISIBLE);
                        if(task.isSuccessful()){

                            ((TextView)view.findViewById(R.id.mesg)).setText(mContext.getString(R.string.signup_successful_sending_verification_emil));
                            counterResendCodeNumber();

                        }else{
                            ((TextView)view.findViewById(R.id.mesg)).setText(mContext.getString(R.string.couldnt_send_verification_email));
                            butResendVerification.setEnabled(true);
                        }
                    }
                });
    }

    private void counterResendCodeNumber() {
        butResendVerification.setText("");
        counter = "";

        new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                //counter = ""+(millisUntilFinished / 1000);
                counter = String.format(Locale.getDefault(), "%d "+
                        mContext.getString(R.string.minutes)+ " " + mContext.getString(R.string.and_text)+
                                " %d "+ mContext.getString(R.string.seconds),
                        TimeUnit.MILLISECONDS.toMinutes( millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                butResendVerification.setText(counter);


            }
            public void onFinish() {
                butResendVerification.setText(mContext.getString(R.string.resend_check_verify_link));
                butResendVerification.setEnabled(true);
            }
        }.start();
    }

    private void updateEmailDb(final FirebaseUser firebaseUser) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(firebaseUser.getUid())
                .child(mContext.getString(R.string.field_email))
                .setValue(firebaseUser.getEmail());

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(firebaseUser.getUid())
                .child(mContext.getString(R.string.field_email))
                .setValue(firebaseUser.getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        restartApp();
                    }
                });
    }

    //
    private void restartApp(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


         /*
    -------------------------------Firebase-------------------------------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && !user.isAnonymous()) {
                    Log.d(TAG, "SignIn : Uid. : " + user.getUid());
                    email.setText(user.getEmail());

                } else {
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
