package com.entage.nrd.entage.newItem;


import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.Models.EntagePageDivision;
import com.entage.nrd.entage.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentDivisionItem extends Fragment {
    private static final String TAG = "FragmentName_Catego";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view ;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, myRefArchives, myRefItems;

    private RadioGroup divisions;
    private ArrayList<EntagePageDivision> entage_page_divisions;

    // Edit Mode
    private String division_item, new_division_item;

    private String entagePageId, itemId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_division_item, container, false);
        mContext = getActivity();

        getIncomingBundle();

        setupFirebaseAuth();

        init();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId = bundle.getString("entagePageId");
                itemId = bundle.getString(mContext.getString(R.string.field_item_id));
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        setupBarAddItemLayout();

        initWidgets();
        onClickListener();

        getDataFromDb();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.change_division_item));

        divisions = view.findViewById(R.id.radioGrp);
    }

    private void onClickListener() {

    }

    private void setupDivisions(){
        if(entage_page_divisions != null){
            for (final EntagePageDivision entagePageDivision : entage_page_divisions){

                AppCompatRadioButton radioButton = new AppCompatRadioButton(mContext);
                radioButton.setText(entagePageDivision.getDivision_name());
                radioButton.setTextSize(18);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.entage_blue)));
                }

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            new_division_item = entagePageDivision.getDivision_name_db();
                        }
                    }
                });

                divisions.addView(radioButton);

                if(division_item.equals(entagePageDivision.getDivision_name_db())){
                    radioButton.setChecked(true);
                }
            }
        }
    }

    private void getDataFromDb() {
        showProgress(false);

        entage_page_divisions = new ArrayList<>();
        // get name item
        Query query = myRef
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_entage_page_divisions));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        entage_page_divisions.add(snapshot.getValue(EntagePageDivision.class));
                    }
                }

                // get name item
                Query query = myRefItems
                        .child(mContext.getString(R.string.field_item_in_categorie));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            division_item = (String) dataSnapshot.getValue();
                        }

                        setupDivisions();
                        showProgress(true);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
                showProgress(true);
                Toast.makeText(mContext,  mContext.getString(R.string.happened_wrong_try_again) ,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataToDb() {
        if(!division_item.equals(new_division_item)){

            showProgress(false);
            // edit division in: items/
            myRef.child(getString(R.string.dbname_items))
                    .child(itemId)
                    .child(mContext.getString(R.string.field_item_in_categorie))
                    .setValue(new_division_item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //remove item from old division
                            myRef.child(mContext.getString(R.string.dbname_entage_page_categories)) // entage_page_categories
                                    .child(entagePageId) // Entage_page_id
                                    .child(division_item) // Categorie Name .child(newItem.getCategories_item().get(0))
                                    .child(mContext.getString(R.string.field_categorie_items)) // Categorie_items
                                    .child(itemId) // Item Id
                                    .removeValue();// Item

                            // set Item to new Division
                            myRef.child(mContext.getString(R.string.dbname_entage_page_categories)) // entage_page_categories
                                    .child(entagePageId) // Entage_page_id
                                    .child(new_division_item) // Categorie Name .child(newItem.getCategories_item().get(0))
                                    .child(mContext.getString(R.string.field_categorie_items)) // Categorie_items
                                    .child(itemId) // Item Id
                                    .setValue(itemId) // Item
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                                    Toast.LENGTH_SHORT).show();


                                            // restart th activity
                                            Intent intent = new Intent(getActivity(), EntageActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showProgress(true);
                            Toast.makeText(mContext,  mContext.getString(R.string.happened_wrong_try_again)+ " " + e.getMessage() ,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        }else {

            Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                    Toast.LENGTH_SHORT).show();

        }
    }

    /*  -------------------------------Firebase-------------------------------------------------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        if(itemId != null){
            myRef = mFirebaseDatabase.getReference();

            myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                    .child(itemId);
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    // setup Bar Add Item Layout
    private TextView save;
    private RelativeLayout nextStep;
    private ImageView tips;
    private void setupBarAddItemLayout(){
        save = view.findViewById(R.id.save);
        save.setText(mContext.getString(R.string.change_division_item));
        nextStep = view.findViewById(R.id.next_step);
        nextStep.setVisibility(View.GONE);
        view.findViewById(R.id.progress_next_step).setVisibility(View.GONE);

        tips = view.findViewById(R.id.tips);

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataToDb();
            }
        });

        view.findViewById(R.id.progress_next_step).setVisibility(View.GONE);
        nextStep.setVisibility(View.GONE);
    }

    private void showProgress(boolean boo){
        save.setEnabled(boo);
        save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
    }

}
