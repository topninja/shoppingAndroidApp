package com.entage.nrd.entage.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.Utilities.SectionsStatePagerAdapter;
import com.entage.nrd.entage.adapters.AdapterAutoCompleteText;
import com.entage.nrd.entage.adapters.AdapterBubbleText;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.CategoriesSearchingAdapter;
import com.entage.nrd.entage.utilities_1.CategoriesSelectedViewAdapter;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;


public class FragmentSearch extends Fragment {
    private static final String TAG = "FragmentSearch";

    private Context mContext ;
    private View view;

    private InputMethodManager imm;
    private String userId;
    private DatabaseReference databaseReference;

    private RelativeLayout optionsSort, optionsSortArrow, layout_switch_searching;
    private TextView search, showAllCategories;
    private ImageView sort, filter, back;
    private ImageView moveToTabs;
    private AutoCompleteTextView textSearch;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SectionsStatePagerAdapter adapterViewPager;
    private View dialogView;
    public boolean isSortLabelOpen = false;
    private int pixels ;

    private RecyclerView recyclerViewBubbleText;
    private AdapterBubbleText adapterBubbleText;
    private ArrayList<String> categoriesTexts;

    private CategorieWithChildren mainParent;
    private CategoriesSearchingAdapter categoriesAdapter;
    private AlertDialog.Builder builder;
    private AlertDialog alert;

    // searching
    private ArrayList<String> selectedCategoriesAlgoliaCode;
    private HashMap<Integer, String> namesResultTab;
    private HashMap<Integer, Integer> countsResultTab;
    private boolean searchingForItems = true;

    Client client ;
    private Index index;
    private String indexNameAtAlgolia;
    private ArrayList<String> categorie_path;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();

    private ArrayList<String>  lastSearched;
    private View.OnClickListener clickListenerDeleteListSearched;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        if(view == null){
            view = inflater.inflate(R.layout.fragment_main_search1, container , false);
            mContext = getActivity();
            globalVariable = ((GlobalVariable)mContext.getApplicationContext());

            getIncomingBundle();
            init();
        }

        if(categorie_path == null && mContext !=null){
            textSearch.showDropDown();
            textSearch.requestFocus();
            keyboard(true);
        }

