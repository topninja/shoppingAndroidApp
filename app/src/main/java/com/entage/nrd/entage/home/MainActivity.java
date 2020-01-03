package com.entage.nrd.entage.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.entage.nrd.entage.BuildConfig;
import com.entage.nrd.entage.login.RegisterActivity;
import com.entage.nrd.entage.Models.Account;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.NetworkUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Context myContext = MainActivity.this;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference reference;

    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG ,"onCreate: starting.");

        //ColorTopMainBar.setColorTopMainBar(this);

        progressbar = findViewById(R.id.progressBar2);
        progressbar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_IN);
        progressbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_IN);


        this.findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });

        // setup layout direction for all layouts
        Locale localeNew = new Locale("ar");
        Locale.setDefault(localeNew);
        Resources your_resource = getResources();
        Configuration config = your_resource.getConfiguration();
        config.locale = localeNew;
        config.setLayoutDirection(localeNew);
        // END

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startConnection();
            }
        }, 1000);
        /*String topic2 = Topics.getTopicsAdminInEntagePage("-LYdt-CkPpdAlQ9cPign");
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_notifications))
                .child(getString(R.string.field_new_item))
                .child("-LYdt-CkPpdAlQ9cPign")
                .setValue(new Notification("-LYdt-CkPpdAlQ9cPign", "-LYdt-CkPpdAlQ9cPign",
                        topic2, "مرحبا بك في عالم انتاجي", "مع تمنياتنا  لك بالنوفيق",
                        "null"));*/
        //FirebaseUser user = FirebaseAuth.getInstance().user;
        //user.delete();

        /*String userId = "vKW8RWg6AzQ0SErHhEJeh3vHhzD2";
        String topic1 = Topics.getTopicsUsersEntagePage();
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users_notifications))
                .child(userId)
                .child(topic1)
                .child("-Lalpm8SSt9FPWdhdEil")
                .setValue(new SubscribeTopic(userId, topic1, true));*/

        /*String topic2 = Topics.getTopicsAdminInEntagePage("-Lalpm8SSt9FPWdhdEil");
        String topic3 = Topics.getTopicsCustomersInEntagePage("-Lalpm8SSt9FPWdhdEil");

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users_notifications))
                .child(userId)
                .child(Topics.getTopicsAdminInEntagePage(""))
                .child("-Lalpm8SSt9FPWdhdEil")
                .setValue(new SubscribeTopic(userId, topic2, true));

        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_users_notifications))
                .child(userId)
                .child(Topics.getTopicsCustomersInEntagePage(""))
                .child("-Lalpm8SSt9FPWdhdEil")
                .setValue(new SubscribeTopic(userId, topic3, true));*/


        //FirebaseMessaging.getInstance().unsubscribeFromTopic("users_entage_page");
        //String topic2 = Topics.getTopicsAdminInEntagePage("-LYdt-CkPpdAlQ9cPign");
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("customers_in_entage_page");
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("admin_users_entage_page");
        //Log.d(TAG, "onCreate: topic3: " + topic2);
        /*String topic3 = Topics.getTopicsCustomersInEntagePage("-LYdt-CkPpdAlQ9cPign");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("customers_in_entage_page-LYdt-CkPpdAlQ9cPign");*/

        /*String topic1 = Topics.getTopicsUsersEntagePage();
        FirebaseMessaging.getInstance().subscribeToTopic(topic1);

        String topic2 = Topics.getTopicsAdminInEntagePage("-La8vr7OddvbkomWh7i1");
        FirebaseMessaging.getInstance().subscribeToTopic(topic2);

        String topic3 = Topics.getTopicsCustomersInEntagePage("-La8vr7OddvbkomWh7i1");
        FirebaseMessaging.getInstance().subscribeToTopic(topic3);*/

    }

    private void startConnection(){
        progressbar.setVisibility(View.VISIBLE);
        this.findViewById(R.id.errorInternet).setVisibility(View.GONE);
        this.findViewById(R.id.retry).setVisibility(View.GONE);


        if(!NetworkUtils.isNetworkConnected(this)){
            progressbar.setVisibility(View.GONE);
            this.findViewById(R.id.errorInternet).setVisibility(View.VISIBLE);
            this.findViewById(R.id.retry).setVisibility(View.VISIBLE);

        }else {
            setupFirebaseAuth();
        }
    }

    /**
     * Check If First Run
     */
    private void checkUserAuth() {

        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            // This is just a normal run
            Log.d(TAG, "checkFirstRun: This is just a normal run ");
            normalRun();

            return;

        }
        else if (savedVersionCode == DOESNT_EXIST) {
            // TODO This is a new install (or the user cleared the shared preferences)
            Log.d(TAG, "checkFirstRun: TODO This is a new install (or the user cleared the shared preferences)");
            clearSharedPreferences();

            if(mAuth.getCurrentUser() == null ){
                createAnonymousAccount(); // create new Anonymous Account

            }
            else if(mAuth.getCurrentUser().isAnonymous()){
                    goToHomeActivity();

            }
            else {
                mAuth.signOut();
                createAnonymousAccount(); // create new Anonymous Account
            }

        }
        else if (currentVersionCode > savedVersionCode) {
            // TODO This is an upgrade
            Log.d(TAG, "checkFirstRun: TODO This is an upgrade");
            clearSharedPreferences();

            normalRun();
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }

    private void normalRun(){
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            createAnonymousAccount();

        }else {
            if (user.isAnonymous()) {
                Log.d(TAG, "SignIn : isAnonymous: " + user.getUid());
                goToHomeActivity();

            }else {
                Log.d(TAG, "SignIn : Uid:  " + user.getUid());
                checkVerified(user);//checkTokenId(); // --> checkIfFirstLogin();
            }
        }
    }

    private void checkVerified(FirebaseUser user){
        Log.d(TAG, "checkIfFirstLogin: " + user.getDisplayName());
        if(user.getEmail() != null && user.getEmail().length() > 0 && !user.isEmailVerified()){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();

        }
        else {
            // check user register all required data
            if(user.getDisplayName() == null || user.getDisplayName().length() == 0){
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.putExtra("register_required_data", true);
                startActivity(intent);
                finish();

            }else {
                goToHomeActivity();
            }
        }
    }

    private void createAnonymousAccount(){
        Log.d(TAG, "create Anonymous Account");
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    String uid = user.getUid();
                    Log.d(TAG, "signInAnonymously: success with Uid: " + uid);

                    reference.child("anonymous_account")
                            .child(uid)
                            .setValue(new Account(uid, DateTime.getTimestamp(), getDeviceName(), BuildConfig.VERSION_CODE,
                                    BuildConfig.VERSION_NAME));

                    goToHomeActivity();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.getException());
                    findViewById(R.id.errorInternet).setVisibility(View.VISIBLE);
                    Toast.makeText(myContext, getString(R.string.error_internet), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goToHomeActivity(){
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void clearSharedPreferences(){
        SharedPreferences sharedPrefs = getSharedPreferences("entaji_app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.apply();
    }

    //
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    /**
     * SetUP Firebase Auth
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        //mAuth.signOut();
        checkUserAuth();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
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
        if(mAuth!=null){
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if( mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
