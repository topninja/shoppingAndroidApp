package com.entage.nrd.entage.home;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.entage.nrd.entage.BuildConfig;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.Models.VersionApp;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.MyFirebaseMessagingService;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends AppCompatActivity implements  OnActivityListener {
    private static final String TAG = "HomeActivity";

    private Context myContext = HomeActivity.this;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isThereFragmentInStuck = false;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser firebaseUser;
    private String userId;

    private FragmentHomeContainer fragmentHomeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG ,"onCreate: starting.");

        //ColorTopMainBar.setColorTopMainBar(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initImageLoader();

        setupFirebaseAuth();

    }

    private void init() {
        fragmentHomeContainer = new FragmentHomeContainer();
        onActivityListener_noStuck(fragmentHomeContainer);
    }

    @Override
    public void onBackPressed() {
        if(!fragmentHomeContainer.isVisible()){
            super.onBackPressed();

        }else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;

            }else {
                this.doubleBackToExitPressedOnce = true;

                if(fragmentHomeContainer.isInHomeFragment()){
                    Toast.makeText(this,  getString(R.string.click_twice_to_exit) ,
                            Toast.LENGTH_SHORT).show();
                }else {
                    doubleBackToExitPressedOnce=false;
                }
            }

            //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    private void initImageLoader(){
        if (!ImageLoader.getInstance().isInited()){
            UniversalImageLoader universalImageLoader = new UniversalImageLoader(myContext);
            ImageLoader.getInstance().init(universalImageLoader.getConfig());
        }
    }

    // implements OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commit();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commit();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commit();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss(); // transaction.commit(); -->java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }

    @Override
    public void onGridImageSelected(ItemWithDataUser item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        onActivityListener(new ViewItemFragment(), bundle);
    }

    //Check Update
    private void checkUpdate() {
        Query query = mFirebaseDatabase.getReference()
                .child(getString(R.string.dbname_app_data))
                .child("version_app");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    VersionApp versionApp = dataSnapshot.getValue(VersionApp.class);

                    if(versionApp != null && BuildConfig.VERSION_CODE < versionApp.getVersion_code()){
                        if(!isDestroyed()){
                            View _view = getLayoutInflater().inflate(R.layout.dialog_new_update, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(myContext, R.style.AlertDialogBlue);
                            builder.setView(_view);
                            final AlertDialog alertSharing = builder.create();
                            alertSharing.setCancelable(true);
                            alertSharing.setCanceledOnTouchOutside(false);

                            if(versionApp.isForce_update()){
                                _view.findViewById(R.id.update).setVisibility(View.VISIBLE);
                                _view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        openAppOnGooglePlay();
                                    }
                                });
                                alertSharing.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        finish();
                                    }
                                });

                            }else {
                                ((LinearLayout)_view.findViewById(R.id.update_now).getParent()).setVisibility(View.VISIBLE);
                                _view.findViewById(R.id.update_now).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        openAppOnGooglePlay();
                                    }
                                });
                                _view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertSharing.dismiss();
                                    }
                                });
                                init();
                            }
                            alertSharing.show();
                        }
                    }else {
                        init();
                    }
                }else {
                    init();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                init();
            }
        });
    }

    private void openAppOnGooglePlay(){
        Uri uri = Uri.parse("market://details?id=" + myContext.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + myContext.getPackageName())));
        }
    }

    //Token id
    private void checkTokenId(){
        final String token_id_service = MyFirebaseMessagingService.getToken(this); // this token saved from MyFirebaseMessagingService class
        final String token_id = getSharedPreferences("token_id", MODE_PRIVATE).getString("token_id", null); // this token saved from this class
        Log.d(TAG, "checkTokenId: token_id_service: " +  token_id_service);
        Log.d(TAG, "checkTokenId: token_id: " + token_id);

        if(token_id_service != null){
            setTokenIdToDb(token_id_service);

        }
        else {
            if(token_id != null){
                setTokenIdToDb(token_id);

            }else {
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                String newToken = instanceIdResult.getToken();
                                getSharedPreferences("token_id", MODE_PRIVATE).edit().putString("token_id", newToken).apply();

                                setTokenIdToDb(newToken);
                            }
                        });
            }
        }
    }

    private void setTokenIdToDb(final String tokenId){
        mFirebaseDatabase.getReference().child("users_token")
                .child(userId)
                .setValue(tokenId);
    }

    //subscribe
    private void checkSubscribes(){
        Query query = mFirebaseDatabase.getReference()
                .child(getString(R.string.dbname_users_subscribes))
                .child(userId)
                .orderByValue()
                .equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        String topic = singleSnapshot.getKey();
                        subscribeToTopic(topic);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void subscribeToTopic(final String topic){
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mFirebaseDatabase.getReference().child(getString(R.string.dbname_users_subscribes))
                                    .child(userId)
                                    .child(topic)
                                    .setValue(true);
                        }
                    }
                });
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();

        checkUpdate();

        if(!firebaseUser.isAnonymous()){
            checkTokenId();
            checkSubscribes();
        }
    }

}
