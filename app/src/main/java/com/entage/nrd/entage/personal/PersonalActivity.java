package com.entage.nrd.entage.personal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import com.entage.nrd.entage.home.FragmentLogin;
import com.entage.nrd.entage.home.FragmentPersonal;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.Models.UserSettings;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PersonalActivity extends AppCompatActivity  {
    private static final String TAG = "PersonalActivity";

    private Context mContext ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        mContext = PersonalActivity.this;

        //ColorTopMainBar.setColorTopMainBar(this);
        init();
    }

    private void init() {
        Log.d(TAG, "init: ");
        setupFirebaseAuth();
        initImageLoader();
    }

    private void initImageLoader(){
        if (!ImageLoader.getInstance().isInited()){
            UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
            ImageLoader.getInstance().init(universalImageLoader.getConfig());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PersonalActivity.this.overridePendingTransition( R.anim.right_to_left_start, R.anim.left_to_right_end);
    }

    /*
-------------------------------Firebase-------------------------------------------------------
 */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Intent intent = getIntent();
            if(intent.hasExtra(getString(R.string.calling_value))){

            }else {
                if (user.isAnonymous()) {
                    FragmentLogin fragmentLogin = new FragmentLogin();
                    FragmentTransaction transaction = PersonalActivity.this.getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isComeFromPersonalActivity", true);
                    fragmentLogin.setArguments(bundle);
                    transaction.replace(R.id.containerEntage, fragmentLogin);
                    transaction.commit();

                }else {
                    FragmentPersonal personalFragment = new FragmentPersonal();
                    FragmentTransaction transaction = PersonalActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.containerEntage, personalFragment);
                    transaction.commit();
                }
            }
        }else {
            Log.d(TAG, "restartApp: ");
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }
    }

}
