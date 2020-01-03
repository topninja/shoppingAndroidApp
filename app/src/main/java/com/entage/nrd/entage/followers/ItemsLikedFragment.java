package com.entage.nrd.entage.followers;

import android.content.Context;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemsLikedFragment extends Fragment {
    private static final String TAG = "ItemsLikedFragment";
    private static final int ACTIVITY_NUM = 3 ;

    private OnActivityListener mOnActivityListener;
    private DatabaseReference myRef;
    private String user_id;

    //vars
    private ArrayList<String> entagePagesIds;
    private ArrayList<ItemWithDataUser> resultItems;
    private AdapterItems adapterItems;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private View view;
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_recycler_view , container , false);
            mContext = getActivity();

            //init();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
    }

    private void init() {
        initWidgets();
        onClickListener();

        setDataToList();
        getLikedItemsIds();
    }

    private void initWidgets(){
        entagePagesIds = new ArrayList<>();
        resultItems = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar_1);
        //mItems = new ArrayList<>();
    }

    private void onClickListener(){

    }

    private void setDataToList(){
        Log.d(TAG, "updateUsersList: updating users list");

        recyclerView =  view.findViewById(R.id.recyclerView_items);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        resultItems = new ArrayList<>();
        //adapterItems = new AdapterItems(mContext,recyclerView, resultItems);
        recyclerView.setAdapter(adapterItems);
    }

    private void getLikedItemsIds(){
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef = FirebaseDatabase.getInstance().getReference();

        Query query = myRef
                .child(mContext.getString(R.string.dbname_item_likes_user))
                .child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        getItem(singleSnapshot.getKey());
                    }
                    progressBar.setVisibility(View.GONE);

                }else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getItem(String idItem){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_items))
                .child(idItem);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    checkWishList(dataSnapshot.getValue(Item.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // checking methods
    private void checkWishList(Item item){

        final ItemWithDataUser itemWithDataUser = new ItemWithDataUser(item, true, false);

        Query query = myRef
                .child(mContext.getString(R.string.dbname_users_wish_list))
                .child(user_id)
                .child(itemWithDataUser.getItem().getItem_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    itemWithDataUser.setInWishList(true);
                }

                resultItems.add(itemWithDataUser);
                adapterItems.notifyItemInserted(resultItems.size()-1);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    //

}
