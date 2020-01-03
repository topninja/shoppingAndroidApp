package com.entage.nrd.entage.editEntagePage;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.entage.ActivateEntagePageFragment;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.login.FragmentTermsAndConditionsOfEntajiPage;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Subscriptions.EntajiPageSubscriptionActivity;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsEntagePageActivity extends AppCompatActivity implements OnActivityListener {

    private static final String TAG = "EntageOptionsActivity";
    private Context mContext ;

    private ListView listView;
    private ArrayList<String> fragmentsNames;

    private EntagePage entagePage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entage_options);
        mContext = SettingsEntagePageActivity.this;
        Log.d(TAG, "onCreate: started.");

        setupSettingsListFragments();
        getIncomingIntent();

        //ColorTopMainBar.setColorTopMainBar(this);

        ((TextView)findViewById(R.id.titlePage)).setText(getString(R.string.setting_entage_page));
        // SetUp Back Arrow Button
        ImageView backArrow = (ImageView) findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG , "onClick: navigation back to entage setting.");
                finish();
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();
        try{
            Bundle extras = intent.getExtras();
            if(extras!=null){
                entagePage = extras.getParcelable("entagePageSelected");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getFragments().size() == 0){
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void setupSettingsListFragments(){
        fragmentsNames= new ArrayList<>();
        fragmentsNames.add(getString(R.string.entajiPageSubscription));
        fragmentsNames.add(getString(R.string.entageEditProfile));
        fragmentsNames.add(getString(R.string.entage_edit_categories));

        fragmentsNames.add(getString(R.string.entageOrdersArchive));
        fragmentsNames.add(getString(R.string.entageStatistics));
        fragmentsNames.add(getString(R.string.activate_entage_page));
        fragmentsNames.add(getString(R.string.entageHelpCenter));
        //fragmentsNames.add(getString(R.string.entageTermsOfUseEntajiPage));
        fragmentsNames.add(getString(R.string.cancel_delete_entaji_page));

        listView = findViewById(R.id.listViewEntageOptions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_black_text, fragmentsNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                String fragment = (String)adapter.getItemAtPosition(position);
                if(fragment.equals(getString(R.string.cancel_delete_entaji_page))){
                    checkCanDelete();

                }else {
                    openFragment(fragment);
                }
            }
        });
    }

    private void openFragment(String fragment){
        listView.setVisibility(View.GONE);

        Bundle bundle = new Bundle();
        bundle.putParcelable("entagePage", entagePage);

        if(fragment.equals(getString(R.string.entageEditProfile))){
            onActivityListener(new FragmentEditProfileEntage(), bundle);
        }

        else if(fragment.equals(getString(R.string.activate_entage_page))){
            onActivityListener(new ActivateEntagePageFragment());
        }

        else if(fragment.equals(getString(R.string.entage_edit_categories))){
            onActivityListener(new FragmentEditCategoriesEntage(), bundle);
        }

        else if(fragment.equals(getString(R.string.entageTermsOfUseEntajiPage))){
            onActivityListener(new FragmentTermsAndConditionsOfEntajiPage());
        }

        else if(fragment.equals(getString(R.string.entajiPageSubscription))){
            listView.setVisibility(View.VISIBLE);
            bundle.putString("entajiPagesIds", entagePage.getEntage_id());
            Intent intent = new Intent(mContext, EntajiPageSubscriptionActivity.class);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        else if (fragment.equals(getString(R.string.entageHelpCenter))){
            FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
            bundle.putString("typeProblem", mContext.getString(R.string.entaji_pages_help_center));
            bundle.putString("id", entagePage.getEntage_id());
            fragmentInformProblem.setArguments(bundle);
            onActivityListener(fragmentInformProblem);

        }else {
            listView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCanDelete(){
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_app_data))
                .child("can_delete");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean can_delete = (boolean) dataSnapshot.getValue();

                    if(can_delete){
                        deleteEntajiPage();

                    }else {
                        MessageDialog messageDialog = new MessageDialog();
                        messageDialog.errorMessage(mContext, "تم رفض الوصول الى هذه العملية");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void deleteEntajiPage(){
        String entajiPageId = entagePage.getEntage_id();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();


    }



    // ** OnActivityListener
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


}
