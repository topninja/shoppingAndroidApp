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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CustomAdapterListView_SavedSpecifications;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentChooseSavedSpecifications extends Fragment {
    private static final String TAG = "FragmentChooseSavedSp";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view ;

    private ImageView delete;
    private ListView listView;
    private AutoCompleteTextView mSearch;


    private ArrayAdapter<String> listView_Adapter;
    private CustomAdapterListView_SavedSpecifications savedDescriptionsAdapter;
    private ArrayAdapter<String> autoCompleteText_Adapter ;

    private ArrayList<String> nameSpecifications;
    private ArrayList<ArrayList<DataSpecifications>> savedSpecifications;
    private int positionSelect ;
    private ArrayList<DataSpecifications> selectedGroupSpecifications;

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

    private void inti() {
        initWidgets();
        onClickListener();

        if(globalVariable.getMyGroupSpecifications() == null){
            getSavedSpecifications();

        }else {
            nameSpecifications = new ArrayList<>();
            savedSpecifications = new ArrayList<>();
            HashMap<String, ArrayList<DataSpecifications> > myGroupSpecifications = globalVariable.getMyGroupSpecifications();
            for(Map.Entry<String, ArrayList<DataSpecifications> > map : myGroupSpecifications.entrySet()){
                nameSpecifications.add(map.getKey());
                savedSpecifications.add(map.getValue());
            }
            setupListView();
        }
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.add_saved_description));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        mSearch = view.findViewById(R.id.autoCompleteText);
        listView = view.findViewById(R.id.listView);
        delete = view.findViewById(R.id.delete);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener() {
    }

    private void setupListView() {
        /*ArrayList<ArrayList<DataSpecifications>> listsDescriptions = new ArrayList<>();
        for(Map.Entry<String, ArrayList<DataSpecifications>> entry : savedSpecifications.entrySet()) {
            String name = entry.getKey();
            //List<TypeDataDescription> data = entry.getValue();
            listsDescriptions.add(entry.getValue());
        }*/

        listView.setVisibility(View.VISIBLE);


        savedDescriptionsAdapter =
                new CustomAdapterListView_SavedSpecifications(mContext, R.layout.custom_list_saved_descriptions,
                        nameSpecifications, entagePageId, savedSpecifications, view);
        listView.setAdapter(savedDescriptionsAdapter);

        autoCompleteText_Adapter = new ArrayAdapter<String> (mContext,android.R.layout.simple_list_item_1, nameSpecifications);
        mSearch.setAdapter(autoCompleteText_Adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                positionSelect = position;
                selectSavedSpecifications();
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

                selectSavedSpecifications();

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

    private void selectSavedSpecifications(){

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
                selectedGroupSpecifications = savedSpecifications.get(positionSelect);

                if (position == 0){
                   mOnActivityDataItemListener.setGroupSpecifications(selectedGroupSpecifications, false);
                }else if(position == 1){
                    mOnActivityDataItemListener.setGroupSpecifications(selectedGroupSpecifications, true);
                }

                alert.dismiss();

                getActivity().onBackPressed();
            }
        });


    }

    private void getSavedSpecifications() {
        setProgressBarTop(true, true, false, false);

        // get images
        Query query1 = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_group_specifications));
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    HashMap<String, ArrayList<DataSpecifications> > myGroupSpecifications = new HashMap<>();
                    nameSpecifications = new ArrayList<>();
                    savedSpecifications = new ArrayList<>();
                    //savedSpecifications = new HashMap<>();
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                        String key = singleSnapshot.getKey();
                        nameSpecifications.add(key);
                        ArrayList<DataSpecifications> specifications = new ArrayList<>();
                        for (DataSnapshot single: singleSnapshot.getChildren()) {
                            specifications.add(new DataSpecifications(single.child("specifications_id").getValue().toString()
                                    ,single.child("specifications").getValue().toString()
                                    ,single.child("data").getValue().toString()));
                        }
                        savedSpecifications.add(specifications);

                        myGroupSpecifications.put(key, specifications);
                    }

                    globalVariable.setMyGroupSpecifications(myGroupSpecifications);
                    setupListView();

                }

                setProgressBarTop(false, true, false, true);
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

}