        return view;
    }

    public void getIncomingBundle() {
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                categorie_path = bundle.getStringArrayList("facets");
                searchingForItems = bundle.getBoolean("entage_pages");

                Log.d(TAG, "getIncomingBundle: categorie_path: " +categorie_path.toString() );
                Log.d(TAG, "getIncomingBundle: searchingForItems: " +searchingForItems);

                searchingForItems = !searchingForItems;
               // Log.d(TAG, "getIncomingBundle: " + searchingForItems);
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        setupCategoriesTextsAdapter();
        onClickListener();

        setupAlgolia();

        setupViewPager();

        initImageLoader();

        if(globalVariable.getCategoriesLists() != null){
            mainParent = globalVariable.getCategoriesLists();
            showAllCategories.setEnabled(true);

            if(categorie_path!=null){
                if(!searchingForItems){
                    switching(!searchingForItems);
                }
            }

            searchingFromSelectedCategorie(categorie_path);

        }else {
            fetchCategoriesLists();
        }
    }

    private void initWidgets() {
        textSearch = view.findViewById(R.id.text_search);
        setupAdapter();

        layout_switch_searching =  view.findViewById(R.id.layout_switch_searching);
        back =  view.findViewById(R.id.back);
        showButtons();

        showAllCategories =  view.findViewById(R.id.show_all_categories);
        showAllCategories.setEnabled(false);

        optionsSort =  view.findViewById(R.id.relLayout_search_sort);
        optionsSortArrow =  view.findViewById(R.id.sort_options_arrow);
        collapse(optionsSort);

        sort =  view.findViewById(R.id.sort);
        filter =  view.findViewById(R.id.filter);

        search =  view.findViewById(R.id.go_search);

        moveToTabs =  view.findViewById(R.id.moveToTabs);
        moveToTabs.setVisibility(View.GONE);

        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tabs);
        view.findViewById(R.id.move_to_another_categorie).setVisibility(View.GONE);

        selectedCategoriesAlgoliaCode = new ArrayList<>();
        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        pixels = (int) (26 * mContext.getResources().getDisplayMetrics().density);

    }

    private void onClickListener() {
        optionsSortArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(true);
                filterLabel(false);
            }
        });

        moveToTabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogListMoveToTabs();
            }
        });

        showAllCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogCategories();
            }
        });
        view.findViewById(R.id.text_main_categorie).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(false);

                String text = textSearch.getText().toString();
                if(text.length()> 0){
                    text = StringManipulation.removeLastSpace(textSearch.getText().toString());
                }
                initSearching(text);
            }
        });

        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSortLabelOpen){
                    filterLabel(false);

                }else {
                    filterLabel(true);
                }
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,  mContext.getString(R.string.will_be_available_in_next_update) ,
                        Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(false);
                getActivity().onBackPressed();
            }
        });

        layout_switch_searching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switching(searchingForItems);
                searchingForItems = !searchingForItems;
            }
        });

        textSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    keyboard(false);

                    String text = textSearch.getText().toString();
                    if(text.length()> 0){
                        text = StringManipulation.removeLastSpace(textSearch.getText().toString());
                    }
                    initSearching(text);

                    return true;
                }
                return false;
            }
        });

        textSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textSearch.getText().length() == 0){
                    if(isAdded() && mContext != null){
                        //textSearch.showDropDown();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) { }
        });

        textSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textSearch.showDropDown();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            public void onPageSelected(int position) {
                adapterBubbleText.setSelected(position);
                recyclerViewBubbleText.scrollToPosition(position);
            }
        });
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void setupAdapter(){
        lastSearched = new ArrayList<>();
        // get last four search
        String _0 = mContext.getSharedPreferences("entaji_app",
                MODE_PRIVATE).getString("lastSearched_0", null);
        String _1 = mContext.getSharedPreferences("entaji_app",
                MODE_PRIVATE).getString("lastSearched_1", null);
        String _2 = mContext.getSharedPreferences("entaji_app",
                MODE_PRIVATE).getString("lastSearched_2", null);
        String _3 = mContext.getSharedPreferences("entaji_app",
                MODE_PRIVATE).getString("lastSearched_3", null);
        if(_0 != null){
            lastSearched.add(_0);
        }
        if(_1 != null){
            lastSearched.add(_1);
        }
        if(_2 != null){
            lastSearched.add(_2);
        }
        if(_3 != null){
            lastSearched.add(_3);
        }
        //

        clickListenerDeleteListSearched = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                if(textView.getText().toString().equals(mContext.getString(R.string.delete_list_searched))){
                    for(int i=0; i<lastSearched.size() ; i++){
                        mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                                .remove("lastSearched_" + i).apply();
                    }

                    lastSearched.clear();
                    textSearch.setAdapter( new AdapterAutoCompleteText(mContext,
                            R.layout.layout_auto_complete_text_searching, new ArrayList<String>(), clickListenerDeleteListSearched));
                    textSearch.dismissDropDown();
                }
            }
        };

        ArrayList<String> arrayList = new ArrayList<>(lastSearched);
        Collections.reverse(arrayList);
        textSearch.setAdapter( new AdapterAutoCompleteText(mContext,
                R.layout.layout_auto_complete_text_searching, arrayList, clickListenerDeleteListSearched));
        textSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));

                keyboard(false);
                initSearching((String) parent.getItemAtPosition(position));

            }
        });
        textSearch.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                textSearch.showDropDown();
                return false;
            }
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbname_movement_tracking));
    }

    private void addTextSearch(String text){
        int index = lastSearched.size();
        lastSearched.add(index, text);
        if(index<=3) {
            mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                    .putString("lastSearched_" + index, text).apply();
        }else {
            lastSearched.remove(0);
            for(int i=0; i<lastSearched.size() ; i++){
                mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                        .putString("lastSearched_" + i, lastSearched.get(i)).apply();
            }
        }

        ArrayList<String> arrayList = new ArrayList<>(lastSearched);
        Collections.reverse(arrayList);
        textSearch.setAdapter( new AdapterAutoCompleteText(mContext,
                R.layout.layout_auto_complete_text_searching, arrayList, clickListenerDeleteListSearched));

        FirebaseMethods.setItems_on_views(mContext, databaseReference, mContext.getString(R.string.mt_users_search), text, userId);
    }

    private void setupAlgolia(){
        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        client = new Client(globalVariable.getApplicationID(), globalVariable.getAPIKey());
    }

    //
    private void initSearching(String text){
        Log.d(TAG, "initSearching: " + searchingForItems);
        filterLabel(false);

        text = text.replace("\n", "");
        text = StringManipulation.removeLastSpace(text);
        text = StringManipulation.replaceSomeCharsToSpace(text);
        //String digit = text.replaceAll("\\D+","");

        if(text.length() == 1 || text.length() > 40 || (text.length()>0 && TextUtils.isDigitsOnly(text))){
            messageDialog.errorMessage(mContext, mContext.getString(R.string.error_text_search));
            return;
        }

        if(selectedCategoriesAlgoliaCode.size() > 0){
            clear();

            ArrayList<String> selectedCategories = categoriesAdapter.getSelectedCategories();
            StringBuilder selectedCategoriesText = new StringBuilder();
            for(int i=0; i<selectedCategories.size(); i++){
                selectedCategoriesText.append(CategoriesItemList.getCategories_name(selectedCategories.get(i)));
                if(i+1 < selectedCategories.size()){
                    selectedCategoriesText.append(", ");
                }
            }

            //
            if(text.length() == 0){ // user search without text and choose categories
                // setTextOfResult
                setTitleSearch("<b>"+ (selectedCategories.size()==1 ? mContext.getString(R.string.search_in_categorie)
                        : mContext.getString(R.string.search_in_categories))+"</b>"+" "+
                        selectedCategoriesText);

                ArrayList<String> selectedCategoriesToDb = categoriesAdapter.getSelectedCategoriesToDb();
                for(String categoriesPaths : selectedCategoriesToDb){
                    ArrayList<String> arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(categoriesPaths);

                    //Log.d(TAG, "initSearching: " + listToString(arrayList, "/"));
                    String nameTab = CategoriesItemList.getCategories_name(arrayList.get(arrayList.size()-1));
                    String facet = StringManipulation.listToString(arrayList, "/").substring(1);
                    addFragmentViewResult(nameTab, -1, facet, null, text);
                }

            }else { // user search with text and choose categories
                addTextSearch(text);

                // setTitleSearch
                setTitleSearch("<b>"+mContext.getString(R.string.result_searching_about)+"</b>"+" "+ text + " "
                         + "<b>"+ (selectedCategories.size()==1 ? mContext.getString(R.string.in_categorie)
                                : mContext.getString(R.string.in_categories))+"</b>"+" "+  selectedCategoriesText);

                for(String facetText : selectedCategoriesAlgoliaCode){

                    String code = CategoriesItemList.getCategories_algolia_codes().get(facetText);

                    String nameTab = CategoriesItemList.getCategories_name(code);
                    String facet = "";
                    if(mainParent.getChildren().contains(code)){
                        facet = "categorie_level_1";
                        facetText = code;
                    }else {
                        facet = "categorie_level_2";
                    }

                    addFragmentViewResult(nameTab, -1, facet, facetText, text);
                }
            }

        }else {
            if(text.length()>0){  // user search with text and without choose any categories
                clear();

                addTextSearch(text);

                // setTitleSearch
                setTitleSearch("<b>"+mContext.getString(R.string.result_searching_about)+"</b>"+" "+ text);

                // search in all categories
                addFragmentViewResult(mContext.getString(R.string.all_results), -1, null, null, text);

                if(searchingForItems){
                    searchInAllCategories(text, mainParent.getChildren());
                }

            }else {
                messageDialog.errorMessage(mContext, mContext.getString(R.string.error_searching));
            }
        }

    }

    private void clear(){
        //clear
        namesResultTab = new HashMap<>();
        countsResultTab = new HashMap<>();
        setupViewPager();
        int size = categoriesTexts.size();categoriesTexts.clear();
        adapterBubbleText.notifyItemRangeRemoved(0, size);

        //
        indexNameAtAlgolia = searchingForItems ? "entage_items" : "entage_pages";

        // clear this of selected categorie
        categorie_path = null;
    }

    private void searchInAllCategories(final String text, ArrayList<String> facets){
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(text);

        index = client.getIndex(indexNameAtAlgolia);
        for(final String facetText : facets){
            index.searchForFacetValuesAsync("categorie_level_1", facetText, query, new CompletionHandler() {
                @Override
                public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                    if(jsonObject != null){
                        //Log.d(TAG, "requestCompleted: " + facetText + ": "  + jsonObject.toString());
                        try {
                            JSONArray jsonArray = jsonObject.getJSONArray("facetHits");
                            int count = 0;
                            for(int i=0 ; i < jsonArray.length(); i++){
                                JSONObject result = (JSONObject)jsonArray.get(i);
                                String value = result.get("value").toString();
                                if(value.equals(facetText)){ // search for 03 and ignore 030
                                    count = (int) result.get("count");
                                }
                            }
                            if(count != 0){
                                String nameTab = CategoriesItemList.getCategories_name(facetText);
                                addFragmentViewResult(nameTab, count, "categorie_level_1", facetText, text);
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void addFragmentViewResult(String nameTab, int count, String facet, String facetText, String text){
        Bundle bundle = new Bundle();
        bundle.putString("facet", facet);
        bundle.putString("facetText", facetText);
        bundle.putString("text", text);
        bundle.putString("indexNameAtAlgolia", indexNameAtAlgolia);
        bundle.putBoolean("searchingForItems", searchingForItems);

        ViewResultSearchingFragment viewResultSearchingFragment = new ViewResultSearchingFragment();
        viewResultSearchingFragment.setArguments(bundle);
        adapterViewPager.addFragment(viewResultSearchingFragment, nameTab);
        adapterViewPager.notifyDataSetChanged();

        /*int index = tabLayout.getTabCount()-1;
        namesResultTab.put(index, nameTab);
        countsResultTab.put(index, count);

        for(int i=0 ; i<tabLayout.getTabCount(); i++){
            if(countsResultTab.get(i) == -1){
                tabLayout.getTabAt(i).setText(namesResultTab.get(i));
            }else {
                tabLayout.getTabAt(i).setText(namesResultTab.get(i)+" ("+countsResultTab.get(i)+")");
            }
        }*/

        //
        int pos = categoriesTexts.size();
        categoriesTexts.add(pos, nameTab);
        if(pos == 0){
            adapterBubbleText.setSelected(0);
        }
        adapterBubbleText.notifyItemInserted(pos);

        /*if(tabLayout.getTabCount()>2){
            //moveToTabs.setVisibility(View.VISIBLE);
        }*/


    }

    private void setTitleSearch(String text){
        ((TextView)view.findViewById(R.id.text_main_categorie))
                .setText(Html.fromHtml(text));
    }

    private void openDialogListMoveToTabs(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        final ListView listView = _view.findViewById(R.id.listView);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.categories_of_results));
        ((TextView)_view.findViewById(R.id.text_view1)).setTextSize(20);

        ArrayList<String> arrayList = new ArrayList<>();
        for(int i=0 ; i<tabLayout.getTabCount(); i++){
            if(i == 0){
                arrayList.add(namesResultTab.get(i));

            }else {
                if(countsResultTab.get(i) != -1){
                    arrayList.add(namesResultTab.get(i) + "  (" + countsResultTab.get(i) + ")");
                }else {
                    arrayList.add(namesResultTab.get(i));
                }
            }
        }

        listView.setVisibility(View.VISIBLE);
        ArrayAdapter<String> listView_Adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1 , arrayList);
        listView.setAdapter(listView_Adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
                alert.dismiss();
            }
        });
    }

    private void setupCategoriesTextsAdapter(){
        recyclerViewBubbleText =  view.findViewById(R.id.recyclerView_bubble_text);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int tag = (int) v.getTag();
                if(tag != viewPager.getCurrentItem()){
                    v.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_bubble_text_1));
                    adapterBubbleText.setSelected(tag);
                    viewPager.setCurrentItem(tag);
                    recyclerViewBubbleText.scrollToPosition(tag);
                }
            }
        };
        categoriesTexts = new ArrayList<>();
        LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        adapterBubbleText = new AdapterBubbleText(mContext, categoriesTexts, onClickListener);
        recyclerViewBubbleText.setNestedScrollingEnabled(false);
        recyclerViewBubbleText.setHasFixedSize(true);
        recyclerViewBubbleText.setLayoutManager(linearLayoutManager);
        recyclerViewBubbleText.setAdapter(adapterBubbleText);
    }

    //
    private void searchingFromSelectedCategorie(ArrayList<String> categorie_path){
        if(categorie_path != null){
            String mainCode = categorie_path.get(0);
            String targetCategorieCode = categorie_path.get(categorie_path.size()-1);
            if(mainParent.getCategorieWithChildren().containsKey(mainCode)){

                keyboard(false);
                filterLabel(false);

                //clear
                namesResultTab = new HashMap<>();
                countsResultTab = new HashMap<>();
                setupViewPager();

                // setTitleSearch
                setTitleSearch("<b>"+mContext.getString(R.string.search_in_categorie)+"</b>"+" "+
                        CategoriesItemList.getCategories_name(mainCode) + "/" + CategoriesItemList.getCategories_name(targetCategorieCode));

                //
                indexNameAtAlgolia = searchingForItems ? "entage_items" : "entage_pages";

                String nameTab = CategoriesItemList.getCategories_name(targetCategorieCode);
                String facet = StringManipulation.listToString(categorie_path, "/").substring(1);
                addFragmentViewResult(nameTab, -1, facet, null, "");

                targetCategorieCode = null;
                CategorieWithChildren targetCategorie = globalVariable.getCategoriesLists();
                for(int i=0 ; i<categorie_path.size(); i++){
                    if(targetCategorie.getCategorieWithChildren().containsKey(categorie_path.get(i))){
                        targetCategorie = targetCategorie.getCategorieWithChildren().get(categorie_path.get(i));
                    }else {
                        targetCategorieCode = categorie_path.get(i);
                    }
                }

                if(targetCategorieCode == null){ // target has children
                    // get all children of our categorie
                    getChild_addResult(targetCategorie);
                }
            }
        }
    }

    private void getChild_addResult(CategorieWithChildren categorieWithChildren){
        for(String child : categorieWithChildren.getChildren()){
            categorieWithChildren.getPathOfChild(child);

            String nameTab = CategoriesItemList.getCategories_name(child);
            String facet = StringManipulation.listToString(categorieWithChildren.getPathOfChild(child), "/").substring(1);
            addFragmentViewResult(nameTab, -1, facet, null, "");
        }


        ArrayList<String> arrayList = new ArrayList<>(categorieWithChildren.getCategorieWithChildren().keySet());
        for(String child : arrayList){
            getChild_addResult(categorieWithChildren.getCategorieWithChildren().get(child));
        }
    }

    //
    private void setupViewPager(){
        //
        if(adapterViewPager != null){
            for(int i=0; i<adapterViewPager.getCount(); i++){
                getFragmentManager().beginTransaction().remove(adapterViewPager.getItem(i)).commit();
                adapterViewPager.notifyDataSetChanged();
                tabLayout.removeTabAt(i);
            }
            viewPager.removeAllViews();
            moveToTabs.setVisibility(View.GONE);
            viewPager.requestLayout();
        }


        //
        adapterViewPager = new SectionsStatePagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter
        viewPager.setAdapter(adapterViewPager);
        tabLayout.setupWithViewPager(viewPager);
        //tabLayout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    }

    private void openDialogCategories() {
        if(dialogView == null){
            dialogView = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
            ((TextView)dialogView.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.the_categories));
            ((TextView)dialogView.findViewById(R.id.text_view1)).setTextSize(20);
            dialogView.findViewById(R.id.relLayout_categories).setVisibility(View.VISIBLE);

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
    }

    private void switching(boolean items){
        if(items){
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pixels, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            view.findViewById(R.id.s0).setLayoutParams(layoutParams); //causes layout update

            layout_switch_searching.getChildAt(1).setVisibility(View.GONE);
            layout_switch_searching.getChildAt(2).setVisibility(View.VISIBLE);

        }else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pixels, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            view.findViewById(R.id.s0).setLayoutParams(layoutParams); //causes layout update

            layout_switch_searching.getChildAt(2).setVisibility(View.GONE);
            layout_switch_searching.getChildAt(1).setVisibility(View.VISIBLE);
        }
    }

    //
    private void filterLabel(boolean boo){
        if(boo){
            if(!isSortLabelOpen){
                sort.setImageResource(R.drawable.ic_sort_selected);
                isSortLabelOpen = true;
                optionsSortArrow.setVisibility(View.VISIBLE);
                expand(optionsSort);
                keyboard(false);
            }
        }else {
            if(isSortLabelOpen){
                if(adapterBubbleText != null){
                    adapterBubbleText.notifyDataSetChanged();
                }
                sort.setImageResource(R.drawable.ic_sort);
                isSortLabelOpen = false;
                optionsSortArrow.setVisibility(View.GONE);
                collapse(optionsSort);
            }
        }
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

    private void showButtons(){
        TransitionManager.beginDelayedTransition(layout_switch_searching, new TransitionSet()
                .addTransition(new Slide()));
        TransitionManager.beginDelayedTransition((ViewGroup) view.findViewById(R.id.relLayout_1), new TransitionSet()
                .addTransition(new Fade()));

        view.findViewById(R.id.home).setVisibility(View.GONE);
        layout_switch_searching.setVisibility(View.VISIBLE);
        view.findViewById(R.id.personal).setVisibility(View.GONE);
        back.setVisibility(View.VISIBLE);
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
                showAllCategories.setEnabled(true);

                searchingFromSelectedCategorie(categorie_path);
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


    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    public void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

}
