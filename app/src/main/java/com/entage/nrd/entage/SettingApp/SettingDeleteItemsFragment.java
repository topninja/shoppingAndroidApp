package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingDeleteItemsFragment extends Fragment {
    private static final String TAG = "SettingCategoriesFrag";

    private OnActivityListener mOnActivityListener;
    private View view;
    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private TextView loadMore;
    private ImageView back;
    private RecyclerView recyclerViewItems;
    private DeleteItemsAdapter deleteItemsAdapter;
    private ArrayList<String> ids;

    private String lastKey;
    private HashMap<String, Item> itemsByIds;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_delete_items, container, false);
        mContext = getActivity();

        setupFirebaseAuth();
        inti();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try {
            mOnActivityListener = (OnActivityListener) getActivity();
        } catch (ClassCastException e) {
            Log.d(TAG, "onAttach: ClassCastException;" + e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti() {
        initWidgets();
        onClickListener();

        fetchItems();
    }

    private void initWidgets() {
        back = view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        loadMore = view.findViewById(R.id.loade_more);
    }

    private void onClickListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchItemsLoadMore();
            }
        });
    }

    private void setupAdapter(){
        recyclerViewItems =  view.findViewById(R.id.recyclerView_items);
        recyclerViewItems.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.addItemDecoration(new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL));

        deleteItemsAdapter = new DeleteItemsAdapter(mContext, itemsByIds, ids);
        recyclerViewItems.setAdapter(deleteItemsAdapter);
    }

    private void fetchItems(){
        itemsByIds = new HashMap<>();
        ids = new ArrayList<>();

        Query query = myRef
                .child(mContext.getString(R.string.dbname_items))
                .orderByKey()
                .limitToFirst(10);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int x = 0;
                if(dataSnapshot.exists()){
                    // keys
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        itemsByIds.put(snapshot.getKey(), snapshot.getValue(Item.class));
                        ids.add(snapshot.getKey());
                        x++;
                        if(x==10){
                            lastKey = snapshot.getKey();
                        }
                    }
                }

                setupAdapter();

                loadMore.setVisibility(View.VISIBLE);
                view.findViewById(R.id.progressBarLoadMore).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void fetchItemsLoadMore(){

        if(lastKey != null){
            loadMore.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.progressBarLoadMore).setVisibility(View.VISIBLE);
            Query query = myRef
                    .child(mContext.getString(R.string.dbname_items))
                    .orderByKey()
                    .startAt(lastKey)
                    .limitToFirst(10);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int x = 0;
                    if(dataSnapshot.exists()){
                        // keys
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if(x!=0){
                                itemsByIds.put(snapshot.getKey(), snapshot.getValue(Item.class));
                                ids.add(snapshot.getKey());
                            }
                            x++;
                            if(x == 10){
                                lastKey = snapshot.getKey();
                            }
                        }
                    }

                    deleteItemsAdapter.notifyDataSetChanged();

                    loadMore.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.progressBarLoadMore).setVisibility(View.GONE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    loadMore.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.progressBarLoadMore).setVisibility(View.GONE);
                }
            });

            lastKey = null;

        }else {
            loadMore.setVisibility(View.GONE);
        }
    }


    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "SignIn : Uid:  " + user.getUid());
                }else {
                    getActivity().finish();
                    Log.d(TAG, "SignOut");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if( mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
