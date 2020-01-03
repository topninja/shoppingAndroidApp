package com.entage.nrd.entage.newItem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterFieldsSpecification;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.SpecificationsItemList;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentSpecificationsItem extends Fragment {
    private static final String TAG = "FragmentSpecificatio";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;

    private InputMethodManager imm ;
    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton chooseSpecifications, chooseGroupSpecifications, chooseSavedSpecifications ;
    private TextView hideAll, removeAll;
    private RelativeLayout saveSpecifications;

    private AdapterFieldsSpecification adapterSpecification;
    private ArrayList<DataSpecifications> dataSpecification;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();
    private String entagePageId, itemId;
    private boolean isDataFetched = true;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_specifications_item, container, false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();
            init();

        }else {

            mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[4]);
            mOnActivityDataItemListener.setIconBack(R.drawable.ic_options_back_item);

            getSpecifications();
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

        getDataFromDbArchives();
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[4]);

        floatingActionMenu =   view.findViewById(R.id.menu);
        floatingActionMenu.setClosedOnTouchOutside(true);

        floatingActionMenu = view.findViewById(R.id.menu);
        floatingActionMenu.setClosedOnTouchOutside(true);
        chooseSpecifications = view.findViewById(R.id.choose_specifications);
        chooseGroupSpecifications = view.findViewById(R.id.choose_group_specifications);
        chooseSavedSpecifications = view.findViewById(R.id.choose_saved_specifications);

        removeAll = view.findViewById(R.id.remove_all);
        hideAll = view.findViewById(R.id.hide_all);
        saveSpecifications = view.findViewById(R.id.save_specifications);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
    }

    private void onClickListener(){
        chooseSpecifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);

                if(adapterSpecification != null){
                    adapterSpecification.fetchData();

                    mOnActivityDataItemListener.setSelectedSpecifications(adapterSpecification.getNamesSpecification());
                    mOnActivityDataItemListener.onActivityListener(new FragmentChooseSpecifications());
                }
            }
        });

        chooseSavedSpecifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);

                if(adapterSpecification != null){
                    adapterSpecification.fetchData();

                    mOnActivityDataItemListener.onActivityListener(new FragmentChooseSavedSpecifications());
                }
            }
        });

        chooseGroupSpecifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionMenu.close(true);

                if(adapterSpecification != null){
                    adapterSpecification.fetchData();

                    mOnActivityDataItemListener.onActivityListener(new FragmentChooseGroupSpecifications());
                }
            }
        });

        removeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapterSpecification != null){
                    adapterSpecification.clear(true);
                }
            }
        });

        hideAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapterSpecification != null){
                    TextView textView = (TextView) v;
                    if(textView.getText().equals(mContext.getString(R.string.hide_all))){
                        adapterSpecification.hideAll();
                        textView.setText(mContext.getString(R.string.show_all));
                    }else {
                        adapterSpecification.showAll();
                        textView.setText(mContext.getString(R.string.hide_all));
                    }
                }
            }
        });

        saveSpecifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataSpecification.size()>0){
                    saveDescription();
                }else {
                    Toast.makeText(mContext,  mContext.getString(R.string.error_no_data_to_save) ,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupAdapter(){
        RecyclerView recyclerView =  view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapterSpecification = new AdapterFieldsSpecification(mContext, recyclerView, dataSpecification, linearLayoutManager);

        recyclerView.setAdapter(adapterSpecification);
    }

    private void saveDescription(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        final EditText nameSavedDescription = _view.findViewById(R.id.edit_text);
        nameSavedDescription.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.write_name_save_specifications));
        builder.setView(_view);
        builder.setPositiveButton(mContext.getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String name = nameSavedDescription.getText().toString();
                if(name != null && name.length()>1){

                    // collect data
                    ArrayList<DataSpecifications> specificationsArrayList = adapterSpecification.getSpecification();

                    if(specificationsArrayList.size()>0){

                        showProgress(false, true);

                        FirebaseDatabase.getInstance().getReference()
                                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                                .child(entagePageId)
                                .child(mContext.getString(R.string.field_saved_group_specifications))
                                .child(name)
                                .setValue(specificationsArrayList)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        globalVariable.setMyGroupSpecifications(null);
                                        showProgress(true, true);

                                        Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showProgress(true, true);

                                        if(e.getMessage().contains("Permission denied")){
                                            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                                        }else {
                                            messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ " " +
                                                    e.getMessage());
                                        }
                                    }
                                }) ;
                    }

                }else {
                    Toast.makeText(mContext,  mContext.getString(R.string.error_named) ,
                            Toast.LENGTH_SHORT).show();
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

    private void getSpecifications(){
        if(mOnActivityDataItemListener.getGroupSpecifications() != null){
            if(mOnActivityDataItemListener.getClearAndSet()){
                adapterSpecification.clear(false);
            }

            dataSpecification.addAll(mOnActivityDataItemListener.getGroupSpecifications());

        }else if (mOnActivityDataItemListener.getSelectedSpecifications() != null){
            ArrayList<String> namesSpecificationExisting = adapterSpecification.getNamesSpecification();
            ArrayList<String> namesSpecification = mOnActivityDataItemListener.getSelectedSpecifications();

            // check delete
            for(String spec : namesSpecificationExisting){
                if(!namesSpecification.contains(spec)){
                    // search for it
                    DataSpecifications delete = null;
                    for(DataSpecifications specifi : dataSpecification){
                        if(specifi.getSpecifications().equals(spec)){
                            delete = specifi;
                        }
                    }
                    if(delete != null){
                        dataSpecification.remove(delete);
                    }
                }
            }

            // check add
            for(String spec : namesSpecification){
                if(!namesSpecificationExisting.contains(spec)){
                    dataSpecification.add(new DataSpecifications(SpecificationsItemList.getSpecificationsByName(spec),
                            spec, ""));
                }
            }
        }

        adapterSpecification.notifyDataSetChanged();


        mOnActivityDataItemListener.setGroupSpecifications(null, false);
        mOnActivityDataItemListener.setSelectedSpecifications(null);
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
                .child(itemId).child(mContext.getString(R.string.field_specifications));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_specifications));

        myRefLastEdit = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_last_edit_was_in));

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void getDataFromDbArchives(){
        showProgress(false, false);

        dataSpecification = new ArrayList<>();

        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot single: dataSnapshot.getChildren()) {
                        dataSpecification.add(single.getValue(DataSpecifications.class));
                    }

                    if(dataSpecification.size()> 0 && dataSpecification.get(0).getSpecifications_id()!=null  &&
                            dataSpecification.get(0).getSpecifications_id().equals("-1")){
                        dataSpecification.remove(0);
                    }

                    setupAdapter();

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
    }

    private void getDataFromDbItems(){
        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot single: dataSnapshot.getChildren()) {
                        dataSpecification.add(single.getValue(DataSpecifications.class));
                    }

                    if(dataSpecification.size()> 0 && dataSpecification.get(0).getSpecifications_id()!=null  &&
                            dataSpecification.get(0).getSpecifications_id().equals("-1")){
                        dataSpecification.remove(0);
                    }
                }

                setupAdapter();
                showProgress(true, false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
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
        showProgress(false, true);

        ArrayList<DataSpecifications> specificationsArrayList = adapterSpecification.getSpecification();

        if(specificationsArrayList.size() == 0){
            specificationsArrayList.add(new DataSpecifications("-1","-1","-1"));
        }

        myRefArchives.setValue(specificationsArrayList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
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

    private void updateTimeLastEdit(){
        myRefLastEdit.setValue(DateTime.getTimestamp());
    }

    // setup Bar Add Item Layout
    private TextView save;
    private RelativeLayout nextStep;
    private ImageView tips;
    private void setupBarAddItemLayout(){
        save = view.findViewById(R.id.save);
        nextStep = view.findViewById(R.id.next_step);

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_options_item);

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
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentOptionsItem());
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
        final String title = mContext.getResources().getStringArray(R.array.advices)[4];
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
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_specification_item)[0]);
    }
}
