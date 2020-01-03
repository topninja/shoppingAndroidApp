package com.entage.nrd.entage.basket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterBasket;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.entage.OnActivityOrderListener;

public class FragmentBasket extends Fragment {
    private static final String TAG = "FragmentBasket";

    private Context mContext ;
    private View view;

    private OnActivityListener mOnActivityListener;


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_recycler_view_bar, container , false);
        mContext = getActivity();

        init();

        return view;
    }

    private void init() {
        initWidgets();
        onClickListener();

        setupAdapter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
    }


    private void initWidgets() {
        ImageView backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        backArrow.setColorFilter(Color.rgb(255, 255, 255), PorterDuff.Mode.SRC_ATOP);

        TextView titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.basket));
        titlePage.setTextColor(Color.rgb(255, 255, 255));
        view.findViewById(R.id.appBarLayout).setBackgroundColor(mContext.getColor(R.color.entage_blue));
    }

    private void onClickListener() {
        view.findViewById(R.id.orders).setVisibility(View.VISIBLE);
        view.findViewById(R.id.orders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserBasketActivity.class);
                intent.putExtra("type", "orders");
                mContext.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            }
        });
    }

    private void setupAdapter(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(false);


        AdapterBasket basketAdapter = new AdapterBasket(mContext, recyclerView, (TextView) view.findViewById(R.id.text0),
                mOnActivityListener);
        recyclerView.setAdapter(basketAdapter);
    }
}
