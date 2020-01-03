package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesAdapter;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentCategoriesItem extends Fragment {
    private static final String TAG = "FragmentCategoriesItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefArchivesItemFor, myRefItemsItemFor, myRefLastEdit;

    private TextInputLayout textInputLayout;
    private TextView path, removeAllCategories;
    private ImageView backPath;
    private TextView selectedCategoriesText;
    private AutoCompleteTextView autoCompleteTextCat;
    private RecyclerView recyclerViewItems;
    private RelativeLayout layoutHappenedWrong;
    private ArrayAdapter<String> autoCompleteTextAdapterCat ;
    private InputMethodManager imm ;
    private CheckBox item_for_all, item_for_men, item_for_women, item_for_children;

    private CategorieWithChildren mainParent;
    private CategoriesAdapter categoriesAdapter;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();

    private String entagePageId, itemId;
    private boolean isDataFetched = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_categories_item, container, false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();
            init();
        }

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

    public void getIncomingBundle() {
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
        setupAdviceLayout();
        setupBarAddItemLayout();

        initWidgets();
        onClickListener();

        if(globalVariable.getCategoriesLists() != null){
            mainParent = globalVariable.getCategoriesLists();
            setupAdapter();
            getDataFromDbArchives();
            view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);

        }else {
            fetchCategoriesLists();
        }
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[2]);

        autoCompleteTextCat = view.findViewById(R.id.auto_complete_text);
        path = view.findViewById(R.id.path);
        backPath = view.findViewById(R.id.backPath);
        selectedCategoriesText = view.findViewById(R.id.selectedCategories);
        removeAllCategories = view.findViewById(R.id.removeAllCategories);
        layoutHappenedWrong = view.findViewById(R.id.layout_happened_wrong);
        textInputLayout  = view.findViewById(R.id.TextInputLayout1);

        item_for_all = view.findViewById(R.id.item_for_all);
        item_for_men = view.findViewById(R.id.item_for_men);
        item_for_women  = view.findViewById(R.id.item_for_women);
        item_for_children = view.findViewById(R.id.item_for_children);

        textInputLayout.setErrorEnabled(true);
        textInputLayout.setErrorTextAppearance(R.style.ErrorText);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        CategoriesItemList.init(mContext);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
    }

    private void onClickListener(){
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

        item_for_all.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    item_for_women.setChecked(false);
                    item_for_men.setChecked(false);
                    item_for_children.setChecked(false);
                }
            }
        });
        item_for_men.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(item_for_women.isChecked() && item_for_children.isChecked()){
                        item_for_women.setChecked(false);
                        item_for_children.setChecked(false);
                        item_for_all.setChecked(true);
                    }else {
                        item_for_all.setChecked(false);
                    }
                }
            }
        });
        item_for_women.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(item_for_men.isChecked() && item_for_children.isChecked()){
                        item_for_men.setChecked(false);
                        item_for_children.setChecked(false);
                        item_for_all.setChecked(true);
                    }else {
                        item_for_all.setChecked(false);
                    }
                }
            }
        });
        item_for_children.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(item_for_women.isChecked() && item_for_men.isChecked()){
                        item_for_women.setChecked(false);
                        item_for_men.setChecked(false);
                        item_for_all.setChecked(true);
                    }else {
                        item_for_all.setChecked(false);
                    }
                }
            }
        });

    }

    private void setupAdapter(){
        recyclerViewItems =  view.findViewById(R.id.recyclerView_items);

        categoriesAdapter = new CategoriesAdapter(mContext, mainParent, path, selectedCategoriesText, false, autoCompleteTextCat);

        recyclerViewItems.setNestedScrollingEnabled(false);
        recyclerViewItems.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));
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

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_categories_item));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_categories_item));

        myRefLastEdit = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_last_edit_was_in));

        myRefArchivesItemFor = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_items))
                .child(itemId);

        myRefItemsItemFor = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId);


        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void getDataFromDbArchives(){
        showProgress(false, false);
        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    ArrayList<String> arrayList = (ArrayList<String>) dataSnapshot.getValue();
                    categoriesAdapter.setSelectedCategorieDb(arrayList);

                    showProgress(true, false);

                }else {
                    getDataFromDbItems();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });

        //
        Query query1 = myRefArchivesItemFor;
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child(mContext.getString(R.string.field_item_for_men)).exists()){
                        item_for_men.setChecked((Boolean) dataSnapshot.child(mContext.getString(R.string.field_item_for_men)).getValue());
                    }
                    if(dataSnapshot.child(mContext.getString(R.string.field_item_for_women)).exists()){
                        item_for_women.setChecked((Boolean) dataSnapshot.child(mContext.getString(R.string.field_item_for_women)).getValue());
                    }
                    if(dataSnapshot.child(mContext.getString(R.string.field_item_for_children)).exists()){
                        item_for_children.setChecked((Boolean) dataSnapshot.child(mContext.getString(R.string.field_item_for_children)).getValue());
                    }

                    if(!item_for_men.isChecked() && !item_for_women.isChecked() && !item_for_children.isChecked()){
                        item_for_all.setChecked(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isDataFetched = false;
            }
        });
    }

    private void getDataFromDbItems(){
        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    categoriesAdapter.setSelectedCategorieDb((ArrayList<String>) dataSnapshot.getValue());
                }
                showProgress(true, false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });

        //
        Query query1 = myRefItemsItemFor;
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child(mContext.getString(R.string.field_item_for_men)).exists()){
                        item_for_men.setChecked((Boolean) dataSnapshot.child(mContext.getString(R.string.field_item_for_men)).getValue());
                    }
                    if(dataSnapshot.child(mContext.getString(R.string.field_item_for_women)).exists()){
                        item_for_women.setChecked((Boolean) dataSnapshot.child(mContext.getString(R.string.field_item_for_women)).getValue());
                    }
                    if(dataSnapshot.child(mContext.getString(R.string.field_item_for_children)).exists()){
                        item_for_children.setChecked((Boolean) dataSnapshot.child(mContext.getString(R.string.field_item_for_children)).getValue());
                    }

                    if(!item_for_men.isChecked() && !item_for_women.isChecked() && !item_for_children.isChecked()){
                        item_for_all.setChecked(true);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                isDataFetched = false;
            }
        });
    }

    private void databaseError(DatabaseError databaseError){
        Log.d(TAG, "onCancelled: query cancelled");
        if(databaseError.getMessage().equals("Permission denied")){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied)+ "  " +
                    databaseError.getMessage());
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_msg)+ "  " +
                    databaseError.getMessage());
        }
    }

    private void saveData() {
        Log.d(TAG, "saveData: ");
        keyboard(false);
        textInputLayout.setErrorEnabled(false);
        view.findViewById(R.id.error_chose_one).setVisibility(View.GONE);

        if ((categoriesAdapter != null && categoriesAdapter.getSelectedCategorieNames().size() == 0) ){
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(mContext.getString(R.string.error_select_one_categorie));

        }else if (!item_for_men.isChecked() && !item_for_women.isChecked() && !item_for_children.isChecked() && !item_for_all.isChecked()){
            view.findViewById(R.id.error_chose_one).setVisibility(View.VISIBLE);

        }else {
            showProgress(false, true);

            ArrayList<String> arrayList = new ArrayList<>(categoriesAdapter.getSelectedCategorieToDb());
            myRefArchives.setValue(arrayList)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                myRefArchivesItemFor.child(mContext.getString(R.string.field_item_for_men)).setValue(item_for_men.isChecked());
                                myRefArchivesItemFor.child(mContext.getString(R.string.field_item_for_women)).setValue(item_for_women.isChecked());
                                myRefArchivesItemFor.child(mContext.getString(R.string.field_item_for_children)).setValue(item_for_children.isChecked());

                                updateTimeLastEdit();
                                Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                        Toast.LENGTH_SHORT).show();

                            }else {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) +
                                        task.getException().getMessage());
                            }

                            showProgress(true, true);
                        }
                    });
        }
    }

    private void updateTimeLastEdit(){
        myRefLastEdit.setValue(DateTime.getTimestamp());
    }

    private void fetchCategoriesLists(){

        mainParent = new CategorieWithChildren();
        mainParent.setParent(null);
        mainParent.setChildren(new ArrayList<String>());

        Query query = mFirebaseDatabase.getReference()
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

                getDataFromDbArchives();

                view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);
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

    // setup Bar Add Item Layout
    private TextView save;
    private RelativeLayout nextStep;
    private ImageView tips;
    private void setupBarAddItemLayout(){
        save = view.findViewById(R.id.save);
        nextStep = view.findViewById(R.id.next_step);

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_description_item);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataFetched){
                    saveData();
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");
                }
            }
        });

        nextStep.findViewById(R.id.next_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentDescriptionItem());
            }
        });
    }

    private void showProgress(boolean boo, boolean all){
        if(all){
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            nextStep.setEnabled(boo);
            nextStep.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            tips.setEnabled(boo);

        }else {
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        }

        //
    }

    private void setupAdviceLayout(){
        tips = view.findViewById(R.id.tips);
        final String title = mContext.getResources().getStringArray(R.array.advices)[2];
        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices(), bundle);
            }
        });
        view.findViewById(R.id.advice_oky).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilitiesMethods.collapse(view.findViewById(R.id.advice_linear_layout));
            }
        });
        view.findViewById(R.id.advice_see_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices(), bundle);
            }
        });
        ((TextView)view.findViewById(R.id.advice_title)).setText(title);
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_categories_item)[0]);
    }


}
