package com.entage.nrd.entage.entage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.Models.LogProcessCreateEntajiPage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.createEntagePage.NoEntagePageFragment;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EntageActivity extends AppCompatActivity implements OnActivityListener{
    private static final String TAG = "EntageActivity";


    //firebase
    private DatabaseReference myRef;
    private String userId;

    private Context myContext ;
    public static boolean active = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout); // activity_frame_layout
        myContext = EntageActivity.this;
        Log.d(TAG, "OnCreat: started.");


        setupFirebaseAuth();
        inti();

    }

    private void inti(){
        //ColorTopMainBar.setColorTopMainBar(this);

        initImageLoader();

        onActivityListener_noStuck(new WaitFragment());

        getListOfEntagePageAccess();
        //userDoesntHasEntagePage();
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(myContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getListOfEntagePageAccess() {

       // entage_pages_access
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_entage_pages_access))
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        ArrayList<String> entajiPagesIds = new ArrayList<>();

                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            entajiPagesIds.add(singleSnapshot.getKey());
                        }

                        getSharedPreferences("entaji_app",
                                MODE_PRIVATE).edit().putStringSet("entagePages_list", new HashSet<>(entajiPagesIds)).apply();

                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList( "entajiPagesIds" , entajiPagesIds);
                        onActivityListener_noStuck(new EntageFragment() , bundle);

                    }else {
                        userDoesntHasEntagePage();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(myContext, getString(R.string.error_internet),
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    }

    private void userDoesntHasEntagePage(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, new NoEntagePageFragment());
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition( R.anim.right_to_left_start, R.anim.left_to_right_end);
    }

    // ** OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss();
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


    /*
        -------------------------------Firebase-------------------------------------------------------
      */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
       //myRef = FirebaseDatabase.getInstance().getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }


}
