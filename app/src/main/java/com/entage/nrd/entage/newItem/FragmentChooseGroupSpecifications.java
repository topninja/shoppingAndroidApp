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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CustomAdapterListView_SavedGroupSpecifications;
import com.entage.nrd.entage.utilities_1.SpecificationsItemList;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentChooseGroupSpecifications extends Fragment {
    private static final String TAG = "FragmentChooseGroupSp";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view ;

    private ImageView delete;
    private ListView listView;
    private AutoCompleteTextView mSearch;


    private ArrayAdapter<String> listView_Adapter;
    private CustomAdapterListView_SavedGroupSpecifications savedDescriptionsAdapter;
    private ArrayAdapter<String> autoCompleteText_Adapter ;

    private ArrayList<String> nameSpecifications;
    private HashMap<String, ArrayList<DataSpecifications> > groupSpecifications;
    private int positionSelect ;
    private ArrayList<DataSpecifications> selectedGroupSpecifications;
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

        setupGroupSpecifications();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.choose_group__specifications));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        mSearch = view.findViewById(R.id.autoCompleteText);
        listView = view.findViewById(R.id.listView);
        delete = view.findViewById(R.id.delete);
    }

    private void onClickListener() {

    }

    private void setupListView() {
        ArrayList<ArrayList<DataSpecifications>> listsDescriptions = new ArrayList<>();
        for(String name : nameSpecifications){
            listsDescriptions.add(groupSpecifications.get(name));
        }

        listView.setVisibility(View.VISIBLE);
        savedDescriptionsAdapter =
                new CustomAdapterListView_SavedGroupSpecifications(mContext, R.layout.custom_list_saved_descriptions_2,
                        nameSpecifications, entagePageId, listsDescriptions, view);
        listView.setAdapter(savedDescriptionsAdapter);

        autoCompleteText_Adapter = new ArrayAdapter<String> (mContext,android.R.layout.simple_list_item_1, nameSpecifications);
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

    private void setupGroupSpecifications(){
        setProgressBarTop(true, true, false, false);

        setupSpecificationsList();

        nameSpecifications = new ArrayList<>();
        groupSpecifications = new HashMap<>();

        // we have 2 groups in array file
        ArrayList<String[]> allGroups = new ArrayList<>();
        allGroups.add(mContext.getResources().getStringArray(R.array.group_specifications_phones));
        allGroups.add(mContext.getResources().getStringArray(R.array.group_specifications_food));
        allGroups.add(mContext.getResources().getStringArray(R.array.group_specifications_pc));

        // the group names in index 0 .
        for(int i=0; i<allGroups.size(); i++){
            nameSpecifications.add(allGroups.get(i)[0]);

            ArrayList<DataSpecifications> arrayList = new ArrayList<>();
            for(int x=1; x<allGroups.get(i).length ; x++){ // start from 1, index 0 for name
                String item = (SpecificationsItemList.getSpecificationsById(allGroups.get(i)[x]));
                arrayList.add(new DataSpecifications( allGroups.get(i)[x]
                        , item , item));
            }
            groupSpecifications.put(nameSpecifications.get(i), arrayList);
        }


        setupListView();

        setProgressBarTop(false, true, false, true);
    }

    private void setupSpecificationsList(){
        SpecificationsItemList.setSpecifications_keys(mContext.getResources().getStringArray(R.array.specifications_keys));
        SpecificationsItemList.setSpecifications(mContext.getResources().getStringArray(R.array.specifications));
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
                selectedGroupSpecifications = groupSpecifications.get(nameSpecifications.get(positionSelect));

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
