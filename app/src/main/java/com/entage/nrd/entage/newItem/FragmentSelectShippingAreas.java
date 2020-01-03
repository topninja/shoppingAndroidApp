package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterChooseAreas;
import com.entage.nrd.entage.utilities_1.CountriesData;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.geonames.FeatureClass;
import org.geonames.InvalidParameterException;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;



public class FragmentSelectShippingAreas extends Fragment {
    private static final String TAG = "FragmentSelectShippingAreas";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view;
    private InputMethodManager imm;

    private TextView getCities, chooseAreas;
    private CheckBox showSelectedAreas;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewItems;
    private Spinner typeSearchSpinner;
    private AutoCompleteTextView  autoCompleteTextViewCountries;
    private EditText searchForCities ;

    private String selectCountry = null  ;
    private int positionTypeSearch ;
    private ArrayList<AreaShippingAvailable> countries;
    private View.OnClickListener onClickListenerArea, onClickListenerFreeShipping;
    private AdapterView.OnItemSelectedListener onShoppingCompanySelected;
    private ArrayList<AreaShippingAvailable> resultsDataSearchAreas, savedSearchContinents;
    private HashMap<String, HashMap<String, AreaShippingAvailable>> myAreaShippingAvailable;
    private AdapterChooseAreas chooseAreasAdapter;

    private MessageDialog messageDialog = new MessageDialog();
    private String lang ;
    private GlobalVariable globalVariable;

