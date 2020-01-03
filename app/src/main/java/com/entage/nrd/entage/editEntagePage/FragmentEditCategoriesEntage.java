package com.entage.nrd.entage.editEntagePage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.AddingItemToAlgolia;
import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesAdapter;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentEditCategoriesEntage extends Fragment {
    private static final String TAG = "FragmentEditCateg";


    private View view ;
    private Context mContext;

    private ImageView backArrow;
    private TextView  save;
    private ProgressBar mProgressBar ;
    private RecyclerView recyclerViewItems;
    private TextView selectedCategoriesText;
    private AutoCompleteTextView autoCompleteTextCat;
    private TextView path, removeAllCategories;
    private ImageView backPath;
    private RelativeLayout layoutHappenedWrong;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private GlobalVariable globalVariable;
    private CategorieWithChildren mainParent;
    private CategoriesAdapter categoriesAdapter;
    private ArrayAdapter<String> autoCompleteTextAdapterCat ;
    private MessageDialog messageDialog = new MessageDialog();

    private EntagePage entagePage;;
    private String objectId;
    private ArrayList<String> currentCategories;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_categories_entage , container , false);
        mContext = getActivity();

        setupFirebaseAuth();

        getIncomingBundle();
        init();

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePage =  bundle.getParcelable("entagePage");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(){
        initWidgets();
        onClickListener();

        getObjectIdFromAlgolia();

        if(globalVariable.getCategoriesLists() != null){
            mainParent = globalVariable.getCategoriesLists();
            setupAdapter();
            view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);
            getCategoriesFromDb();

        }else {
            fetchCategoriesLists();
        }

    }

    private void initWidgets(){
        mProgressBar = view.findViewById(R.id.progressBar);
        path = view.findViewById(R.id.path);
        backPath = view.findViewById(R.id.backPath);
        selectedCategoriesText = view.findViewById(R.id.selectedCategories);
        removeAllCategories = view.findViewById(R.id.removeAllCategories);
        layoutHappenedWrong = view.findViewById(R.id.layout_happened_wrong);
        autoCompleteTextCat = view.findViewById(R.id.auto_complete_text);

        ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.entage_edit_categories));
        backArrow = (ImageView) view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        save = view.findViewById(R.id.save);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        backPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoriesAdapter!=null){
                    categoriesAdapter.goPrevious();
                }
            }
        });

        removeAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoriesAdapter!=null){
                    categoriesAdapter.removeAllSelected();
                }
            }
        });
    }

    private void setupAdapter(){
        recyclerViewItems =  view.findViewById(R.id.recyclerView_items);
        recyclerViewItems.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));

        categoriesAdapter = new CategoriesAdapter(mContext, mainParent, path, selectedCategoriesText, true, autoCompleteTextCat);
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


        // set data
        categoriesAdapter.setSelectedCategorieDb(entagePage.getCategories_entage_page());
    }

    private boolean checkInputData(){

        if(categoriesAdapter != null && categoriesAdapter.getSelectedCategorieNames().size() == 0){
            setErrorMessage(selectedCategoriesText,  mContext.getString(R.string.error_select_one_categorie));
            return false;
        }

        return true;
    }

    private boolean isThereChangeInCategories(){
        ArrayList<ArrayList<String>> categories = categoriesAdapter.getSelectedCategoriePaths();
        ArrayList<ArrayList<String>> categoriesItem = new ArrayList<>();
        for(String cat : entagePage.getCategories_entage_page()){
            categoriesItem.add(StringManipulation.convertPrintedArrayListToArrayListObject(cat));
        }

        // check
        if(categories.size() != categoriesItem.size()){
            return true;
        }

        for(ArrayList<String> arrayList_1 : categories){
            boolean isEqual = false;
            for(ArrayList<String> arrayList_2 : categoriesItem){
                if(arrayList_1.equals(arrayList_2)){
                    isEqual = true;
                }
            }
            if(!isEqual){
                return true;
            }
        }

        return false;
    }

    private void setErrorMessage(TextView editText, String message){
        TextInputLayout textInputLayout = (TextInputLayout) editText.getParentForAccessibility();
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setErrorTextAppearance(R.style.ErrorText);
        textInputLayout.setError(message);
    }

    private void keyboard(boolean show){
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
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

    private void saveData(){

        save.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        if(checkInputData() && isThereChangeInCategories()) {
            if(objectId !=null){

                String APIKey = "cf51400386c025e21bbbff240f715906";
                GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
                Client client = new Client(globalVariable.getApplicationID(), APIKey);
                Index index_items = client.getIndex("entage_pages");

                final ArrayList<String> categories = categoriesAdapter.getSelectedCategorieToDb();


                AddingItemToAlgolia itemToAlgolia = new AddingItemToAlgolia(null, null, null,
                        StringManipulation.getCategoriesForAlgolia(categories), null);
                // add main categories also
                for(String s : categories){
                    ArrayList<String> arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(s);
                    if(!itemToAlgolia.getCategorie_level_2().contains(arrayList.get(0))){
                        itemToAlgolia.getCategorie_level_2().add(arrayList.get(0));
                    }
                }


                index_items.partialUpdateObjectAsync(new JSONObject(itemToAlgolia.getItem()), objectId,
                        false,  new CompletionHandler() {
                            @Override
                            public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                                if (error != null) {
                                    mProgressBar.setVisibility(View.GONE);
                                    save.setVisibility(View.VISIBLE);
                                    messageDialog.errorMessage(mContext, error.getMessage());

                                }else {
                                    setItemByCategories(categories);

                                    myRef.child(getString(R.string.dbname_entage_pages)) // entage_page_categories
                                            .child(entagePage.getEntage_id()) // Entage_page_id
                                            .child(mContext.getString(R.string.field_categories_entage_page))
                                            .setValue(categories);

                                    entagePage.setCategories_entage_page(categories);

                                    mProgressBar.setVisibility(View.GONE);
                                    save.setVisibility(View.VISIBLE);
                                }
                            }
                        });

            }else {
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error));
                mProgressBar.setVisibility(View.GONE);
                save.setVisibility(View.VISIBLE);
            }
        }else {
            mProgressBar.setVisibility(View.GONE);
            save.setVisibility(View.VISIBLE);
        }
    }

    private void setItemByCategories(ArrayList<String> categories){

        // remove current in db
        for(String path : entagePage.getCategories_entage_page()){
            ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
            DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_entage_pages_by_categories));
            for(String catId : cat){
                databaseReference = databaseReference.child(catId);

                databaseReference.child("entage_page_id")
                        .child(entagePage.getEntage_id())
                        .removeValue();
            }
        }

        // add new to db
        for(String path : categories){
            ArrayList<String> cat = StringManipulation.convertPrintedArrayListToArrayListObject(path);
            DatabaseReference databaseReference =  myRef.child(mContext.getString(R.string.dbname_entage_pages_by_categories));
            for(String catId : cat){
                databaseReference = databaseReference.child(catId);

                databaseReference.child("entage_page_id")
                        .child(entagePage.getEntage_id())
                        .setValue(entagePage.getEntage_id());
            }
        }
    }

    private void fetchCategoriesLists(){

        mainParent = new CategorieWithChildren();
        mainParent.setParent(null);
        mainParent.setChildren(new ArrayList<String>());

        Query query = myRef
                .child(mContext.getString(R.string.dbname_app_data))
                .child("categories");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // keys
                    for(DataSnapshot snapshotKes : dataSnapshot.getChildren()){
                        //main parent
                        for(DataSnapshot snapshotParent : snapshotKes.getChildren()){
                            String code = snapshotParent.getKey();
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

                globalVariable.setCategoriesLists(mainParent);
                setupAdapter();
                view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);

                getCategoriesFromDb();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);
                layoutHappenedWrong.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getChildren(CategorieWithChildren categorieParent, DataSnapshot snapshotParent){
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

    private void getObjectIdFromAlgolia(){
        mProgressBar.setVisibility(View.VISIBLE);
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(entagePage.getEntage_id())
                .setAttributesToRetrieve("objectID");
        //.setHitsPerPage(50);

        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        Client client = new Client(globalVariable.getApplicationID(), globalVariable.getAPIKey());
        Index index_items = client.getIndex("entage_pages");

        index_items.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                try {
                    if(content != null){
                        JSONArray jsonArray = content.getJSONArray("hits");
                        for(int i=0 ; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            objectId = jsonObject.get("objectID").toString();

                            mProgressBar.setVisibility(View.GONE);
                            save.setVisibility(View.VISIBLE);
                        }
                    }else {
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getCategoriesFromDb(){
        /*currentCategories = new ArrayList<>();

        Query query = myRef
                .child(entagePage.getEntage_id()) // Entage_page_id
                .child(mContext.getString(R.string.field_categories_entage_page));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentCategories = (ArrayList<String>) dataSnapshot.getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });*/
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

}
