package com.entage.nrd.entage.newItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterShowArchivesItem;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.ActivityForOpenFragments;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class FragmentAddItem extends Fragment {
    private static final String TAG = "FragmentAddItem";

    private Context mContext ;
    private View view ;

    private FirebaseDatabase mFirebaseDatabase;
    private OnActivityDataItemListener mOnActivityDataItemListener;
    private OnActivityListener mOnActivityListener;

    private ImageView back;
    private RelativeLayout addNewItem;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private String entagePageId, currentDivisionNameDb;
    private ArrayList<String> itemsIds, itemsNames, itemsDate;
    private AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add_new_item, container, false);
        mContext = getActivity();

        getIncomingBundle();
        init();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }
        catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
        mOnActivityDataItemListener = null;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId = bundle.getString("entagePageId");
                currentDivisionNameDb = bundle.getString("currentDivisionNameDb");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        onClickListener();

        checkEntagePageActivation();
        fetchSavedItems();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.adding_item));

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        addNewItem = view.findViewById(R.id.add_new_item);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void onClickListener() {
        addNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_createNewItem();
            }
        });
    }

    private void setupAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerView.getChildLayoutPosition(v);
                if(position != -1){
                    mOnActivityDataItemListener.setItemId(itemsIds.get(position));
                    mOnActivityDataItemListener.onActivityListener(new FragmentDataItem());
                }
            }
        };

        String [][] texts = new String[itemsIds.size()][2];
        for(int i=0; i<itemsIds.size(); i++){
            texts[i][0] = itemsNames.get(i);
            texts[i][1] = itemsDate.get(i);
        }

        AdapterShowArchivesItem basketAdapter = new AdapterShowArchivesItem(mContext, texts, onClickListener);
        recyclerView.setAdapter(basketAdapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void dialog_createNewItem(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_sharing_link, null, false);
        final TextView textView = _view.findViewById(R.id.sharing);
        textView.setText(mContext.getString(R.string.new_item));
        final TextView errorMsg = _view.findViewById(R.id.error);
        final AutoCompleteTextView autoCompleteTextCat = _view.findViewById(R.id.auto_complete_text);

        ((TextView)_view.findViewById(R.id.text)).setText(mContext.getString(R.string.enter_name_item));
        autoCompleteTextCat.setHint(mContext.getString(R.string.name_item));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alertSharing.dismiss();
                errorMsg.setVisibility(View.GONE);

                String string = autoCompleteTextCat.getText().toString();
                string = StringManipulation.removeLastSpace(string);
                string = StringManipulation.replaceSomeCharsToSpace(string);
                textView.setText(string);

                if (string.length() < 3){
                    errorMsg.setText(mContext.getString(R.string.error_name_item));
                    errorMsg.setVisibility(View.VISIBLE);

                }else {
                    textView.setClickable(false);
                    textView.setVisibility(View.INVISIBLE);
                    createNewItem(string, textView, errorMsg);
                }
            }
        });
    }

    private void getNumberITem(){

    }

    private void createNewItem(String item_name, final TextView textView, final TextView errorMsg){
        String newKey = FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbname_items)).push().getKey();
        final String itemId = newKey;
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(newKey != null){
             DatabaseReference myRefArchives = mFirebaseDatabase.getReference()
                     .child(mContext.getString(R.string.dbname_entage_pages_archives))
                    .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                    .child(itemId);

            final Item item = new Item();
            item.setItem_id(itemId);
            item.setItem_number(DateTime.getDateToday().getTime());
            item.setEntage_page_id(entagePageId);
            item.setName_item(mContext.getString(R.string.new_item_name));
            final String date = DateTime.getTimestamp();
            item.setDate_created(date);
            item.setLast_edit_was_in(date);
            item.setLast_publish_was_in("-1");
            item.setName_item(item_name);

            item.setWriter_id(user_id);
            item.setLanguageItem(Locale.getDefault().getLanguage());
            item.setItem_in_categorie(currentDivisionNameDb);
            item.setUsers_ids_has_access(new ArrayList<String>());
            item.getUsers_ids_has_access().add(user_id);

            myRefArchives.setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        if(mOnActivityDataItemListener != null){
                            alertDialog.dismiss();
                            mOnActivityDataItemListener.setItemId(itemId);
                            mOnActivityDataItemListener.onActivityListener(new FragmentDataItem());
                        }else {
                            getActivity().finish();
                        }

                    }else {
                        if(alertDialog.isShowing() && textView!= null){
                            textView.setVisibility(View.VISIBLE);
                            errorMsg.setText(mContext.getString(R.string.happened_wrong_title)+"\n"+
                                    mContext.getString(R.string.check_your_internet_connection));
                            errorMsg.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }else {
            if(alertDialog.isShowing() && textView!= null){
                textView.setVisibility(View.VISIBLE);
                errorMsg.setText(mContext.getString(R.string.happened_wrong_title)+"\n"+
                        mContext.getString(R.string.check_your_internet_connection));
                errorMsg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchSavedItems(){
        itemsIds = new ArrayList<>();
        itemsNames = new ArrayList<>();
        itemsDate = new ArrayList<>();

        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_items));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String id = (String) snapshot.child(mContext.getString(R.string.field_item_id)).getValue();
                        String name = (String) snapshot.child(mContext.getString(R.string.field_name_item)).getValue();
                        String createdDate = (String) snapshot.child(mContext.getString(R.string.field_date_created)).getValue();

                        if(id != null){
                            int index = itemsIds.size();
                            itemsIds.add(index, id);
                            itemsNames.add(index, name);
                            itemsDate.add(index, createdDate);
                        }
                    }

                    if(itemsIds.size() == 0){
                        view.findViewById(R.id.noDataFound).setVisibility(View.VISIBLE);
                    }else {
                        setupAdapter();
                    }

                }else {
                    view.findViewById(R.id.noDataFound).setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void checkEntagePageActivation(){
        mFirebaseDatabase.getReference()
                .child(getString(R.string.dbname_entage_pages_status))
                .child(entagePageId)
                .child("status_page")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(!dataSnapshot.getValue().equals("PAGE_AUTHORIZED")){
                                activate();
                            }
                        }else {
                            activate();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private AlertDialog _alertDialog ;
    private void activate(){
        if(isAdded() && mContext != null){
            View _view = this.getLayoutInflater().inflate(R.layout.dialog_activate_entage_page, null, false);

            TextView activation = _view.findViewById(R.id.activation);
            TextView cancel = _view.findViewById(R.id.cancel);

            activation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _alertDialog.dismiss();
                    Intent intent = new Intent(mContext, ActivityForOpenFragments.class);
                    intent.putExtra("notification_flag", "ActivateEntagePageFragment");
                    startActivity(intent);
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _alertDialog.dismiss();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(_view);
            _alertDialog = builder.create();
            _alertDialog.setCancelable(true);
            _alertDialog.setCanceledOnTouchOutside(false);
            _alertDialog.show();
        }
    }

}
