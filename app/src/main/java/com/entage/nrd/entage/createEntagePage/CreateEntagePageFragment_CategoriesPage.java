package com.entage.nrd.entage.createEntagePage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.CategoriesSearchingAdapter;
import com.entage.nrd.entage.utilities_1.CategoriesSelectedViewAdapter;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CreateEntagePageFragment_CategoriesPage extends Fragment{
    private static final String TAG = "CreateEntagePageFragmen";

    private View view;
    private Context mContext;
    private OnActivityListener onActivityListener;
    private CreateEntagePageListener mCreateEntagePageListener;

    private TextView  showAllCategories;
    private TextView messageError;
    private CategorieWithChildren mainParent;
    private ArrayList<String> selectedCategoriesAlgoliaCode;
    private CategoriesSearchingAdapter categoriesAdapter;
    private View dialogView;
    private AlertDialog.Builder builder;
    private AlertDialog alert;

    private ArrayList<String> selectedCategories;
    private GlobalVariable globalVariable;

    public CreateEntagePageFragment_CategoriesPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_create_entage_page, container , false);
            mContext = getActivity();

            inti();
        }else {
            onClickListener();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            onActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        try{
            mCreateEntagePageListener = (CreateEntagePageListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti(){
        initWidgets();
        onClickListener();

        selectedCategories = mCreateEntagePageListener.getEntagePage().getCategories_entage_page();
        if(categoriesAdapter != null){
            categoriesAdapter.setSelectedCategorieDb(selectedCategories);
        }

        if(globalVariable.getCategoriesLists() != null) {
            mainParent = globalVariable.getCategoriesLists();

        }else {
            fetchCategoriesLists();
        }

    }

    private void initWidgets() {
        showAllCategories = view.findViewById(R.id.show_all_categories);
        messageError = view.findViewById(R.id.messageError_1);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        selectedCategoriesAlgoliaCode = new ArrayList<>();

        view.findViewById(R.id.linearLayout_1).setVisibility(View.GONE);
        view.findViewById(R.id.linearLayout_2).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.creat_entage_page_select_categories_4));
        ((TextView)view.findViewById(R.id.text2)).setText(mContext.getString(R.string.creat_entage_page_select_categories_1));
        ((ImageView)view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_categories);
    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(null);
        mCreateEntagePageListener.getNextButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnableButtons(false);
                messageError.setText("");
                if(categoriesAdapter != null && categoriesAdapter.getSelectedCategoriesToDb().size() != 0){
                    setEnableButtons(true);
                    mCreateEntagePageListener.getEntagePage().setCategories_entage_page(categoriesAdapter.getSelectedCategoriesToDb());
                    onActivityListener.onActivityListener(new CreateEntagePageFragment_EmailPage());

                }else {
                    messageError.setVisibility(View.VISIBLE);
                    messageError.setText(mContext.getString(R.string.error_name_page_select_categories_must_1));
                    setEnableButtons(true);
                }

            }
        });

        showAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(globalVariable.getCategoriesLists() == null) {
                    openWaiteDialog();
                }else {
                    openDialogCategories();
                }
            }
        });
    }

    private void openDialogCategories() {
        if(dialogView == null){
            dialogView = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
            ((TextView)dialogView.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.the_categories));
            ((TextView)dialogView.findViewById(R.id.text_view1)).setTextSize(20);
            dialogView.findViewById(R.id.relLayout_categories).setVisibility(View.VISIBLE);

            //mContext.getString(R.string.creat_entage_page_select_categories)
            setupAdapterCategories(dialogView);

            builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(dialogView);
            builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            alert = builder.create();

            categoriesAdapter.setAlertDialog(alert);
        }

        alert.show();
    }

    private void setupAdapterCategories(View view){
        RecyclerView recyclerView = this.view.findViewById(R.id.listViewSelectedCategories);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager1);
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        CategoriesSelectedViewAdapter adapter = new CategoriesSelectedViewAdapter(mContext, selectedCategoriesAlgoliaCode);
        recyclerView.setAdapter(adapter);


        ///
        view.findViewById(R.id.progressBarCategories_1).setVisibility(View.GONE);
        TextView path = view.findViewById(R.id.path);
        ImageView backPath = view.findViewById(R.id.backPath);
        final AutoCompleteTextView autoCompleteTextCat = view.findViewById(R.id.auto_complete_text);

        RecyclerView recyclerViewItems = view.findViewById(R.id.recyclerView_categories);
        recyclerViewItems.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));

        categoriesAdapter = new CategoriesSearchingAdapter(mContext, recyclerViewItems, mainParent, selectedCategoriesAlgoliaCode,
                path, autoCompleteTextCat, adapter);
        recyclerViewItems.setAdapter(categoriesAdapter);

        adapter.setCategoriesAdapter(categoriesAdapter);


        ArrayList<String> arrayList = new ArrayList<>();
        for(String code : mainParent.getChildren()){
            arrayList.add(CategoriesItemList.getCategories_name(code));
        }

        ArrayAdapter<String> autoCompleteTextAdapterCat = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, arrayList);
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

        backPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(categoriesAdapter!=null){
                    categoriesAdapter.goPrevious();
                }
            }
        });

        categoriesAdapter.setSelectedCategorieDb(selectedCategories);
    }

    private void setEnableButtons(boolean enable){
        mCreateEntagePageListener.getNextButton().setEnabled(enable);
        if(enable){
            mCreateEntagePageListener.getNextButton().getChildAt(1).setVisibility(View.GONE);
            mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue));
        }else {
            mCreateEntagePageListener.getNextButton().getChildAt(1).setVisibility(View.VISIBLE);
            mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue_ops));
        }
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

    private void openWaiteDialog(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.loading_categories));
        _view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alert.dismiss();
                if(globalVariable.getCategoriesLists() != null){
                    openDialogCategories();
                }
            }
        }, 3000);
    }

    private void fetchCategoriesLists(){

        CategoriesItemList.init(mContext);
        mainParent = new CategorieWithChildren();
        mainParent.setParent(null);

        Query query = FirebaseDatabase.getInstance().getReference()
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
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                view.findViewById(R.id.progressBarCategories).setVisibility(View.GONE);
                view.findViewById(R.id.layout_happened_wrong).setVisibility(View.VISIBLE);
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

}
