package com.entage.nrd.entage.newItem;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.Advice;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterAdvices;

import java.util.ArrayList;

public class FragmentAdvices extends Fragment {
    private static final String TAG = "FragmentNameItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private AdapterAdvices adapterAdvices;
    private ArrayList<Advice> advices;

    private String title_open;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        mContext = getActivity();

        getIncomingBundle();
        init();

        return view;
    }

    @Override
    public void onAttach(Context context) {
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
                title_open = bundle.getString("title_open");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        onClickListener();

        setupAdapter();
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getString(R.string.advices_add_new_item));

        String [] advices_titles = mContext.getResources().getStringArray(R.array.advices);

        advices = new ArrayList<>();

        // advices_name_item
        add(advices_titles[0],mContext.getResources().getStringArray(R.array.advices_name_item), R.drawable.ic_name_item);

        // advices_photo_item
        add(advices_titles[1],mContext.getResources().getStringArray(R.array.advices_photo_item), R.drawable.ic_images_item);

        // advices_categories_item
        add(advices_titles[2],mContext.getResources().getStringArray(R.array.advices_categories_item), R.drawable.ic_categories_item);

        // advices_description_item
        add(advices_titles[3],mContext.getResources().getStringArray(R.array.advices_description_item), R.drawable.ic_description_item);

        // advices_specification_item
        add(advices_titles[4],mContext.getResources().getStringArray(R.array.advices_specification_item), R.drawable.ic_specifications_item);

        // advices_options_item
        add(advices_titles[5],mContext.getResources().getStringArray(R.array.advices_options_item), R.drawable.ic_options_item);

        // advices_price_item
        add(advices_titles[6],mContext.getResources().getStringArray(R.array.advices_price_item), R.drawable.ic_price_item);

        // advices_shipping_item
        add(advices_titles[7],mContext.getResources().getStringArray(R.array.advices_shipping_item), R.drawable.ic_shipping_item);

        // advices_payment_item
        add(advices_titles[8],mContext.getResources().getStringArray(R.array.advices_payment_item), R.drawable.ic_payment_item);

    }

    private void onClickListener(){
    }

    private void setupAdapter(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapterAdvices = new AdapterAdvices(mContext, recyclerView, advices);
        recyclerView.setAdapter(adapterAdvices);
    }

    private void add(String title, String[] information, int imgRes){
        advices.add(new Advice(title, information,
                title.equals(title_open), imgRes));
    }
}
