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
import android.widget.TextView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesEditModeAdapter;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingCategoriesAddFragment extends Fragment {
    private static final String TAG = "SettingCategoriesAdd";

    private View view;
    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private TextView saveData;
    private ArrayAdapter<String> autoCompleteTextAdapterCat ;
    private InputMethodManager imm ;
    private AutoCompleteTextView autoCompleteTextAllCat, autoCompleteTextCat;
    private TextView path;
    private ImageView backPath;
    private RecyclerView recyclerViewItems;
    private ArrayList<String> categoriesNames;

    private CategoriesEditModeAdapter categoriesAdapter;
    private CategorieWithChildren mainParent;
    private MessageDialog messageDialog = new MessageDialog();
    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_add_categories , container , false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    private void inti(){
        initWidgets();
        onClickListener();

        setupCategories();
        setupAllCategories();


    }

    private void initWidgets() {
        autoCompleteTextAllCat = view.findViewById(R.id.auto_complete_text);
        saveData = view.findViewById(R.id.save_data);

        autoCompleteTextCat = view.findViewById(R.id.auto_complete_text_1);
        path = view.findViewById(R.id.path);
        backPath = view.findViewById(R.id.backPath);

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
        saveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void setupCategories(){
        recyclerViewItems =  view.findViewById(R.id.recyclerView_items);
        recyclerViewItems.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));

        mainParent = new CategorieWithChildren();
        categoriesAdapter = new CategoriesEditModeAdapter(mContext, mainParent, path, autoCompleteTextCat);
        recyclerViewItems.setAdapter(categoriesAdapter);

        autoCompleteTextAdapterCat = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, new ArrayList<String>());
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

    private void setupAllCategories(){
        // get all categories
        categoriesNames = new ArrayList<String>(Arrays.asList(CategoriesItemList.getCategories_array_name()));

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

    private void saveData() {
        saveData.setVisibility(View.GONE);
        view.findViewById(R.id.progressBarSave).setVisibility(View.VISIBLE);

        String key = myRef.child(mContext.getString(R.string.dbname_app_data)).child("categories").push().getKey();
        DatabaseReference reference = myRef.child(mContext.getString(R.string.dbname_app_data)).child("categories").child(key);


        // first child is parent node
        extraction(mainParent.getCategorieWithChildren().get(mainParent.getChildren().get(0)), reference);

        view.findViewById(R.id.progressBarSave).setVisibility(View.GONE);
        saveData.setVisibility(View.VISIBLE);
        messageDialog.errorMessage(mContext, mContext.getString(R.string.successfully_save));

        globalVariable.setCategoriesLists(null);
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
