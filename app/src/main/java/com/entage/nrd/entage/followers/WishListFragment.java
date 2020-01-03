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
import android.widget.ImageView;
import android.widget.TextView;

import com.entage.nrd.entage.Models.ItemShortData;
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

public class WishListFragment extends Fragment {
    private static final String TAG = "WishListFragment";
    private static final int ACTIVITY_NUM = 3 ;

    private String user_id;
    private DatabaseReference myRef;

    //vars
    private ArrayList<ItemShortData> items;
    private AdapterItems adapterItems;
    private RecyclerView recyclerView;

    private View view;
    private Context mContext;

    private ImageView backArrow;
    private TextView titlePage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_recycler_view_bar , container , false);
            mContext = getActivity();

            init();
        }

        return view;
    }

    private void init() {
        initWidgets();
        onClickListener();

        setDataToList();
        getWishListIds();
    }

    private void initWidgets(){
        items = new ArrayList<>();

        backArrow = (ImageView) view.findViewById(R.id.back);
        titlePage = (TextView) view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.myWishList));
        backArrow.setVisibility(View.VISIBLE);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setDataToList(){
        Log.d(TAG, "updateUsersList: updating users list");

        recyclerView =  view.findViewById(R.id.recyclerView);

        items = new ArrayList<>();
        items.add(null);items.add(null);items.add(null);
        items.add(null);items.add(null);items.add(null);
        items.add(null);items.add(null);items.add(null);

        adapterItems = new AdapterItems(mContext,recyclerView, items);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterItems);
    }

    private void getWishListIds(){
        Log.d(TAG, "getFollowing: searching for following");
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        myRef = FirebaseDatabase.getInstance().getReference();
        Query query = myRef
                .child(mContext.getString(R.string.dbname_users_wish_list))
                .child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        getItem(singleSnapshot.getKey());
                    }

                }else {
                  recyclerView.setVisibility(View.GONE);
                  view.findViewById(R.id.text0).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
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
                    if(items.get(0) == null){
                        items.clear();
                        adapterItems.notifyDataSetChanged();
                    }

                    int index = items.size();
                    items.add(index, dataSnapshot.getValue(ItemShortData.class));
                    adapterItems.notifyItemInserted(index);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    //

}
