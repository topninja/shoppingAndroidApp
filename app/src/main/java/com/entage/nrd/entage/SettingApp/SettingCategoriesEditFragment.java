package com.entage.nrd.entage.SettingApp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesEditModeAdapter;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
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
import java.util.Arrays;
import java.util.HashMap;

public class SettingCategoriesEditFragment extends Fragment {
    private static final String TAG = "SettingCategoriesAdd";

    private View view;
    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private InputMethodManager imm ;
    private TextView path, save, reload_data;
    private ImageView backPath;
    private AutoCompleteTextView autoCompleteTextCat, autoCompleteTextAllCat;
    private RecyclerView recyclerViewItems;
    private RelativeLayout layoutHappenedWrong;
    private ArrayAdapter<String> autoCompleteTextAdapterCat ;
    private MessageDialog messageDialog = new MessageDialog();

    private CategorieWithChildren mainParent;
    private CategoriesEditModeAdapter categoriesAdapter;
    private HashMap<String, String> categoriesByKey;
    private GlobalVariable globalVariable;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_edit_categories , container , false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    private void inti(){
        initWidgets();
        onClickListener();

        fetchCategoriesLists();
    }

    private void initWidgets() {
        autoCompleteTextAllCat = view.findViewById(R.id.auto_complete_text_1);
        autoCompleteTextCat = view.findViewById(R.id.auto_complete_text);
        path = view.findViewById(R.id.path);
        backPath = view.findViewById(R.id.backPath);
        layoutHappenedWrong = view.findViewById(R.id.layout_happened_wrong);
        save = view.findViewById(R.id.save_data);
        reload_data = view.findViewById(R.id.reload_data);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        CategoriesItemList.init(mContext);
    }

    private void onClickListener() {
        backPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoriesAdapter!=null){
                    categoriesAdapter.goPrevious();
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEditData();
            }
        });
        reload_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCategoriesLists();
            }
        });
    }

    private void setupAllCategories(){
        // get all categories
        ArrayList<String> categoriesNames = new ArrayList<String>(Arrays.asList(CategoriesItemList.getCategories_array_name()));

        //
        autoCompleteTextAdapterCat = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, categoriesNames);
        autoCompleteTextAllCat.setAdapter(autoCompleteTextAdapterCat);

        autoCompleteTextAllCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                keyboard(false);

                String catCode = CategoriesItemList.getCategories_code((String) parent.getItemAtPosition(position));

                categoriesAdapter.addNewCategorieToList(catCode);

                autoCompleteTextAllCat.setText("");
            }
        });

        autoCompleteTextAllCat.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                autoCompleteTextAllCat.showDropDown();
                return false;
            }
        });

    }

    private void setupAdapter(){
        recyclerViewItems =  view.findViewById(R.id.recyclerView_items);
        recyclerViewItems.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));


        categoriesAdapter = new CategoriesEditModeAdapter(mContext, mainParent, path, autoCompleteTextCat);
        recyclerViewItems.setAdapter(categoriesAdapter);
        recyclerViewItems.setVisibility(View.VISIBLE);

        ArrayList<String> arrayList = new ArrayList<>();
        for(String code : mainParent.getChildren()){
            arrayList.add(CategoriesItemList.getCategories_name(code));
        }
        autoCompleteTextAdapterCat = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, arrayList);
        autoCompleteTextCat.setAdapter(autoCompleteTextAdapterCat);

        autoCompleteTextCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));

                keyboard(false);

                String code = CategoriesItemList.getCategories_code((String) parent.getItemAtPosition(position));

                categoriesAdapter.selectFromAutoCompleteText(code);

                autoCompleteTextCat.setText("");
            }
        });

        autoCompleteTextCat.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                autoCompleteTextCat.showDropDown();
                return false;
            }
        });
    }

    private void keyboard(boolean show){
        //InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
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

    private void fetchCategoriesLists(){

        reload_data.setVisibility(View.GONE);

        categoriesByKey = new HashMap<>();
        mainParent = new CategorieWithChildren();
        mainParent.setParent(null);

        Query query = myRef
                .child(mContext.getString(R.string.dbname_app_data))
                .child("categories");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // keys
                    for(DataSnapshot snapshotKes : dataSnapshot.getChildren()){
                        String key = snapshotKes.getKey();

                        //main parent
                        for(DataSnapshot snapshotParent : snapshotKes.getChildren()){
                            String code = snapshotParent.getKey();
                            categoriesByKey.put(code, key);

                            CategorieWithChildren categorie = new CategorieWithChildren();
                            categorie.setParent(mainParent);
                            categorie.setCategorieCode(code);
                            categorie.setChildren((ArrayList<String>) snapshotParent.child("children").getValue());

                            getChildren(categorie, snapshotParent);

                            mainParent.getChildren().add(code);
                            mainParent.getCategorieWithChildren().put(code, categorie);
                        }
                    }
                }

                setupAdapter();
                setupAllCategories();
                view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);
                reload_data.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);
                layoutHappenedWrong.setVisibility(View.VISIBLE);
                reload_data.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getChildren(CategorieWithChildren categorieParent, DataSnapshot snapshotParent){
        Log.d(TAG, "getChildren: 123456789");
        //Log.d(TAG, "getChildren: " + categorieParent.getParentCode());
        for(String child : categorieParent.getChildren()){
            if(snapshotParent.child(child).exists()){
                CategorieWithChildren categorie = new CategorieWithChildren();
                categorie.setParent(categorieParent);
                categorie.setCategorieCode(child);
                categorie.setChildren((ArrayList<String>) snapshotParent.child(child).child("children").getValue());

                categorieParent.getCategorieWithChildren().put(child, categorie);

                getChildren(categorie, snapshotParent.child(child));
            }
        }
    }

    private void saveEditData(){
        CategorieWithChildren categorie = categoriesAdapter.getCurrentCategorie();

        if(categorie.getParent() == null){
            messageDialog.errorMessage(mContext, "لم تقم بتحديد الفئة ");

        }else {
            save.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.progressBarSave).setVisibility(View.VISIBLE);

            globalVariable.setCategoriesLists(null);

            // get parent code
            while (categorie.getParent().getParent() != null){
                categorie = categorie.getParent();
            }

            String oldKey = categoriesByKey.get(categorie.getCategorieCode());
            if(oldKey != null){
                final CategorieWithChildren finalCategorie = categorie;
                myRef.child(mContext.getString(R.string.dbname_app_data))
                        .child("categories").child(oldKey)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                String newKey = myRef.child(mContext.getString(R.string.dbname_app_data)).child("categories").push().getKey();
                                categoriesByKey.remove(finalCategorie.getCategorieCode());
                                categoriesByKey.put(finalCategorie.getCategorieCode(), newKey);

                                if(newKey != null){
                                    // first child is parent node
                                    extraction(finalCategorie, myRef.child(mContext.getString(R.string.dbname_app_data))
                                            .child("categories")
                                            .child(newKey));

                                    save.setVisibility(View.VISIBLE);
                                    view.findViewById(R.id.progressBarSave).setVisibility(View.GONE);
                                }
                            }
                        });
            }
        }
    }

    private void extraction(CategorieWithChildren categorieWithChildren, DatabaseReference reference){
        reference.child(categorieWithChildren.getCategorieCode()).
                child("children").setValue(categorieWithChildren.getChildren());

        for(CategorieWithChildren child : categorieWithChildren.getCategorieWithChildren().values()){
            extraction(child, reference.child(categorieWithChildren.getCategorieCode()));
        }
    }

    /*  ----------Firebase------------  */
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
                    getActivity().finish();
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
