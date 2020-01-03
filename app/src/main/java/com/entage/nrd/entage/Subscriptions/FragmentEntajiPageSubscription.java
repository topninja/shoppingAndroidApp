package com.entage.nrd.entage.Subscriptions;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterPackages;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FragmentEntajiPageSubscription extends Fragment {
    private static final String TAG = "FragmentMessages";

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, referenceSubscriptions;
    private String user_id;

    private Context mContext ;
    private View view;

    private RecyclerView recyclerView;
    private AdapterPackages adapterPackages;
    private ArrayList<SubscriptionPackage> subscription;
    private HashMap<String, SubscriptionPackage> packageHashMap;
    private String entajiPagesId;

    private boolean isRemovedProgress = false;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_recycler_view, container , false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();

            init();
        }
        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entajiPagesId =  bundle.getString("entajiPagesIds");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        onClickListener();

        fetchEntajiPagePackages();
    }

    private void initWidgets() {
    }

    private void onClickListener() {
    }

    private void setupAdapter(){
        recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        subscription = new ArrayList<>();
        subscription.add(null);

        adapterPackages = new AdapterPackages(mContext, recyclerView, subscription, packageHashMap, false, entajiPagesId);
        recyclerView.setAdapter(adapterPackages);
    }

    private void fetchEntajiPagePackages(){
        packageHashMap = new HashMap<>();

        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_app_data))
                .child(mContext.getString(R.string.field_entaji_page_packages));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        packageHashMap.put(snapshot.getKey(), snapshot.getValue(SubscriptionPackage.class));
                    }

                    setupAdapter();
                    fetchEntajiPageSubscriptions();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void fetchEntajiPageSubscriptions(){
        referenceSubscriptions
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                removeProgressBar();
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(snapshot.getKey().equals(mContext.getString(R.string.dbname_current_subscription))){
                            subscription.add(0, snapshot.getValue(SubscriptionPackage.class));
                        }else {
                            subscription.add(snapshot.getValue(SubscriptionPackage.class));
                        }
                    }
                    adapterPackages.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void removeProgressBar(){
        if(subscription.size()>0 && subscription.get(0) == null){
            subscription.clear();
            adapterPackages.notifyDataSetChanged();
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
    */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        referenceSubscriptions = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_subscription))
                .child(entajiPagesId);
    }

}
