package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.EntagePageWithDataUser;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.EntagePagesSettingAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SettingEditePagesFragment extends Fragment {
    private static final String TAG = "SettingEditePages";

    //vars
    private ArrayList<EntagePageWithDataUser> resultEntagePages;
    private EntagePagesSettingAdapter entagePagesAdapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

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
        getPages();
    }

    private void initWidgets(){
        resultEntagePages = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar_1);


        backArrow = view.findViewById(R.id.back);
        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.edit_entage_pages));
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
        entagePagesAdapter = new EntagePagesSettingAdapter(mContext , recyclerView, resultEntagePages);
        recyclerView.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        recyclerView.setAdapter(entagePagesAdapter);
    }

    private void getPages(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_entage_pages));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        resultEntagePages.add(new EntagePageWithDataUser(snapshot.getValue(EntagePage.class), false, false,
                                null));
                        entagePagesAdapter.notifyItemInserted(resultEntagePages.size()-1);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
