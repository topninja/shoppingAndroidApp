package com.entage.nrd.entage.createEntagePage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.AddingItemToAlgolia;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.EntagePageAccess;
import com.entage.nrd.entage.Models.EntagePageAdminData;
import com.entage.nrd.entage.Models.EntagePageSettings;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.Models.LogProcessCreateEntajiPage;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateEntagePageActivity extends AppCompatActivity implements OnActivityListener, CreateEntagePageListener {
    private static final String TAG = "CreateEntagePage";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, myRefLogs;
    private OnFailureListener onFailureListener;

    private DatabaseReference  ref_succeed, ref_error;
    private ValueEventListener listener_succeed, listener_error;

    private Context mContext;

    private RelativeLayout next;
    private TextView nextText;
    private ImageView back;

    private String userId;
    private ArrayList<String> listEntagePagesAccessIds;
    private EntagePage entagePage;

    private String newKey_entagePage;
    private EntagePageAdminData entagePageAdminData;
    private EntagePageAccess entagePageAccess;
    private EntagePageSettings entagePageSettings;
    private String userTokenId;

    private HashMap<String, Boolean> logs;
    private FragmentUploadCreateEntajiPage fragmentUploadCreateEntajiPage;
    private MessageDialog messageDialog = new MessageDialog();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entage_page);
        mContext =  CreateEntagePageActivity.this;

        //ColorTopMainBar.setColorTopMainBar(this);

        setupFirebaseAuth();
        inti();
    }

    private void inti() {
        next = this.findViewById(R.id.next_Rela);
        nextText = this.findViewById(R.id.next_text);
        back = this.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userId = mAuth.getCurrentUser().getUid();
        entagePage = new EntagePage();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, new CreateEntagePageFragment_1());
        transaction.commit();

        getListEntagePageAccess();

        logs = new HashMap<>();
        logs.put("setAdminData", false);
        logs.put("setEntagePagesSubscription", false);
        logs.put("setEntagePageAccess", false);
        logs.put("setEntagePage", false);
        logs.put("setEntagePageSettings", false);
        logs.put("setEntajiPageByCategories", false);
        logs.put("subscribeToTopic", false);
        logs.put("addToAlgoliaSearch", false);
    }

    @Override
    public RelativeLayout getNextButton() {
        return next;
    }

    @Override
    public TextView getNextText(){
        return nextText;
    }

    @Override
    public EntagePage getEntagePage() {
        return entagePage;
    }

    // ------ OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.left_to_right_start, R.anim.right_to_left_end);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commit();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.left_to_right_start, R.anim.right_to_left_end);
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
        transaction.commit();
    }

    @Override
    public void onGridImageSelected(ItemWithDataUser item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        onActivityListener(new ViewItemFragment(), bundle);
    }

    // DataBase
    private void getListEntagePageAccess(){
        listEntagePagesAccessIds = new ArrayList<>();

        Query query = myRef
                .child(getString(R.string.dbname_user_account_settings))
                .child(userId)
                .child(getString(R.string.field_entage_pages_access));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    listEntagePagesAccessIds = (ArrayList<String>) dataSnapshot.getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateEntagePageActivity.this, getString(R.string.error_internet),
                        Toast.LENGTH_LONG).show();
               //finish();
            }
        });

       myRef
                .child(getString(R.string.dbname_users_token))
                .child(userId)
               .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userTokenId = (String) dataSnapshot.getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateEntagePageActivity.this, getString(R.string.error_internet),
                        Toast.LENGTH_LONG).show();
                //finish();
            }
        });
    }

    @Override
    public void setDataToDataBase() {
        fragmentUploadCreateEntajiPage = new FragmentUploadCreateEntajiPage();
        onActivityListener(fragmentUploadCreateEntajiPage);
        findViewById(R.id.linLayout1).setVisibility(View.GONE);

        newKey_entagePage =  myRef.child(mContext.getString(R.string.dbname_entage_pages)).push().getKey(); // new key for entage page

        entagePageAdminData = new EntagePageAdminData();
        entagePageAdminData.setUser_id(userId);
        entagePageAdminData.setEntage_id(newKey_entagePage);
        entagePageAdminData.setDate_created(DateTime.getDateToday());
        entagePageAdminData.setPackage_entage_page("1_starter");
        entagePageAdminData.setEntage_page_number(ServerValue.TIMESTAMP);

        entagePageAccess = new EntagePageAccess();
        entagePageAccess.setUser_id(userId);
        entagePageAccess.setEntage_id(newKey_entagePage);
        entagePageAccess.setAdmin(true);
        entagePageAccess.setAuthorization_access(true);

        ArrayList<String> ids = new ArrayList<>();
        ids.add(userId);
        entagePage.setUsers_ids(ids);
        entagePage.setEntage_id(newKey_entagePage);
        entagePage.setEntage_page_number(entagePageAdminData.getEntage_page_number());

        entagePageSettings = new EntagePageSettings();
        entagePageSettings.setUser_id(userId);
        entagePageSettings.setEntage_id(newKey_entagePage);
        entagePageSettings.setCategories_number("1");

        listEntagePagesAccessIds.add(newKey_entagePage);

        if (newKey_entagePage != null) {

            createEntajiPage();

        }else {
            error(mContext.getString(R.string.happened_wrong_try_again));
        }

    }

    @Override
    public void show_hideBar(boolean show) {
        findViewById(R.id.linLayout1).setVisibility(show? View.VISIBLE:View.GONE);
    }

    private void createEntajiPage(){
        LogProcessCreateEntajiPage logProcessCreateEntajiPage = new LogProcessCreateEntajiPage();

        logProcessCreateEntajiPage.setEntagePageAdminData(entagePageAdminData);
        logProcessCreateEntajiPage.setEntagePageSettings(entagePageSettings);
        logProcessCreateEntajiPage.setEntagePage(entagePage);
        logProcessCreateEntajiPage.setEntagePageAccess(entagePageAccess);

        ArrayList<String> categories = entagePage.getCategories_entage_page();
        AddingItemToAlgolia itemToAlgolia = new AddingItemToAlgolia(entagePage.getName_entage_page(), entagePage.getEntage_id()
                , null,StringManipulation.getCategoriesForAlgolia(categories), entagePage.getEntage_id());
        // add main categories also
        for(String s : categories){
            ArrayList<String> arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(s);
            if(!itemToAlgolia.getCategorie_level_2().contains(arrayList.get(0))){
                itemToAlgolia.getCategorie_level_2().add(arrayList.get(0));
            }
        }
        logProcessCreateEntajiPage.setItemToAlgolia(itemToAlgolia.getItem());

        String title = NotificationsTitles.WelcomeEntajiPage(mContext);
        String body = NotificationsTitles.wishBst(mContext);
        NotificationOnApp notificationOnApp = new NotificationOnApp(newKey_entagePage,
                newKey_entagePage, null, null,
                title, body,
                mContext.getString(R.string.notif_flag_entaji_page), userId,
                null, null,
                DateTime.getTimestamp(),
                0,
                false);
        logProcessCreateEntajiPage.setNotificationOnApp(notificationOnApp);

        String topic2 = Topics.getTopicsAdminInEntagePage(newKey_entagePage);
        Notification notification = new Notification(newKey_entagePage,
                newKey_entagePage, "-1", topic2,
                title, body,
                mContext.getString(R.string.notif_flag_entaji_page), userId, "topic",
                "-1", "-1", entagePage.getEntage_id());
        logProcessCreateEntajiPage.setNotification(notification);

        // add new to db
        ArrayList<ArrayList<String>> mCategoriesForDb = new ArrayList<>();
        for(String path : categories) {
            ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
            mCategoriesForDb.add(cat);
           /* DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_entage_pages_by_categories));
            for(String catId : cat){
                databaseReference = databaseReference.child(catId);

                databaseReference.child("entage_page_id")
                        .child(entagePage.getEntage_id())
                        .setValue(entagePage.getEntage_id());
            }*/
        }

        logProcessCreateEntajiPage.setCategoriesForDb(mCategoriesForDb);

        logProcessCreateEntajiPage.setTokenId(userTokenId);

        myRef.child(mContext.getString(R.string.dbname_log_process_create_entaji_page))
                .child(userId)
                .child(newKey_entagePage)
                .setValue(logProcessCreateEntajiPage)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            startListen();
                        }else {
                            Log.d(TAG, "onComplete: " + task.getException().getMessage());
                            if(task.getException().getMessage().equals("Firebase Database error: Permission denied")){
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_title),
                                        mContext.getString(R.string.permission_denied));
                            }
                            else {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_title),
                                        mContext.getString(R.string.happened_wrong_try_again));
                            }
                            fragmentUploadCreateEntajiPage.showExit();
                        }
                    }
                });
    }

    private void startListen(){
        ref_succeed = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_log_process_create_entaji_page))
                .child(userId)
                .child(newKey_entagePage)
                .child("succeed");
        listener_succeed = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent intent = new Intent(CreateEntagePageActivity.this, EntageActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        ref_succeed.addValueEventListener(listener_succeed);

        ref_error = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_log_process_create_entaji_page))
                .child(userId)
                .child(newKey_entagePage)
                .child("error");
        listener_error = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String message = mContext.getString(R.string.happened_wrong_try_again);

                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_title),
                            mContext.getString(R.string.happened_wrong_try_again));
                    fragmentUploadCreateEntajiPage.showExit();

                    removeEventListener();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        ref_error.addValueEventListener(listener_error);
    }

    private void error(String mesg){
        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again)+", "+ mesg);
        fragmentUploadCreateEntajiPage.showExit();
    }

    private void removeEventListener(){
        if(ref_succeed != null){
            ref_succeed.removeEventListener(listener_succeed);
        }
        if(ref_error != null){
            ref_error.removeEventListener(listener_error);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeEventListener();
    }




    /*
-------------------------------Firebase-------------------------------------------------------
*/
    @SuppressLint("LongLogTag")
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

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
