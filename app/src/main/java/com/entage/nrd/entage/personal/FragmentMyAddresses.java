package com.entage.nrd.entage.personal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentMyAddresses extends Fragment {
    private static final String TAG = "FragmentEditPe";

    private OnActivityListener mOnActivityListener;

    private View view;
    private Context mContext;

    private ImageView backArrow;
    private TextView  titlePage, add_new_addresses, no_addresses ;
    private ProgressBar progressBar;
    private ListView listView;

    private ArrayList<MyAddress> myAddresses;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_addresses , container , false);
        mContext = getActivity();

        //setupFirebaseAuth();
        inti();

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

    private void inti(){
        initWidgets();
        onClickListener();

        fetchMyAddresses();
    }

    private void initWidgets(){
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.my_address));

        add_new_addresses = view.findViewById(R.id.add_new_addresses);
        no_addresses = view.findViewById(R.id.no_addresses);
        progressBar = view.findViewById(R.id.progressBar_9);

        listView = view.findViewById(R.id.listView);

        view.findViewById(R.id.layout).setVisibility(View.VISIBLE);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        add_new_addresses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("myAddresses", myAddresses);
                bundle.putInt("position", -1);
                mOnActivityListener.onActivityListener(new FragmentAddAddress(), bundle);
            }
        });
    }

    private void fetchMyAddresses(){
        progressBar.setVisibility(View.VISIBLE);
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // get my addresses
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_addresses))
                .child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    myAddresses = new ArrayList<>();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        myAddresses.add(snapshot.getValue(MyAddress.class));
                    }

                    setupAdapter();

                }else {
                    progressBar.setVisibility(View.GONE);
                    no_addresses.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.line).setVisibility(View.VISIBLE);
                    add_new_addresses.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void setupAdapter() {
        progressBar.setVisibility(View.GONE);
        ArrayList<String> arrayList = new ArrayList<>();
        for(MyAddress rl : myAddresses){
            arrayList.add(rl.getTitle());
        }

        listView.setVisibility(View.VISIBLE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_black_text, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                //String value = (String)adapter.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("myAddresses", myAddresses);
                bundle.putInt("position", position);
                mOnActivityListener.onActivityListener(new FragmentAddAddress(), bundle);
            }
        });

        if(myAddresses.size()==2){
            add_new_addresses.setVisibility(View.GONE);
            view.findViewById(R.id.line).setVisibility(View.GONE);
        }else {
            view.findViewById(R.id.line).setVisibility(View.VISIBLE);
            add_new_addresses.setVisibility(View.VISIBLE);
        }
    }
}
