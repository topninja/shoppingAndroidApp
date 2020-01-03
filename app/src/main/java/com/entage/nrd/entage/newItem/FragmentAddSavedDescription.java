package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.DescriptionItem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CustomAdapterListView_SavedDescriptions;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FragmentAddSavedDescription extends Fragment {
    private static final String TAG = "FragmentAddSavedDes";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view ;

    private ListView listView;
    private AutoCompleteTextView mSearch;

    private ArrayList<String> nameSavedDescriptions;
    private ArrayAdapter<String> listView_Adapter;
    private CustomAdapterListView_SavedDescriptions savedDescriptionsAdapter;
    private ArrayAdapter<String> autoCompleteText_Adapter ;

    private HashMap<String, DescriptionItem> savedDataDescriptions;
    private DescriptionItem selectedDataDescriptions;
    private int positionSelect ;
    private GlobalVariable globalVariable;

    private String entagePageId, itemId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add__saved_descriptions , container , false);
        mContext = getActivity();

        getIncomingBundle();
        inti();

        return view;
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

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti() {
        initWidgets();
        onClickListener();

        if(globalVariable.getSavedDataDescriptions() == null){
            getSavedDescriptions();
        }else {
            savedDataDescriptions = globalVariable.getSavedDataDescriptions();
            setupListView();
        }
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.add_saved_description));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        mSearch = view.findViewById(R.id.autoCompleteText);
        listView = view.findViewById(R.id.listView);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener() {
    }

    private void setupListView() {
        nameSavedDescriptions = new ArrayList<>();
        List<DescriptionItem> listsDescriptions = new ArrayList<>();
        for(Map.Entry<String, DescriptionItem> entry : savedDataDescriptions.entrySet()) {
            String name = entry.getKey();
            //List<TypeDataDescription> data = entry.getValue();
            listsDescriptions.add(entry.getValue());
            nameSavedDescriptions.add(name);
        }

        listView.setVisibility(View.VISIBLE);

        savedDescriptionsAdapter =
                new CustomAdapterListView_SavedDescriptions(mContext, R.layout.custom_list_saved_descriptions,
                        nameSavedDescriptions, entagePageId, listsDescriptions, view);
        listView.setAdapter(savedDescriptionsAdapter);

        autoCompleteText_Adapter = new ArrayAdapter<String> (mContext,android.R.layout.simple_list_item_1, nameSavedDescriptions);
        mSearch.setAdapter(autoCompleteText_Adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                positionSelect= position;
                selectSavedDescriptions();
            }
        });

        mSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // HideSoftInputFromWindow
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View v = ((Activity)mContext).getCurrentFocus();
                if (v == null) {
                    v = new View(mContext);
                }
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                Object item = parent.getItemAtPosition(position);
                positionSelect = savedDescriptionsAdapter.getPosition((String)item);

                selectSavedDescriptions();

                mSearch.setText("");

            }
        });

        mSearch.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                mSearch.showDropDown();
                return false;
            }
        });

    }

    private void selectSavedDescriptions(){

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        final ListView listView = _view.findViewById(R.id.listView);
        _view.findViewById(R.id.text_view1).setVisibility(View.GONE);
        final String[] arrayList =  mContext.getResources().getStringArray(R.array.select_saved_description);

        listView.setVisibility(View.VISIBLE);
        listView_Adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1 , arrayList);
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
                selectedDataDescriptions = savedDataDescriptions.get(nameSavedDescriptions.get(positionSelect));
                if (position == 0){
                    mOnActivityDataItemListener.setSavedDescriptions(selectedDataDescriptions, false);

                }else if(position == 1){
                    mOnActivityDataItemListener.setSavedDescriptions(selectedDataDescriptions, true);
                }

                alert.dismiss();

                getActivity().onBackPressed();
            }
        });
    }

    private void setProgressBarTop(boolean visibility, boolean isLoading, boolean isSaving, boolean isSuccess){
        final LinearLayout layout = view.findViewById(R.id.wait_loading_layout);
        if(visibility){
            if(isLoading){
                ((TextView)view.findViewById(R.id.text_wait)).setText(mContext.getString(R.string.waite_to_loading));
            }
            if(isSaving){
                ((TextView)view.findViewById(R.id.text_wait)).setText(mContext.getString(R.string.waite_to_save));
            }
            view.findViewById(R.id.relLayout_save_successfully).setVisibility(View.GONE);
            view.findViewById(R.id.relLayout_happened_wrong).setVisibility(View.GONE);

            view.findViewById(R.id.relLayout_save_wait).setVisibility(View.VISIBLE);
            UtilitiesMethods.expand(layout);

        }else {
            int delay = 1000;
            if(isSaving){
                view.findViewById(R.id.relLayout_save_wait).setVisibility(View.GONE);
                if(isSuccess){
                    view.findViewById(R.id.relLayout_save_successfully).setVisibility(View.VISIBLE);
                }else {
                    delay = 5000;
                    view.findViewById(R.id.relLayout_happened_wrong).setVisibility(View.VISIBLE);
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    UtilitiesMethods.collapse(layout);
                }
            }, delay);
        }
    }

    private void getSavedDescriptions() {

        setProgressBarTop(true, true, false, false);

        // get images
        Query query1 = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_description));
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    savedDataDescriptions = new HashMap<>();
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        String name = singleSnapshot.getKey();
                        DescriptionItem descriptionItem = singleSnapshot.getValue(DescriptionItem.class);
                        savedDataDescriptions.put(name, descriptionItem);
                    }

                    globalVariable.setSavedDataDescriptions(savedDataDescriptions);

                    setProgressBarTop(false, true, false, true);
                    setupListView();

                }else {
                    setProgressBarTop(false, true, false, true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
                Toast.makeText(mContext,  getString(R.string.error_internet) ,
                        Toast.LENGTH_SHORT).show();
                setProgressBarTop(false, true, false, false);
            }
        });

    }


}
