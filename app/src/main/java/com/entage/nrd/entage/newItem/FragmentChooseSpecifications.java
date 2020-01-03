package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.SpecificationsItemList;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class FragmentChooseSpecifications extends Fragment {
    private static final String TAG = "FragmentChooseSpecifica";

    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private ListView listView;
    private AutoCompleteTextView mSearchSpeci;
    private Button addNewSpeci, deleteSpeci;
    private ImageView mDeleteSpeci, cancel;

    //private String[] itemsSpeci;
    private ArrayList<String> itemsSpeci;
    private ArrayAdapter<String> listView_Adapter;
    private ArrayAdapter<String> autoCompleteText_Adapter ;
    private ArrayList<String> mySpecification;
    private ArrayList<String> deleteMySpecification;
    private String addedCustomizationText;
    private ListView listView_deleteSpeci;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();
    private ArrayList<String> selectedSinglesSpecifications;
    private String entagePageId, itemId;

    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_specifications, container , false);
        mContext = getActivity();

        setupFirebaseAuth();
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

        if(globalVariable.getMySinglesSpecifications() == null){
            getMySpecificationsFromDb();
        }else {
            mySpecification = globalVariable.getMySinglesSpecifications();
            setupListView();
            itemAlreadySelected();
        }

        //setupListView();
        //itemAlreadySelected();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.choose_specifications));
        mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);

        listView =  view.findViewById(R.id.listViewSpecifications);
        mSearchSpeci =  view.findViewById(R.id.autoCompleteTextSpeci);
        mDeleteSpeci =  view.findViewById(R.id.delete);
        addNewSpeci =  view.findViewById(R.id.button_add_new_speci);
        listView_deleteSpeci =  view.findViewById(R.id.listView_deleteSpeci);
        cancel = view.findViewById(R.id.cancel);
        deleteSpeci = view.findViewById(R.id.button_delete_speci);

        addedCustomizationText = mContext.getString(R.string.added_customization);

        selectedSinglesSpecifications = new ArrayList<>();

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener() {
        mDeleteSpeci.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {

                setupListViewDeleteSpeci();

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                cancelDeleteMode();
            }
        });
        deleteSpeci.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                if(deleteMySpecification != null && deleteMySpecification.size() > 0){
                    deleteSpecifications();
                }else {

                }
            }
        });
        addNewSpeci.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Add New Speci");
                addingNewSpecifications();
            }
        });
    }

    private void setupListView() {
        setupSpecificationsList();
        itemsSpeci = new ArrayList<>();
        itemsSpeci.addAll(mySpecification); // from database
        itemsSpeci.addAll(Arrays.asList(SpecificationsItemList.getSpecifications())); // from app

        ArrayList<String> stringArrayList = new ArrayList<>(itemsSpeci);
        for(int i=0 ; i<mySpecification.size(); i++){
            stringArrayList.add(i, (mySpecification.get(i)+"  "+addedCustomizationText));
            stringArrayList.remove(i+1);
        }

        listView_Adapter = new ArrayAdapter<>(mContext, R.layout.custom_list_item_multiple_choice, stringArrayList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(listView_Adapter);

        autoCompleteText_Adapter = new ArrayAdapter<String> (mContext,android.R.layout.simple_list_item_1, stringArrayList);
        mSearchSpeci.setAdapter(autoCompleteText_Adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean isCheck = listView.isItemChecked(position);
                if(isCheck){
                    addSpecification(itemsSpeci.get(position));
                }else{
                    removeSpecification(itemsSpeci.get(position));
                }

            }
        });

        mSearchSpeci.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));

                // HideSoftInputFromWindow
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View v = ((Activity)mContext).getCurrentFocus();
                if (v == null) {
                    v = new View(mContext);
                }
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                Object item = parent.getItemAtPosition(position);
                int positionInList = listView_Adapter.getPosition((String)item);
                listView.setSelection(positionInList);
                boolean isCheck = listView.isItemChecked(positionInList);

                if(!isCheck){
                    addSpecification(itemsSpeci.get(positionInList));
                    listView.setItemChecked(positionInList, true);
                }else{
                    removeSpecification(itemsSpeci.get(positionInList));
                    listView.setItemChecked(positionInList, false);
                }
                mSearchSpeci.setText("");

            }
        });

        mSearchSpeci.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                mSearchSpeci.showDropDown();
                return false;
            }
        });

    }

    private void addSpecification(String item) {
        mOnActivityDataItemListener.getSelectedSpecifications().add(item);
    }

    private void removeSpecification(String item){
        mOnActivityDataItemListener.getSelectedSpecifications().remove(item);
    }

    private void itemAlreadySelected(){
        ArrayList<String> selectedItems = mOnActivityDataItemListener.getSelectedSpecifications();
        for(String specification : selectedItems){
            if(itemsSpeci.contains(specification)){
                selectedSinglesSpecifications.add(specification);
                listView.setItemChecked(itemsSpeci.indexOf(specification), true);
            }
        }
    }

    private void setupListViewDeleteSpeci(){
        listView.setVisibility(View.GONE);

        mDeleteSpeci.setEnabled(false);
        mDeleteSpeci.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.VISIBLE);
        mSearchSpeci.setEnabled(false);

        addNewSpeci.setVisibility(View.GONE);
        deleteSpeci.setVisibility(View.VISIBLE);

        ((ImageView)view.findViewById(R.id.image)).setColorFilter(mContext.getResources().getColor(R.color.gray1), PorterDuff.Mode.SRC_ATOP);

        if(mySpecification.size() == 0){
           view.findViewById(R.id. no_saved_).setVisibility(View.VISIBLE);

        }else {
            final ArrayAdapter<String> listView_Adapter_delete = new ArrayAdapter<String>(mContext, R.layout.custom_list_item_multiple_choice_red,
                    mySpecification);
            listView_deleteSpeci.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView_deleteSpeci.setAdapter(listView_Adapter_delete);
            listView_deleteSpeci.setVisibility(View.VISIBLE);

            deleteMySpecification = new ArrayList<>();
            listView_deleteSpeci.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    boolean isCheck = listView_deleteSpeci.isItemChecked(position);
                    if(isCheck){
                        deleteMySpecification.add(listView_Adapter_delete.getItem(position));
                    }else{
                        deleteMySpecification.remove(listView_Adapter_delete.getItem(position));
                    }

                }
            });
        }

    }

    private void cancelDeleteMode(){

        cancel.setVisibility(View.GONE);
        mDeleteSpeci.setEnabled(true);
        mDeleteSpeci.setVisibility(View.VISIBLE);
        mSearchSpeci.setEnabled(true);
        ((ImageView)view.findViewById(R.id.image)).setColorFilter(mContext.getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_ATOP);

        listView_deleteSpeci.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        deleteSpeci.setVisibility(View.GONE);
        addNewSpeci.setVisibility(View.VISIBLE);

        view.findViewById(R.id. no_saved_).setVisibility(View.GONE);

    }

    private void setupSpecificationsList(){
        SpecificationsItemList.setSpecifications_keys(mContext.getResources().getStringArray(R.array.specifications_keys));
        SpecificationsItemList.setSpecifications(mContext.getResources().getStringArray(R.array.specifications));
    }

    private boolean checkIsDataInList(String item){
        if(itemsSpeci.contains(item)){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_data_already_there));
            return true;
        }

        return false;
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

    private void getMySpecificationsFromDb(){

        mySpecification = new ArrayList<>();

        setProgressBarTop(true, true, false, false);

        Query query = myRef
                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_single_specifications));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mySpecification = (ArrayList<String>) dataSnapshot.getValue();
                }
                globalVariable.setMySinglesSpecifications(mySpecification);

                setupListView();
                itemAlreadySelected();

                setProgressBarTop(false, true, false, true);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled" + databaseError.getMessage());
                setProgressBarTop(false, true, false, false);
                Toast.makeText(mContext,  mContext.getString(R.string.happened_wrong_try_again) + databaseError.getMessage() ,
                        Toast.LENGTH_SHORT).show();

                setupListView();
                itemAlreadySelected();
            }
        });

    }

    private void addingNewSpecifications(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        final EditText nameSavedSpecification = _view.findViewById(R.id.edit_text);
        TextView note = _view.findViewById(R.id.text_view1);
        TextView call = _view.findViewById(R.id.text_view2);
        nameSavedSpecification.setVisibility(View.VISIBLE);
        //note.setVisibility(View.VISIBLE);
        //call.setVisibility(View.VISIBLE);
        //note.setText(mContext.getString(R.string.note_add_new_spic));
        //note.setTextColor(mContext.getResources().getColor(R.color.red));
        //call.setText(mContext.getString(R.string.technical_support));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.add_new_speci));
        builder.setView(_view);
        builder.setPositiveButton(mContext.getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if(nameSavedSpecification.getText() != null && nameSavedSpecification.length()>1){

                    final String nameNewSpe = StringManipulation.removeLastSpace(nameSavedSpecification.getText().toString());
                    if(!checkIsDataInList(nameNewSpe)){

                        setProgressBarTop(true, false, true, false);

                        mySpecification.add(nameNewSpe);
                        FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                                .child(entagePageId)
                                .child(mContext.getString(R.string.field_saved_single_specifications))
                                .setValue(mySpecification)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        globalVariable.setMySinglesSpecifications(mySpecification);

                                        setupListView();
                                        itemAlreadySelected();

                                        setProgressBarTop(false, false, true, true);
                                        Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        setProgressBarTop(false, false, true, false);
                                        mySpecification.remove(nameNewSpe);

                                        if(e.getMessage().contains("Permission denied")){
                                            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_internet));

                                        }else {
                                            messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ " "+
                                                    e.getMessage());
                                        }
                                    }
                                });
                    }
                }else {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_named));

                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteSpecifications(){

        ArrayList<String> arrayList = new ArrayList<>(mySpecification);
        arrayList.removeAll(deleteMySpecification);

        setProgressBarTop(true, false, true, false);

        FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_single_specifications))
                .setValue(arrayList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        mySpecification.removeAll(deleteMySpecification);
                        globalVariable.setMySinglesSpecifications(mySpecification);
                        deleteMySpecification.clear();

                        setupListView();
                        itemAlreadySelected();

                        cancelDeleteMode();

                        setProgressBarTop(false, false, true, true);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        cancelDeleteMode();
                        setProgressBarTop(false, false, true, false);
                        if(e.getMessage().equals("Permission denied")){
                            Toast.makeText(mContext, mContext.getString(R.string.error_permission_denied),
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(mContext, mContext.getString(R.string.error_internet),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }) ;
    }

    /*  -------------------------------Firebase-------------------------------------------------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
    }

}