    private String entagePageId, itemId, entagePageName;

    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_select_places, container , false);
        mContext = getActivity();

        getIncomingBundle();
        inti();

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
                getEntagePageName();
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void inti() {
        initWidgets();
        setupAdapters();
        onClickListener();

        myAreaShippingAvailable = mOnActivityDataItemListener.getAreaShippingAvailable();

        setupRecyclerView();

        showSelectedAreas.setChecked(true);
        if(myAreaShippingAvailable.size() > 0){
            setSelectedAreas(myAreaShippingAvailable);
        }
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.select_places_shipping_available));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        typeSearchSpinner = view.findViewById(R.id.type_search);
        autoCompleteTextViewCountries = view.findViewById(R.id.input_country);
        getCities = view.findViewById(R.id.get_cities);
        searchForCities = view.findViewById(R.id.search_for_cities);
        showSelectedAreas = view.findViewById(R.id.show_selected_areas);
        chooseAreas = view.findViewById(R.id.choose_areas);
        progressBar = view.findViewById(R.id.progressBar);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        if(globalVariable.getLanguage() != null){
            lang = globalVariable.getLanguage();
        }else {
            lang = Locale.getDefault().getLanguage();
        }
    }

    private void onClickListener() {
        getCities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });

        searchForCities.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    startSearch();
                    return true;
                }
                return false;
            }
        });

        showSelectedAreas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAll();
                setSelectedAreas(myAreaShippingAvailable);
            }
        });

        typeSearchSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectSpinner(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setSelectedAreas(HashMap<String, HashMap<String, AreaShippingAvailable>>  areas){
        showSelectedAreas.setClickable(false);

        resultsDataSearchAreas = new ArrayList<>();
        Set<String> keySet = areas.keySet();
        for(String countryCodes : keySet ){
            for(Map.Entry<String, AreaShippingAvailable> areaShippingAvailable : areas.get(countryCodes).entrySet()){
                resultsDataSearchAreas.add(areaShippingAvailable.getValue());
            }
        }

        if(resultsDataSearchAreas.size() == 0){
            view.findViewById(R.id.no_areas).setVisibility(View.VISIBLE);
        }else {
            view.findViewById(R.id.no_areas).setVisibility(View.GONE);
        }

        setAdapter();
        typeSearchSpinner.setSelection(0);
    }

    private void setupRecyclerView(){
        recyclerViewItems =  view.findViewById(R.id.recyclerView);
        recyclerViewItems.setHasFixedSize(false);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));

        onClickListenerArea = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerViewItems.getChildLayoutPosition((View) v.getParent());
                AreaShippingAvailable area = resultsDataSearchAreas.get(itemPosition);
                if(!checkIfAreaExist(area)){
                    addArea(area, itemPosition);
                }else {
                    removeArea(area, itemPosition);
                }
            }
        };



        /*onClickListenerFreeShipping  = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerViewItems.getChildLayoutPosition((View) v.getParent().getParent());
                AreaShippingAvailable area = resultsDataSearchAreas.get(itemPosition);
                boolean freeShipping = myAreaShippingAvailable.get(area.getCountry_code()).get(area.getId_area()).isFree_shipping();
                myAreaShippingAvailable.get(area.getCountry_code()).get(area.getId_area()).setFree_shipping(!freeShipping);
                chooseAreasAdapter.notifyItemChanged(itemPosition);
            }
        };*/

        /*final String[] shippingCompanies = mContext.getResources().getStringArray(R.array.shipping_companies);
        onShoppingCompanySelected = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = recyclerViewItems.getChildLayoutPosition((View) parent.getParent().getParent().getParent());
                AreaShippingAvailable area = resultsDataSearchAreas.get(itemPosition);
                Object item = parent.getItemAtPosition(position);
                myAreaShippingAvailable.get(area.getCountry_code()).get(area.getId_area()).setShipping_company(shippingCompanies[position]);
                chooseAreasAdapter.notifyItemChanged(itemPosition);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };*/

    }

    private void setupAdapters() {
        // Adapter Countries
        ArrayList<String> countriesNames = CountriesData.getCountriesNames(null);
        countries = new ArrayList<>();
        for (String cn: countriesNames) {
            countries.add(new AreaShippingAvailable("", "", cn,
                    CountriesData.getCountryId(cn), CountriesData.getCountryCode(cn) ,false,false,
                    null, "", false,  "", false, false));
        }

        ArrayAdapter<String> autoCompleteText_AdapterCountries = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, countriesNames);
        autoCompleteTextViewCountries.setAdapter(autoCompleteText_AdapterCountries);
        autoCompleteTextViewCountries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));
                hideKeyboard();

                Object item = parent.getItemAtPosition(position);
                selectCountry = item.toString();
            }
        });

        autoCompleteTextViewCountries.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                autoCompleteTextViewCountries.showDropDown();
                return false;
            }
        });
        // END
    }

    private void addArea(AreaShippingAvailable area, int itemPosition){
        if(!myAreaShippingAvailable.containsKey(area.getCountry_code())){
            myAreaShippingAvailable.put(area.getCountry_code(), new HashMap<String, AreaShippingAvailable>());
        }
        myAreaShippingAvailable.get(area.getCountry_code()).put(area.getId_area(), area);

        chooseAreasAdapter.notifyItemChanged(itemPosition);
        recyclerViewItems.scrollToPosition(itemPosition);
    }

    private void removeArea(AreaShippingAvailable area, int itemPosition){
        myAreaShippingAvailable.get(area.getCountry_code()).remove(area.getId_area());

        if(myAreaShippingAvailable.get(area.getCountry_code()).size()==0){
            myAreaShippingAvailable.remove(area.getCountry_code());
        }

        chooseAreasAdapter.notifyItemChanged(itemPosition);
    }

    private boolean checkIfAreaExist(AreaShippingAvailable area) {
        return myAreaShippingAvailable.containsKey(area.getCountry_code())
                && myAreaShippingAvailable.get(area.getCountry_code()).containsKey(area.getId_area());
    }

    private void setAdapter(){
        chooseAreasAdapter = new AdapterChooseAreas(mContext,recyclerViewItems, resultsDataSearchAreas,
                onClickListenerArea, myAreaShippingAvailable, entagePageName);
        recyclerViewItems.setAdapter(chooseAreasAdapter);
    }

    private void getEntagePageName() {
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entaji_pages_names))
                .child(entagePageId)
                .child("name_entage_page");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    entagePageName = (String) dataSnapshot.getValue();

                }else {
                    entagePageName = mContext.getString(R.string.personal_shipping);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                entagePageName = mContext.getString(R.string.personal_shipping);
            }
        });
    }

    private void startSearch(){
        if (selectCountry != null){
            hideKeyboard();
            if (typeSearchSpinner.getSelectedItemPosition() == 3){ // Search for states/regions/areas
                GeonamesSearch  geonamesSearc = new GeonamesSearch();
                geonamesSearc.startSearch(mContext.getString(R.string.search_for_states),"", CountriesData.getCountryCode(selectCountry),
                        "A", "ADM1");

            }else if (typeSearchSpinner.getSelectedItemPosition() == 4){ // Search for cities
                if (searchForCities.getText().toString().length() != 0){
                    String text = StringManipulation.removeLastSpace(searchForCities.getText().toString());
                    GeonamesSearch  geonamesSearc = new GeonamesSearch();
                    geonamesSearc.startSearch(mContext.getString(R.string.search_for_cities), text,
                            CountriesData.getCountryCode(selectCountry),
                            "P", "");
                }
            }
        }
    }

    // in back ground
    private void setResultList(ToponymSearchResult searchResult){
        if (!((Activity)mContext).isDestroyed()){
            resultsDataSearchAreas = new ArrayList<>();
            if (searchResult != null){
                for (Toponym t : searchResult.getToponyms())
                {
                    String idArea = String.valueOf(t.getGeoNameId());
                    String countryCode = t.getCountryCode();

                    if(countryCode.length()==0){
                        countryCode = "cont";
                    }
                    resultsDataSearchAreas.add(new AreaShippingAvailable("","",t.getName(), idArea
                            , countryCode, true,false, null, "",false, "", false, false));
                }

            }

            // search one time for CONT and save result
            if(resultsDataSearchAreas.size()>0 && resultsDataSearchAreas.get(0).getCountry_code().equals("cont")){
                savedSearchContinents = new ArrayList<>();
                savedSearchContinents.addAll(resultsDataSearchAreas);
            }

            setAdapter();
        }
    }
    //


    // DATABASE
    private void saveData(final boolean isSaveNext, final boolean isSaveExit){
        /*enableTopBarClick(false);
        setProgressBarTop(true, false, true, false);

        OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                enableTopBarClick(true);
                setProgressBarTop(false, false, true, true);
                doneSaveData(isSaveNext, isSaveExit);
            }
        };
        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                enableTopBarClick(true);
                setProgressBarTop(false, false, true, false);
                if(e.getMessage().equals("Firebase Database error: Permission denied")){
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_internet));
                }
            }
        };

        mItem.setArea_shipping_available(myAreaShippingAvailable);
        FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.dbname_entage_pages_archives)) // entage_page_archives
                .child(mItem.getEntage_page_id()) // Entage_page_id
                .child(mContext.getString(R.string.field_saved_items))
                .child(mItem.getItem_id()) // Item Id
                .setValue(mItem)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener) ;*/
    }

    private void saveDataInEditMode() {
        /*enableTopBarClick(false);
        setProgressBarTop(true, false, true, false);


        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_page_categories)) // entage_page_categories
                .child(mItem.getEntage_page_id()) // Entage_page_id
                .child(mItem.getItem_in_categorie()) // Categorie Name .child(newItem.getCategories_item().get(0))
                .child(mContext.getString(R.string.field_categorie_items)) // Categorie_items
                .child(mItem.getItem_id()) // Item Id
                .child(mContext.getString(R.string.field_area_shipping_available))
                .setValue(myAreaShippingAvailable)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // add to database
                        FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.dbname_items))
                                .child(mItem.getItem_id()) // Item Id
                                .child(mContext.getString(R.string.field_area_shipping_available))
                                .setValue(myAreaShippingAvailable)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        mItem.setArea_shipping_available(myAreaShippingAvailable);
                                        mEditItemListener.mItem(mItem);

                                        getActivity().onBackPressed();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        enableTopBarClick(true);
                        setProgressBarTop(false, false, true, false);
                        if(e.getMessage().equals("Firebase Database error: Permission denied")){
                            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                        }else {
                            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_internet));
                        }
                    }
                }) ;*/
    }

    private void hideKeyboard(){
        // HideSoftInputFromWindow
        View v = ((Activity)mContext).getCurrentFocus();
        if (v == null) {
            v = new View(mContext);
        }
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void removeAll(){
        autoCompleteTextViewCountries.setVisibility(View.GONE);
        getCities.setVisibility(View.GONE);
        searchForCities.setVisibility(View.GONE);
        chooseAreas.setVisibility(View.GONE);
        recyclerViewItems.setAdapter(null); // clean
    }

    private void selectSpinner(int position){
        if(position != 0){
            view.findViewById(R.id.no_areas).setVisibility(View.GONE);
            showSelectedAreas.setChecked(false);
            showSelectedAreas.setClickable(true);

            removeAll();

            hideKeyboard();

            if (position == 1){ // Search for conti// nents
                if(savedSearchContinents == null){
                    GeonamesSearch  geonamesSearc = new GeonamesSearch();
                    geonamesSearc.startSearch(mContext.getString(R.string.search_for_continents)
                            , "", "", "", "CONT");

                }else {
                    resultsDataSearchAreas = new ArrayList<>();
                    resultsDataSearchAreas.addAll(savedSearchContinents);
                    setAdapter();
                    recyclerViewItems.setAdapter(chooseAreasAdapter);
                }

            }else if (position == 2){ // Search for countries
                // already we have all countries
                resultsDataSearchAreas = new ArrayList<>();
                resultsDataSearchAreas.addAll(countries);
                setAdapter();
                recyclerViewItems.setAdapter(chooseAreasAdapter);

            }else if (position == 3){ // Search for states/regions/areas
                autoCompleteTextViewCountries.setVisibility(View.VISIBLE);
                getCities.setVisibility(View.VISIBLE);
                searchForCities.setVisibility(View.GONE);

            }else if (position == 4){ // Search for cities
                autoCompleteTextViewCountries.setVisibility(View.VISIBLE);
                getCities.setVisibility(View.VISIBLE);
                searchForCities.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Params : The type of the parameters sent to the task upon execution
     * Progress : The type of the progress units published during the background computation
     * Result : The type of the result of the background computation
     *
     * doInBackground() : This method contains the code which needs to be executed in background. In this method we can
     * send results multiple times to the UI thread by publishProgress() method. To notify that the background processing
     * has been completed we just need to use the return statements
     *
     * onPreExecute() : This method contains the code which is executed before the background processing starts
     *
     * onPostExecute() : This method is called after doInBackground method completes processing. Result from doInBackground
     * is passed to this method
     *
     * onProgressUpdate() : This method receives progress updates from doInBackground method, which is published via
     * publishProgress method, and this method can use this progress update to update the UI thread
     */
    public class GeonamesSearch extends AsyncTask<Integer, String, ToponymSearchResult> {
        /**
         * Params : The type of the parameters sent to the task upon execution
         * Progress : The type of the progress units published during the background computation
         * Result : The type of the result of the background computation
         *
         * doInBackground() : This method contains the code which needs to be executed in background. In this method we can
         * send results multiple times to the UI thread by publishProgress() method. To notify that the background processing
         * has been completed we just need to use the return statements
         *
         * onPreExecute() : This method contains the code which is executed before the background processing starts
         *
         * onPostExecute() : This method is called after doInBackground method completes processing. Result from doInBackground
         * is passed to this method
         *
         * onProgressUpdate() : This method receives progress updates from doInBackground method, which is published via
         * publishProgress method, and this method can use this progress update to update the UI thread
         */

        private ToponymSearchCriteria searchCriteria ;

        public GeonamesSearch() {
        }

        private void startSearch(String typeSearch, String nameStartsWith, String _area, String _featureClass, String _featureCode){
            // for more info http://www.geonames.org/export/codes.html

            // q: "cont"
            // FeatureClass: null
            // - FeatureCode: CONT: continents

            // country: _area
            // FeatureClass: A: country, state, region,...
            // - FeatureCode: ADM1: a primary administrative division of a country, such as a state in the United States

            // FeatureClass: P: city, village,...
            // - FeatureCode:


            // FeatureClass
            FeatureClass featureClass = null;
            if (_featureClass.equals("A")){
                featureClass = FeatureClass.A;

            } else if (_featureClass.equals("P")){
                featureClass = FeatureClass.P;
            }

            searchCriteria = new ToponymSearchCriteria();
            try {
                if(typeSearch.equals(mContext.getString(R.string.search_for_continents))){
                    //api.geonames.org/search?q=cont&username=entage&fcode=CONT&lang=ar
                    searchCriteria.setQ("cont");
                }
                else if(typeSearch.equals(mContext.getString(R.string.search_for_states))){
                    //api.geonames.org/search?country=US&username=entage&fclass=A&fcode=ADM1&lang=ar
                    searchCriteria.setCountryCode(_area);
                    searchCriteria.setFeatureClass(featureClass);
                }
                else if(typeSearch.equals(mContext.getString(R.string.search_for_cities))){
                    // api.geonames.org/search?name_startsWith=مك&country=sa&username=entage&featureClass=P&lang=ar
                    searchCriteria.setNameStartsWith(nameStartsWith);
                    searchCriteria.setCountryCode(_area);
                    searchCriteria.setFeatureClass(featureClass);
                }
                else {
                    searchCriteria.setNameStartsWith(nameStartsWith);
                    searchCriteria.setFeatureClass(featureClass);
                    searchCriteria.setStartRow(100);
                }

                searchCriteria.setFeatureCode(_featureCode);
                searchCriteria.setStyle(Style.SHORT);
                searchCriteria.setLanguage(lang);

                this.execute();
            } catch (InvalidParameterException e) {
                e.printStackTrace();
            }
        }

        /**
         *
         */
        @Override
        protected ToponymSearchResult doInBackground(Integer... integers) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()

            WebService.setUserName(globalVariable.GEONAMES_USERNAME);
            ToponymSearchResult searchResult = null;
            try {
                searchResult = WebService.search(searchCriteria);
                searchResult.setStyle(Style.SHORT);

            } catch (UnknownHostException e) {
                e.printStackTrace();
                //Toast.makeText(mContext, mContext.getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
                //messageDialog.errorMessage(mContext,mContext.getString(R.string.error_internet));
            } catch (Exception e) {
                e.printStackTrace();
                //Toast.makeText(mContext,e.getMessage(), Toast.LENGTH_SHORT).show();
                //messageDialog.errorMessage(mContext,e.getMessage());
            }
            return searchResult;
        }

        @Override
        protected void onPostExecute(ToponymSearchResult searchResult) {
            progressBar.setVisibility(View.GONE);
            setResultList(searchResult);
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... text) {
            // finalResult.setText(text[0]);
        }

    }

}
