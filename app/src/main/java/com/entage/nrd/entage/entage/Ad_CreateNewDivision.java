package com.entage.nrd.entage.entage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.EntagePageDivision;
import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Subscriptions.EntajiPageSubscriptionActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Ad_CreateNewDivision extends Fragment {
    private static final String TAG = "Ad_CreateNewDivision";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    private EditText nameDivision;
    private TextView createDivision, error;
    private ProgressBar progressBar;

    private ArrayList<EntagePageDivision> entagePageDivisions;
    private String entagePageId;
    private SubscriptionPackage subscriptionPackage;


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ad_create_new_division, container , false);
        mContext = getActivity();

        getDataFromBundle();
        init();

        return view;
    }

    private void getDataFromBundle(){
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageDivisions =  bundle.getParcelableArrayList("divisionsPage");
                entagePageId =  bundle.getString("entagePageId");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();

        getPackageEntagePage();
    }

    private void initWidgets() {
        nameDivision = view.findViewById(R.id.division_name);
        createDivision = view.findViewById(R.id.create_division);
        error = view.findViewById(R.id.error);
        progressBar = view.findViewById(R.id.progressBar_0);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.create_new_division_2));
        ImageView backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void onClickNewPage() {

        createDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String package_id = subscriptionPackage.getPackage_id();
                if(subscriptionPackage.isIs_running()){
                    if(package_id == null){
                        Toast.makeText(mContext, mContext.getString(R.string.entaji_page_package_error), Toast.LENGTH_LONG).show();
                    }
                    else if(package_id.equals("1_starter")){
                        if(entagePageDivisions!=null && entagePageDivisions.size()>=1){
                            upgradeDialog(mContext.getString(R.string.max_division_1), false);
                        }else {
                            createNewDivision();
                        }
                    }
                    else if(package_id.equals("2_flame")){
                        if(entagePageDivisions!=null &&entagePageDivisions.size()>=2){
                            upgradeDialog(mContext.getString(R.string.max_division_2), false);
                        }else {
                            createNewDivision();
                        }
                    }
                    else {
                        Toast.makeText(mContext, mContext.getString(R.string.entaji_page_package_error), Toast.LENGTH_LONG).show();
                    }
                }else {
                    upgradeDialog(mContext.getString(R.string.your_subscribe_finished)+" "+ subscriptionPackage.getExpire_date(), true);
                }
            }
        });

    }

    private void createNewDivision(){
        if(nameDivision.length() < 3){
            error.setText(mContext.getString(R.string.error_division_name));
        }else {
            createDivision.setEnabled(false);
            createDivision.setText("");
            nameDivision.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            error.setText("");

            if(entagePageDivisions == null){
                entagePageDivisions = new ArrayList<>();
            }


            String division_name = removeLastSpace(nameDivision.getText().toString()).replace("\n","");
            String division_name_db;
            if(entagePageDivisions.size()>0){
                // on case we have in db : categorie_1, categorie_3 so we avoid repeat categorie_3
                division_name_db = entagePageDivisions.get(entagePageDivisions.size()-1).getDivision_name_db()
                        .replace("categorie_","");
                division_name_db = "categorie_" + (Integer.parseInt(division_name_db) + 1);

            }else {
                division_name_db = "categorie_"+ (entagePageDivisions.size()+1);
            }


            boolean boo = true;
            for(EntagePageDivision entagePageDivision : entagePageDivisions ){
                if(division_name.equals(entagePageDivision.getDivision_name())){
                    boo = false;
                }
            }

            if(boo){
                entagePageDivisions.add(new EntagePageDivision(division_name_db, division_name, true));
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                myRef = mFirebaseDatabase.getReference();
                myRef.child(getString(R.string.dbname_entage_pages)) // entage_pages
                        .child(entagePageId) // Entage_page_id
                        .child(mContext.getString(R.string.field_entage_page_divisions)) // field_entage_page_divisions
                        .setValue(entagePageDivisions).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        /*Intent mIntent = getActivity().getIntent();
                        startActivity(mIntent); // restart th activity*/
                        if(isAdded() && mContext != null && getActivity() != null){
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }
                });
            }else {
                progressBar.setVisibility(View.GONE);
                createDivision.setEnabled(true);
                createDivision.setText(mContext.getString(R.string.create_new_division));
                nameDivision.setEnabled(true);
                error.setText(mContext.getString(R.string.error_name_used));
            }

        }
    }

    private String removeLastSpace(String text){
        if(text.length()>0){
            if(text.charAt(text.length()-1)== ' '){
                text = text.substring(0,text.length()-1);
                return removeLastSpace(text);
            }else {
                return text;
            }
        }else {
            return "";
        }

    }

    private void getPackageEntagePage(){
        progressBar.setVisibility(View.VISIBLE);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_subscription))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_current_subscription))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    subscriptionPackage = dataSnapshot.getValue(SubscriptionPackage.class);

                    progressBar.setVisibility(View.GONE);
                    onClickNewPage();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void upgradeDialog(String text, boolean subscribe){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_upgrade_subscription, null);
        TextView textView = _view.findViewById(R.id.text);
        TextView upgrade = _view.findViewById(R.id.upgrade);
        TextView cancel = _view.findViewById(R.id.cancel);

        if(subscribe){
            upgrade.setText(mContext.getString(R.string.subscribe));
        }

        textView.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        final AlertDialog alert = builder.create();

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("entajiPagesIds", entagePageId);
                Intent intent = new Intent(mContext, EntajiPageSubscriptionActivity.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

}
