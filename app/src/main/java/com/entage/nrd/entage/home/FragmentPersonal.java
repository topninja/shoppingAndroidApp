package com.entage.nrd.entage.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.entage.nrd.entage.BuildConfig;
import com.entage.nrd.entage.Models.LocationInformation;
import com.entage.nrd.entage.Models.UserAccountSettings;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.SettingApp.SettingAppActivity;
import com.entage.nrd.entage.utilities_1.CustomListView;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class FragmentPersonal extends Fragment {
    private static final String TAG = "FragmentPersonal";

    private FirebaseDatabase mFirebaseDatabase;
    private String userId;

    private View view;
    private Context mContext;
    private ViewPager viewPager;

    private TextView username_text, email_text, location, language, currency, followers, privacyPolicy, termsAndConditions, textVersion;
    private ImageView optionSearch;
    private CustomListView listView;
    private InputMethodManager imm;
    private RelativeLayout layout_location, layout_language, layout_currency, layout_followers;
    private ArrayList<String> arrayList;

    private ArrayAdapter<String> adapterListView;
    private int mFollowingCount;
    private UserAccountSettings userAccountSettings;

    private AlertDialog alertSignOut;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        view = inflater.inflate(R.layout.fragment_personal, container , false);
        mContext = getActivity();
        viewPager = (ViewPager) container;

        setupFirebaseAuth();
        inti();

        return view;
    }


    private void inti(){
        initWidgets();

        setupListView();
        checkingAdminUser();

        if(userAccountSettings == null){
            getFollowingCount();
            fetchPersonalInfo();
        }else {
            setDataInWidgets(userAccountSettings);
        }
    }

    private void initWidgets() {
        username_text = view.findViewById(R.id.username);
        email_text = view.findViewById(R.id.userAuth);
        privacyPolicy = view.findViewById(R.id.privacy_policy);
        termsAndConditions = view.findViewById(R.id.terms_and_Conditions);

        location = view.findViewById(R.id.location);
        language = view.findViewById(R.id.language);
        currency = view.findViewById(R.id.currency);
        layout_location = view.findViewById(R.id.layout_location);
        layout_language = view.findViewById(R.id.layout_language);
        layout_currency = view.findViewById(R.id.layout_currency);

        followers = view.findViewById(R.id.followers);
        layout_followers = view.findViewById(R.id.layout_followers);

        listView = view.findViewById(R.id.listView);
        optionSearch = view.findViewById(R.id.options_search);
        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        textVersion = view.findViewById(R.id.textVersion);
        textVersion.setText(BuildConfig.VERSION_NAME);
    }

    private void onClickListener() {
        optionSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1, true);
            }
        });

        layout_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(mContext.getString(R.string.personal_information));
            }
        });
        layout_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(mContext.getString(R.string.personal_information));
            }
        });
        layout_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(mContext.getString(R.string.personal_information));
            }
        });
        layout_followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(mContext.getString(R.string.dbname_following_user));
            }
        });
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(mContext.getString(R.string.privacy_policy_text));
            }
        });
        termsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(mContext.getString(R.string.term_text));
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                String value = (String)adapter.getItemAtPosition(position);
                if(value.equals(mContext.getString(R.string.sign_out))){
                    dialogSignOut();

                }
                else if(value.equals(mContext.getString(R.string.ratingUs))){
                    ratingApp();

                }

               /* else if(value.equals(mContext.getString(R.string.settingApp))){
                    activitySettingApp();

                }*/
                else {
                    openFragment(value);
                }
            }
        });
    }

    private void setupListView() {
        arrayList = new ArrayList<>();
        arrayList.add(mContext.getString(R.string.personal_information));
        arrayList.add(mContext.getString(R.string.my_addresses));
        arrayList.add(mContext.getString(R.string.my_wallet));
        //arrayList.add(mContext.getString(R.string.myCards));
        arrayList.add(mContext.getString(R.string.myWishList));
        arrayList.add(mContext.getString(R.string.myPreferences));
        arrayList.add(mContext.getString(R.string.ratingUs));
        arrayList.add(mContext.getString(R.string.contactUs));
        arrayList.add(mContext.getString(R.string.what_entaji_app));
        arrayList.add(mContext.getString(R.string.sign_out));

        adapterListView = new ArrayAdapter<String>(mContext,  android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapterListView);
    }

    private void openFragment(String fragment){
        if (fragment.equals(getString(R.string.myCards))){

        }else {
            // first open activity then open fragment
            Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
            intent.putExtra("notification_flag", fragment);
            startActivity(intent);
        }
    }

    private void activitySettingApp(){
        // first open activity then open fragment
        Intent intent = new Intent(mContext, SettingAppActivity.class);
        startActivity(intent);
    }

    private void ratingApp(){
        Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
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
                    Uri.parse("http://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
        }
    }

    private void getFollowingCount(){
        mFollowingCount = 0;

        Query query =  mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_following_user))
                .child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    mFollowingCount++;
                }
                followers.setText(String.valueOf(mFollowingCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkingAdminUser(){
      /* Query query = mFirebaseDatabase.getReference()
                .child("admin_id")
                .child(userId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    arrayList.add("اعدادات التطبيق");
                    adapterListView.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });*/
    }

    //
    private void dialogSignOut(){
        final View _view = this.getLayoutInflater().inflate(R.layout.dialog_sign_out, null);
        final TextView signOut = _view.findViewById(R.id.unfollow);
        final TextView cancel = _view.findViewById(R.id.cancel);
        final ProgressBar _progressBar = _view.findViewById(R.id.progressBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setView(_view);
        alertSignOut = builder.create();
        alertSignOut.setCancelable(false);
        alertSignOut.setCanceledOnTouchOutside(false);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)_view.findViewById(R.id.message)).setText(mContext.getString(R.string.waite_to_sign_out));
                _progressBar.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                signOut.setVisibility(View.GONE);

                unsubscribeFromAllTopics();

                //alert.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSignOut.dismiss();
            }
        });

        alertSignOut.show();
    }

    private void unsubscribeFromAllTopics(){
        final Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_subscribes))
                .child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int count = (int) dataSnapshot.getChildrenCount();
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        count--;
                       String topic = singleSnapshot.getKey();
                       if(count == 0){
                           unsubscribeFromTopic(topic, true);
                       }else {
                           unsubscribeFromTopic(topic, false);
                       }
                    }

                }else {
                    sign_out();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                alertSignOut.dismiss();
                Toast.makeText(mContext, mContext.getString(R.string.error),
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void unsubscribeFromTopic(final String topic, final boolean isLast){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_subscribes))
                                    .child(userId)
                                    .child(topic)
                                    .setValue(false);
                        }

                        if(isLast){
                            sign_out();
                        }
                    }
                });
    }

    private void sign_out(){
        ((GlobalVariable)mContext.getApplicationContext()).clear();

        mFirebaseDatabase.getReference()
                .child("users_token")
                .child(userId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseAuth.getInstance().signOut();

                        alertSignOut.dismiss();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
    }
    //


    private void setDataInWidgets(UserAccountSettings user){

        String s = "@"+user.getUsername();
        username_text.setText( s +" | "+ user.getFirst_name()+" "+user.getLast_name());

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser.getEmail() != null && firebaseUser.getEmail().length() > 0){
            email_text.setText(firebaseUser.getEmail());
        }else {
            email_text.setText(firebaseUser.getPhoneNumber());
        }

        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        //Log.d(TAG, "setDataInWidgets: " + user.getLocation_information().toString());
        if(user.getLocation_information()==null){
            globalVariable.setLocationInformation(new LocationInformation());

        }else {
            globalVariable.setLocationInformation(user.getLocation_information());

            String userCountryCode = user.getLocation_information().getCountry_code();

            location.setText(userCountryCode+", "+user.getLocation_information().getCity_name());

            Currency curr = null;
            try {
                curr = Currency.getInstance(user.getCurrency());
            }catch (Exception ignored){}

            if(curr != null){
                currency.setText(curr.getCurrencyCode()+" "+curr.getDisplayName());
                globalVariable.setCurrency(curr);
            }

            globalVariable.setLanguage(user.getLanguage());
            String lan = Locale.getDefault().getDisplayLanguage(new Locale(Locale.getDefault().getLanguage(), userCountryCode));
            if(lan != null){
                language.setText(lan);
            }else {
                language.setText(user.getLanguage());
            }
        }

        /*Locale locale = new Locale(Locale.getDefault().getLanguage(), user.getCountry());
        String code = CountriesData.getCountryId(locale.getDisplayCountry());
        if(code != null){
            location.setText(code+", "+user.getCity());
        }*/
        // locale.getCountry() = SA
        // locale.getISO3Country() = SAU
        // locale.getDisplayCountry() = المملكة العربية السعودية

        followers.setText(String.valueOf(mFollowingCount));

        onClickListener();
    }

    private void keyboard(boolean show){
        if(show){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }else {
            // HideSoftInputFromWindow
            View v = ((Activity)mContext).getCurrentFocus();
            if (v == null) {
                v = new View(mContext);
            }
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    // ******** DATABASE
    private void fetchPersonalInfo() {

        String user_id = userId;
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userAccountSettings = (dataSnapshot.getValue(UserAccountSettings.class));
                    setDataInWidgets(userAccountSettings);
                }else {
                    ///progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
                Toast.makeText(mContext,  getString(R.string.error_internet) ,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
