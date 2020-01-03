package com.entage.nrd.entage.createEntagePage;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.newItem.OnActivityDataItemListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterFAQ;
import com.entage.nrd.entage.adapters.AdapterPackages;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class FragmentShowInfo extends Fragment {
    private static final String TAG = "FragmentShowInfo";

    private static final int ACTIVITY_NUM = 2;

    private CreateEntagePageListener mCreateEntagePageListener;
    private OnActivityDataItemListener mOnActivityDataItemListener;
    private Context mContext ;
    private View view;

    private String info;
    private String[][] afq;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recycler_view_bar , container , false);
        mContext = getActivity();

        getIncomingBundle();
        init();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mCreateEntagePageListener = (CreateEntagePageListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
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
                info = bundle.getString("info");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(){
        ((TextView)view.findViewById(R.id.titlePage)).setText("");

        if(mCreateEntagePageListener != null){
            mCreateEntagePageListener.show_hideBar(false);
        }

        if(mOnActivityDataItemListener!=null){
            view.findViewById(R.id.appBarLayout).setVisibility(View.GONE);
            mOnActivityDataItemListener.setTitle(mContext.getString(R.string.service_amount));
            mOnActivityDataItemListener.setIconBack(R.drawable.ic_back);
        }

        view.findViewById(R.id.back).setVisibility(View.VISIBLE);
        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });


        ///
        recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setVisibility(View.VISIBLE);

       if(info.equals(mContext.getString(R.string.what_are_entage_features_ans_3))){
           ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.features_entage_page));
           setQuestions(new ArrayList<>(Arrays.asList(mContext.getResources().getStringArray(R.array.features_entage_page))));

        }else if(info.equals(mContext.getString(R.string.see_details_fees_sale))){
           ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.service_amount));
           setQuestions(new ArrayList<>(Arrays.asList(mContext.getResources().getStringArray(R.array.afq_amount_deducted))));

        }else if(info.equals(mContext.getString(R.string.see_packages))){
           ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.see_packages));
           fetchEntajiPagePackages();

        }else if(info.equals("afterSenOrder")){
           ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.faq_after_sen_order));
           afterSenOrder();
       }

       if(afq != null){
           AdapterFAQ basketAdapter = new AdapterFAQ(mContext, recyclerView, afq);
           recyclerView.setAdapter(basketAdapter);
       }
    }


    private void afterSenOrder(){
        afq = new String[7][2];
        afq[0][0] = mContext.getString(R.string.send_order_qu_1);
        afq[0][1] = mContext.getString(R.string.send_order_an_1);
        afq[1][0] = mContext.getString(R.string.send_order_qu_2);
        afq[1][1] = mContext.getString(R.string.send_order_an_2);
        afq[2][0] = mContext.getString(R.string.send_order_qu_3);
        afq[2][1] = mContext.getString(R.string.send_order_an_3);
        afq[3][0] = mContext.getString(R.string.send_order_qu_4);
        afq[3][1] = mContext.getString(R.string.send_order_an_4);
        afq[4][0] = mContext.getString(R.string.send_order_qu_5);
        afq[4][1] = mContext.getString(R.string.send_order_an_5);
        afq[5][0] = mContext.getString(R.string.send_order_qu_6);
        afq[5][1] = mContext.getString(R.string.send_order_an_6);
        afq[6][0] = mContext.getString(R.string.send_order_qu_7);
        afq[6][1] = mContext.getString(R.string.send_order_an_7);
    }

    private void setQuestions(ArrayList<String> arrayList ){
        int qusCount = arrayList.size()/2;
        afq = new String[qusCount][2];
        int x =0;
        for(int i=0 ;i<arrayList.size(); i++){
            afq[x][0] = arrayList.get(i); i++;
            afq[x][1] = arrayList.get(i);
            x++;
        }
    }

    private void fetchEntajiPagePackages(){
        final ArrayList<SubscriptionPackage> packages = new ArrayList<>();
        packages.add(null);packages.add(null);
        final AdapterPackages adapterPackages = new AdapterPackages(mContext, recyclerView, packages, null, true, null);
        recyclerView.setAdapter(adapterPackages);

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_app_data))
                .child(mContext.getString(R.string.field_entaji_page_packages));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //removeProgressBar();
                    packages.clear();
                    adapterPackages.notifyDataSetChanged();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        int index = packages.size();
                        packages.add(index, snapshot.getValue(SubscriptionPackage.class));
                        adapterPackages.notifyItemInserted(index);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

}
