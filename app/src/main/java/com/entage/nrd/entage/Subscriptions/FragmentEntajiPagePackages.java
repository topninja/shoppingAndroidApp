package com.entage.nrd.entage.Subscriptions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
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

public class FragmentEntajiPagePackages extends Fragment {
    private static final String TAG = "FragmentEntajiPagePacka";

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef, referenceSubscriptions;
    private String user_id;

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    private RecyclerView recyclerView;
    private AdapterPackages adapterPackages;
    private ArrayList<SubscriptionPackage> packages;
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

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        super.onAttach(context);
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
        setupAdapter();

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

        packages = new ArrayList<>();

        packages.add(null);

        adapterPackages = new AdapterPackages(mContext, recyclerView, packages, null,false, entajiPagesId);
        recyclerView.setAdapter(adapterPackages);
    }

    private void fetchEntajiPagePackages(){
        Query query = referenceSubscriptions;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    removeProgressBar();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        int index = packages.size();
                        packages.add(index, snapshot.getValue(SubscriptionPackage.class));
                        adapterPackages.notifyItemInserted(index);
                    }
                    /*referenceSubscriptions.child("1_starter")
                            .setValue(packages.get(1));*/
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void removeProgressBar(){
        if(packages.size()>0 && packages.get(0) == null){
            packages.clear();
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
                .child(mContext.getString(R.string.dbname_app_data))
                    .child(mContext.getString(R.string.field_entaji_page_packages));
    }


}
