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
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.ChangeEmail;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.ExceptionsMessages;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class FragmentChangeUsername extends Fragment {
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
    private EditText newUsername;
    private String currentUsername;

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
        titlePage.setText(mContext.getString(R.string.change_username));

        save = view.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        newUsername = view.findViewById(R.id.current_email);
        newUsername.setHint(mContext.getString(R.string.hint_username));

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        view.findViewById(R.id.newEmail).setVisibility(View.GONE);
        view.findViewById(R.id.checkNewEmail).setVisibility(View.GONE);
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

        newUsername.setEnabled(boo);
    }

    private void timeDifferenceForLastChange(int timesChangingEmail, ChangeEmail lastChangeEmail){
        Log.d(TAG, "timeDifferenceForLastChange: ");

        enableButtons(true);

        String username = newUsername.getText().toString();
        username = StringManipulation.condenseUsername(StringManipulation.removeLastSpace(username.replace("\n","")));

        if(timesChangingEmail < 3){
            if(lastChangeEmail == null){
                if(checkUserName(username)){
                    enableButtons(false);
                    checkIfUserNameExist();
                }
            }else{
                //
                long diff = DateTime.getDifferenceDays(DateTime.getDateToday(), DateTime.getTimestamp(lastChangeEmail.getDate()));
                if(diff >= 1){
                    if(checkUserName(username)){
                        enableButtons(false);
                        checkIfUserNameExist();
                    }

                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.username_changed_recently));
                }
            }
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.timeout_changed_username));
        }

    }

    private boolean checkUserName(String string1){
        if(string1 != null && string1.length()>2 && string1.length()<20){
            if(!string1.toLowerCase().equals(currentUsername.toLowerCase())){
                return true;
            }else {
                messageDialog.errorMessage(mContext,mContext.getString(R.string.username_equal_new_username));
                return false;
            }

        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_username));
            return false;
        }
    }

    private void checkIfUserNameExist() {


        final String username = newUsername.getText().toString();
        Query query = myRef.child("usernames").child(username.toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){

                    updateEmailDb(username);


                }else {
                    enableButtons(true);
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_name_token));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                enableButtons(true);
                if(databaseError.getMessage().equals("Permission denied")){
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));

                }else{
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+", "+databaseError.getMessage());
                }
            }
        });
    }

    private void updateEmailDb(final String username) {

        // set in previous email
        ChangeEmail changeEmail = new ChangeEmail(userId, currentUsername,
                username.toLowerCase() , DateTime.getTimestamp());
        String newKey = myRef.child(mContext.getString(R.string.dbname_user_previous_username)).push().getKey();
        if (newKey != null) {
            myRef.child(mContext.getString(R.string.dbname_user_previous_username))
                    .child(userId)
                    .child(newKey)
                    .setValue(changeEmail);
        }

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userId)
                .child(mContext.getString(R.string.field_username))
                .setValue(username)

        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    myRef.child("usernames")
                            .child(currentUsername.toLowerCase())
                            .removeValue();

                    myRef.child("usernames")
                            .child(username.toLowerCase())
                            .setValue(userId);

                    final FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();

                    firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        enableButtons(true);
                                        messageDialog.errorMessage(mContext,mContext.getString(R.string.username_updated));
                                        newUsername.setText("");

                                    }else {
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
                }else {
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

    //
    private void checkingTimesChangingEmail(){
        Query query = myRef.child(mContext.getString(R.string.dbname_user_previous_username))
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
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+", "+databaseError.getMessage());
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
                    currentUsername = user.getDisplayName();

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
