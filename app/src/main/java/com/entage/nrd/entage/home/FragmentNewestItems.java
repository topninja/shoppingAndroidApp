package com.entage.nrd.entage.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterNewestItems;

public class FragmentNewestItems extends Fragment {
    private static final String TAG = "FragmentNewestItems";

    //vars
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

    }

    private void initWidgets(){
        backArrow = view.findViewById(R.id.back);
        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.last_items));
        backArrow.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(gridLayoutManager);

        AdapterNewestItems mCardAdapter = new AdapterNewestItems(mContext, recyclerView, false);
        recyclerView.setAdapter(mCardAdapter);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }


}
