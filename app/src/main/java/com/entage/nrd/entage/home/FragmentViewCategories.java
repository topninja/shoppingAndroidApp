package com.entage.nrd.entage.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.entage.nrd.entage.Models.DataViewCategorie;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.LayoutViewCategorie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentViewCategories extends Fragment {
    private static final String TAG = "FragmentSearch";

    private OnActivityListener mOnActivityListener;

    private DatabaseReference reference;

    private Context mContext ;
    private View view;
    private ViewPager viewPager;

    private InputMethodManager imm;
    private boolean doneLoad = false;
    private CategorieWithChildren mainParent;
    private LinearLayout container;
    private ShimmerRecyclerView shimmerRecycler;

    private RelativeLayout layoutSearch;
    private ImageView home, personal;

    private GlobalVariable globalVariable;
    private FragmentSearch fragmentSearch;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_view_categories, container , false);
        mContext = getActivity();
        viewPager = (ViewPager) container;

        init();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void init() {
        initWidgets();
        onClickListener();

        //layoutFood();

        if(globalVariable.getCategoriesLists() != null) {
            mainParent = globalVariable.getCategoriesLists();
            doneLoad = true;
            setupLayouts();
        }else {
            shimmerRecycler.showShimmerAdapter();
            fetchCategoriesLists();
        }
    }

    private void initWidgets() {
        shimmerRecycler = view.findViewById(R.id.shimmer_recycler_view);
        home =  view.findViewById(R.id.home);
        layoutSearch  =  view.findViewById(R.id.relLayoutSearch);
        personal =  view.findViewById(R.id.personal);

        container = view.findViewById(R.id.container);
        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        CategoriesItemList.init(mContext);

        view.findViewById(R.id.text_search).setFocusable(false);
        view.findViewById(R.id.text_search).setFocusableInTouchMode(false);

        reference =  FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbname_app_data));
    }

    private void setupLayouts() {
        shimmerRecycler.hideShimmerAdapter();
        shimmerRecycler.setVisibility(View.GONE);

        /*layout("ca_su_03", R.drawable.bar_food, null, "#ffdbf1a6");
        mainCategoriesView.remove("ca_su_03");
        layout("ca_su_01", R.drawable.bar_phone, null, "#ffa3e5f3");
        mainCategoriesView.remove("ca_su_01");
        layout("ca_su_04", R.drawable.ph_clothes, null, "#ffc1af9b");
        mainCategoriesView.remove("ca_su_04");

        for(Map.Entry<String, DataViewCategorie> map : mainCategoriesView.entrySet()){
            String code = map.getKey();
            DataViewCategorie view = map.getValue();

            Log.d(TAG, "setupLayouts: "+ code+ ", " + view.toString());

            layout(code, 0, view.getImage_url(), view.getColor_hex()!=null?view.getColor_hex():"#cfe2ed");
        }*/

        for(String code : mainParent.getChildren()){
            getDataViewCategorie(code);
        }
    }

    private void layout(final String mainCategorieCode, int resId, String image_url, String color){
        final LayoutViewCategorie layoutViewCategorie = new LayoutViewCategorie(mContext, mainCategorieCode, resId, image_url, color);
        container.addView(layoutViewCategorie);

        layoutViewCategorie.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String code = layoutViewCategorie.getArrayListCode().get(position);

                ArrayList<String> arrayList = mainParent.getCategorieWithChildren().get(mainCategorieCode).getPathOfChild(code);
                openSearchPage(arrayList);
            }
        });
    }

    private void onClickListener() {
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0, true);
            }
        });

        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2, true);
            }
        });

        layoutSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(globalVariable.getCategoriesLists() == null) {
                       openWaiteDialog();
                }else {
                    openSearchPage(null);
                }
            }
        });
        view.findViewById(R.id.text_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(globalVariable.getCategoriesLists() == null) {
                    openWaiteDialog();
                }else {
                    openSearchPage(null);
                }
            }
        });
    }

    private void openSearchPage(ArrayList<String> arrayList){
        if(arrayList != null){
            fragmentSearch = new FragmentSearch();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("facets", arrayList);
            mOnActivityListener.onActivityListener(fragmentSearch, bundle);

        }else {
            if(fragmentSearch == null){
                fragmentSearch = new FragmentSearch();
            }
            mOnActivityListener.onActivityListener(fragmentSearch);
        }
    }

    private void fetchCategoriesLists(){

        CategoriesItemList.init(mContext);
        mainParent = new CategorieWithChildren();
        mainParent.setParent(null);

        reference
                .child("categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        // keys
                        for(DataSnapshot snapshotKes : dataSnapshot.getChildren()){
                            String key = snapshotKes.getKey();

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

                    doneLoad = true;
                    globalVariable.setCategoriesLists(mainParent);
                    setupLayouts();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
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

    private void openWaiteDialog(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.loading_categories));
        _view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        /*builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });*/
        final AlertDialog alert = builder.create();
        alert.show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alert.dismiss();
                if(globalVariable.getCategoriesLists() != null){
                    openSearchPage(null);
                }
            }
        }, 3000);
    }

    private void getDataViewCategorie(final String code){
        if(code.equals("ca_su_03")){
            layout("ca_su_03", R.drawable.bar_food, null, "#ffdbf1a6");
            //mainCategoriesView.remove("ca_su_03");

        }else if(code.equals("ca_su_01")){
            layout("ca_su_01", R.drawable.bar_phone, null, "#ffa3e5f3");
            //mainCategoriesView.remove("ca_su_01");

        }else if(code.equals("ca_su_04")){
            layout("ca_su_04", R.drawable.ph_clothes, null, "#ffc1af9b");
            //mainCategoriesView.remove("ca_su_04");

        }else {
            final DataViewCategorie[] dataViewCategorie = {new DataViewCategorie()};
            reference.child("data_view_categorie")
                    .child(code)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){
                                dataViewCategorie[0] = dataSnapshot.getValue(DataViewCategorie.class);
                            }

                            layout(code, 0, dataViewCategorie[0].getImage_url(),
                                    dataViewCategorie[0].getColor_hex()!=null? dataViewCategorie[0].getColor_hex():"#cfe2ed");
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            layout(code, 0, dataViewCategorie[0].getImage_url(),
                                    dataViewCategorie[0].getColor_hex()!=null? dataViewCategorie[0].getColor_hex():"#cfe2ed");
                        }
                    });
        }
    }

}
